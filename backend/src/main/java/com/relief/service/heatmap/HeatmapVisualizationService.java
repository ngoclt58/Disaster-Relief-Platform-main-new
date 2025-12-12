package com.relief.service.heatmap;

import com.relief.domain.heatmap.*;
import com.relief.repository.heatmap.HeatmapConfigurationRepository;
import com.relief.repository.heatmap.HeatmapDataRepository;
import com.relief.repository.heatmap.HeatmapLayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for heatmap visualization and layer generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HeatmapVisualizationService {

    private static final Logger log = LoggerFactory.getLogger(HeatmapVisualizationService.class);
    
    private final HeatmapDataRepository heatmapDataRepository;
    private final HeatmapConfigurationRepository configurationRepository;
    private final HeatmapLayerRepository layerRepository;
    private final GeometryFactory geometryFactory;
    
    /**
     * Generate heatmap layer for visualization
     */
    @Transactional
    public HeatmapLayer generateHeatmapLayer(HeatmapLayerRequest request) {
        log.info("Generating heatmap layer for type: {}", request.heatmapType());
        
        // Get configuration
        HeatmapConfiguration config = getConfiguration(request.heatmapType(), request.configurationId());
        
        // Get data points
        List<HeatmapData> dataPoints = getDataPoints(request, config);
        
        if (dataPoints.isEmpty()) {
            throw new IllegalArgumentException("No data points available for heatmap generation");
        }
        
        // Calculate bounds
        Envelope bounds = calculateBounds(dataPoints);
        Polygon boundsPolygon = createBoundsPolygon(bounds);
        
        // Generate tiles
        String tileUrlTemplate = generateTiles(dataPoints, config, request);
        
        // Calculate statistics
        HeatmapStatistics stats = calculateStatistics(dataPoints);
        
        // Create heatmap layer
        HeatmapLayer layer = HeatmapLayer.builder()
            .name(request.name())
            .description(request.description())
            .heatmapType(request.heatmapType())
            .bounds(boundsPolygon)
            .tileUrlTemplate(tileUrlTemplate)
            .minZoom(config.getMinZoom() != null ? config.getMinZoom() : 1)
            .maxZoom(config.getMaxZoom() != null ? config.getMaxZoom() : 18)
            .tileSize(config.getTileSize() != null ? config.getTileSize() : 256)
            .dataPointsCount((long) dataPoints.size())
            .intensityMin(stats.minIntensity())
            .intensityMax(stats.maxIntensity())
            .intensityAvg(stats.avgIntensity())
            .configurationId(config.getId())
            .generationParameters(request.generationParameters())
            .fileSizeBytes(calculateFileSize(dataPoints))
            .isPublic(request.isPublic())
            .expiresAt(request.expiresAt())
            .build();
            
        return layerRepository.save(layer);
    }
    
    /**
     * Get heatmap layer for visualization
     */
    public Optional<HeatmapLayer> getHeatmapLayer(Long layerId) {
        return layerRepository.findById(layerId);
    }
    
    /**
     * Get heatmap layers by type
     */
    public List<HeatmapLayer> getHeatmapLayersByType(HeatmapType heatmapType) {
        return layerRepository.findByHeatmapType(heatmapType);
    }
    
    /**
     * Get public heatmap layers
     */
    public List<HeatmapLayer> getPublicHeatmapLayers() {
        return layerRepository.findByIsPublicTrue();
    }
    
    /**
     * Get heatmap layers within bounds
     */
    public List<HeatmapLayer> getHeatmapLayersInBounds(double minLon, double minLat,
                                                      double maxLon, double maxLat) {
        return layerRepository.findWithinBounds(minLon, minLat, maxLon, maxLat);
    }
    
    /**
     * Get heatmap layers by type and bounds
     */
    public List<HeatmapLayer> getHeatmapLayersByTypeAndBounds(HeatmapType heatmapType,
                                                             double minLon, double minLat,
                                                             double maxLon, double maxLat) {
        return layerRepository.findByTypeAndBounds(
            heatmapType.name(), minLon, minLat, maxLon, maxLat
        );
    }
    
    /**
     * Generate heatmap tiles for MapLibre visualization
     */
    public HeatmapTileData generateHeatmapTiles(HeatmapTileRequest request) {
        log.info("Generating heatmap tiles for layer: {}", request.layerId());
        
        HeatmapLayer layer = layerRepository.findById(request.layerId())
            .orElseThrow(() -> new IllegalArgumentException("Heatmap layer not found"));
        
        // Get data points for the tile bounds
        List<HeatmapData> dataPoints = getDataPointsForTile(
            request.minLon(), request.minLat(),
            request.maxLon(), request.maxLat(),
            layer.getHeatmapType()
        );
        
        // Generate tile data
        List<HeatmapTilePoint> tilePoints = dataPoints.stream()
            .map(point -> new HeatmapTilePoint(
                point.getLocation().getX(),
                point.getLocation().getY(),
                point.getIntensity(),
                point.getWeight(),
                point.getRadius(),
                point.getCategory()
            ))
            .toList();
        
        // Calculate tile statistics
        HeatmapStatistics stats = calculateStatistics(dataPoints);
        
        return new HeatmapTileData(
            request.layerId(),
            request.minLon(), request.minLat(),
            request.maxLon(), request.maxLat(),
            tilePoints,
            stats,
            layer.getHeatmapType().name()
        );
    }
    
    /**
     * Get heatmap configuration
     */
    private HeatmapConfiguration getConfiguration(HeatmapType heatmapType, Long configurationId) {
        if (configurationId != null) {
            return configurationRepository.findById(configurationId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));
        }
        
        // Get default configuration for type
        List<HeatmapConfiguration> configs = configurationRepository
            .findByHeatmapTypeAndIsActiveTrue(heatmapType);
        
        if (configs.isEmpty()) {
            // Create default configuration
            return createDefaultConfiguration(heatmapType);
        }
        
        return configs.get(0);
    }
    
    /**
     * Get data points for heatmap generation
     */
    private List<HeatmapData> getDataPoints(HeatmapLayerRequest request, HeatmapConfiguration config) {
        LocalDateTime startDate = request.startDate() != null ? request.startDate() : 
            LocalDateTime.now().minusHours(config.getTimeWindowHours() != null ? config.getTimeWindowHours() : 24);
        LocalDateTime endDate = request.endDate() != null ? request.endDate() : LocalDateTime.now();
        
        if (request.bounds() != null) {
            return heatmapDataRepository.findByTypeAndBounds(
                request.heatmapType().name(),
                request.bounds().minLon(), request.bounds().minLat(),
                request.bounds().maxLon(), request.bounds().maxLat()
            );
        } else {
            return heatmapDataRepository.findByHeatmapType(request.heatmapType());
        }
    }
    
    /**
     * Get data points for tile
     */
    private List<HeatmapData> getDataPointsForTile(double minLon, double minLat,
                                                  double maxLon, double maxLat,
                                                  HeatmapType heatmapType) {
        return heatmapDataRepository.findByTypeAndBounds(
            heatmapType.name(), minLon, minLat, maxLon, maxLat
        );
    }
    
    /**
     * Calculate bounds from data points
     */
    private Envelope calculateBounds(List<HeatmapData> dataPoints) {
        Envelope bounds = new Envelope();
        
        for (HeatmapData point : dataPoints) {
            bounds.expandToInclude(point.getLocation().getX(), point.getLocation().getY());
        }
        
        return bounds;
    }
    
    /**
     * Create bounds polygon
     */
    private Polygon createBoundsPolygon(Envelope bounds) {
        Coordinate[] coordinates = {
            new Coordinate(bounds.getMinX(), bounds.getMinY()),
            new Coordinate(bounds.getMaxX(), bounds.getMinY()),
            new Coordinate(bounds.getMaxX(), bounds.getMaxY()),
            new Coordinate(bounds.getMinX(), bounds.getMaxY()),
            new Coordinate(bounds.getMinX(), bounds.getMinY())
        };
        
        LinearRing ring = geometryFactory.createLinearRing(coordinates);
        return geometryFactory.createPolygon(ring);
    }
    
    /**
     * Generate tiles (simplified - in real implementation, this would generate actual map tiles)
     */
    private String generateTiles(List<HeatmapData> dataPoints, HeatmapConfiguration config, HeatmapLayerRequest request) {
        // In a real implementation, this would generate actual map tiles
        // For now, return a template URL
        return String.format("/api/heatmap/tiles/%s/{z}/{x}/{y}.png", request.name().replaceAll("\\s+", "_"));
    }
    
    /**
     * Calculate statistics from data points
     */
    private HeatmapStatistics calculateStatistics(List<HeatmapData> dataPoints) {
        if (dataPoints.isEmpty()) {
            return new HeatmapStatistics(0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
        
        double minIntensity = dataPoints.stream().mapToDouble(HeatmapData::getIntensity).min().orElse(0.0);
        double maxIntensity = dataPoints.stream().mapToDouble(HeatmapData::getIntensity).max().orElse(0.0);
        double avgIntensity = dataPoints.stream().mapToDouble(HeatmapData::getIntensity).average().orElse(0.0);
        
        double variance = dataPoints.stream()
            .mapToDouble(point -> Math.pow(point.getIntensity() - avgIntensity, 2))
            .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        double avgWeight = dataPoints.stream().mapToDouble(HeatmapData::getWeight).average().orElse(0.0);
        double avgRadius = dataPoints.stream().mapToDouble(HeatmapData::getRadius).average().orElse(0.0);
        
        return new HeatmapStatistics(
            (long) dataPoints.size(),
            avgIntensity,
            minIntensity,
            maxIntensity,
            stdDev,
            avgWeight,
            avgRadius
        );
    }
    
    /**
     * Calculate file size (simplified)
     */
    private Long calculateFileSize(List<HeatmapData> dataPoints) {
        // Simplified calculation - in real implementation, this would be the actual file size
        return (long) (dataPoints.size() * 100); // Assume 100 bytes per point
    }
    
    /**
     * Create default configuration for heatmap type
     */
    private HeatmapConfiguration createDefaultConfiguration(HeatmapType heatmapType) {
        HeatmapConfiguration config = HeatmapConfiguration.builder()
            .name("Default " + heatmapType.name())
            .description("Default configuration for " + heatmapType.name())
            .heatmapType(heatmapType)
            .colorScheme(getDefaultColorScheme(heatmapType))
            .intensityRangeMin(0.0)
            .intensityRangeMax(1.0)
            .radiusMultiplier(1.0)
            .opacity(0.6)
            .blurRadius(15.0)
            .gradientStops(getDefaultGradientStops(heatmapType))
            .aggregationMethod("SUM")
            .timeWindowHours(24)
            .spatialResolutionMeters(100.0)
            .isActive(true)
            .build();
            
        return configurationRepository.save(config);
    }
    
    /**
     * Get default color scheme for heatmap type
     */
    private String getDefaultColorScheme(HeatmapType heatmapType) {
        return switch (heatmapType) {
            case DISASTER_IMPACT -> "{\"colors\": [\"#00ff00\", \"#ffff00\", \"#ff8800\", \"#ff0000\"]}";
            case RESOURCE_DISTRIBUTION -> "{\"colors\": [\"#0000ff\", \"#0088ff\", \"#00ffff\", \"#88ff00\"]}";
            case RESPONSE_EFFECTIVENESS -> "{\"colors\": [\"#ff0000\", \"#ff8800\", \"#ffff00\", \"#00ff00\"]}";
            case NEEDS_DENSITY -> "{\"colors\": [\"#8800ff\", \"#ff00ff\", \"#ff0088\", \"#ff0000\"]}";
            default -> "{\"colors\": [\"#0000ff\", \"#00ff00\", \"#ffff00\", \"#ff0000\"]}";
        };
    }
    
    /**
     * Get default gradient stops for heatmap type
     */
    private String getDefaultGradientStops(HeatmapType heatmapType) {
        return "[{\"offset\": 0, \"color\": \"#00000000\"}, {\"offset\": 0.5, \"color\": \"#ff0000ff\"}, {\"offset\": 1, \"color\": \"#ffff00ff\"}]";
    }
    
    // Data classes
    public record HeatmapLayerRequest(
        String name,
        String description,
        HeatmapType heatmapType,
        Long configurationId,
        Bounds bounds,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String generationParameters,
        boolean isPublic,
        LocalDateTime expiresAt
    ) {}
    
    public record Bounds(
        double minLon, double minLat, double maxLon, double maxLat
    ) {}
    
    public record HeatmapTileRequest(
        Long layerId,
        double minLon, double minLat, double maxLon, double maxLat
    ) {}
    
    public record HeatmapTileData(
        Long layerId,
        double minLon, double minLat, double maxLon, double maxLat,
        List<HeatmapTilePoint> points,
        HeatmapStatistics statistics,
        String heatmapType
    ) {}
    
    public record HeatmapTilePoint(
        double longitude,
        double latitude,
        double intensity,
        double weight,
        double radius,
        String category
    ) {}
    
    public record HeatmapStatistics(
        Long pointCount,
        Double avgIntensity,
        Double minIntensity,
        Double maxIntensity,
        Double intensityStddev,
        Double avgWeight,
        Double avgRadius
    ) {}
}



