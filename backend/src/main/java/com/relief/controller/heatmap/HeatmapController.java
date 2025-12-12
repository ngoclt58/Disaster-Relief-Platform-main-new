package com.relief.controller.heatmap;

import com.relief.service.heatmap.HeatmapDataService;
import com.relief.service.heatmap.HeatmapVisualizationService;
import com.relief.domain.heatmap.HeatmapType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for heatmap data and visualization
 */
@RestController
@RequestMapping("/heatmap")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Heatmap", description = "Heatmap data and visualization services")
public class HeatmapController {
    
    private final HeatmapDataService heatmapDataService;
    private final HeatmapVisualizationService visualizationService;
    
    // Heatmap Data Endpoints
    
    /**
     * Add heatmap data point
     */
    @PostMapping("/data")
    @Operation(summary = "Add heatmap data point", description = "Add a new heatmap data point")
    public ResponseEntity<HeatmapDataResponse> addHeatmapData(
            @Valid @RequestBody HeatmapDataRequest request) {
        
        var dataRequest = new HeatmapDataService.HeatmapDataRequest(
            request.longitude(),
            request.latitude(),
            request.heatmapType(),
            request.intensity(),
            request.weight(),
            request.radius(),
            request.category(),
            request.metadata(),
            request.sourceId(),
            request.sourceType()
        );
        
        var heatmapData = heatmapDataService.addHeatmapData(dataRequest);
        
        var response = new HeatmapDataResponse(
            heatmapData.getId(),
            heatmapData.getLocation().getX(),
            heatmapData.getLocation().getY(),
            heatmapData.getHeatmapType().name(),
            heatmapData.getIntensity(),
            heatmapData.getWeight(),
            heatmapData.getRadius(),
            heatmapData.getCategory(),
            heatmapData.getMetadata(),
            heatmapData.getSourceId(),
            heatmapData.getSourceType(),
            heatmapData.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Bulk add heatmap data points
     */
    @PostMapping("/data/bulk")
    @Operation(summary = "Bulk add heatmap data", description = "Add multiple heatmap data points")
    public ResponseEntity<List<HeatmapDataResponse>> bulkAddHeatmapData(
            @Valid @RequestBody List<HeatmapDataRequest> requests) {
        
        var dataRequests = requests.stream()
            .map(request -> new HeatmapDataService.HeatmapDataRequest(
                request.longitude(),
                request.latitude(),
                request.heatmapType(),
                request.intensity(),
                request.weight(),
                request.radius(),
                request.category(),
                request.metadata(),
                request.sourceId(),
                request.sourceType()
            ))
            .toList();
        
        var heatmapDataList = heatmapDataService.bulkAddHeatmapData(dataRequests);
        
        List<HeatmapDataResponse> responses = heatmapDataList.stream()
            .map(data -> new HeatmapDataResponse(
                data.getId(),
                data.getLocation().getX(),
                data.getLocation().getY(),
                data.getHeatmapType().name(),
                data.getIntensity(),
                data.getWeight(),
                data.getRadius(),
                data.getCategory(),
                data.getMetadata(),
                data.getSourceId(),
                data.getSourceType(),
                data.getCreatedAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get heatmap data within bounds
     */
    @GetMapping("/data/bounds")
    @Operation(summary = "Get heatmap data in bounds", description = "Get heatmap data within a bounding box")
    public ResponseEntity<List<HeatmapDataResponse>> getHeatmapDataInBounds(
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var dataList = heatmapDataService.getHeatmapDataInBounds(minLon, minLat, maxLon, maxLat);
        
        List<HeatmapDataResponse> responses = dataList.stream()
            .map(data -> new HeatmapDataResponse(
                data.getId(),
                data.getLocation().getX(),
                data.getLocation().getY(),
                data.getHeatmapType().name(),
                data.getIntensity(),
                data.getWeight(),
                data.getRadius(),
                data.getCategory(),
                data.getMetadata(),
                data.getSourceId(),
                data.getSourceType(),
                data.getCreatedAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get heatmap data by type
     */
    @GetMapping("/data/type/{type}")
    @Operation(summary = "Get heatmap data by type", description = "Get heatmap data by type")
    public ResponseEntity<List<HeatmapDataResponse>> getHeatmapDataByType(
            @PathVariable HeatmapType type) {
        
        var dataList = heatmapDataService.getHeatmapDataByType(type);
        
        List<HeatmapDataResponse> responses = dataList.stream()
            .map(data -> new HeatmapDataResponse(
                data.getId(),
                data.getLocation().getX(),
                data.getLocation().getY(),
                data.getHeatmapType().name(),
                data.getIntensity(),
                data.getWeight(),
                data.getRadius(),
                data.getCategory(),
                data.getMetadata(),
                data.getSourceId(),
                data.getSourceType(),
                data.getCreatedAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get heatmap data by type and bounds
     */
    @GetMapping("/data/type/{type}/bounds")
    @Operation(summary = "Get heatmap data by type and bounds", description = "Get heatmap data by type within bounds")
    public ResponseEntity<List<HeatmapDataResponse>> getHeatmapDataByTypeAndBounds(
            @PathVariable HeatmapType type,
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var dataList = heatmapDataService.getHeatmapDataByTypeAndBounds(type, minLon, minLat, maxLon, maxLat);
        
        List<HeatmapDataResponse> responses = dataList.stream()
            .map(data -> new HeatmapDataResponse(
                data.getId(),
                data.getLocation().getX(),
                data.getLocation().getY(),
                data.getHeatmapType().name(),
                data.getIntensity(),
                data.getWeight(),
                data.getRadius(),
                data.getCategory(),
                data.getMetadata(),
                data.getSourceId(),
                data.getSourceType(),
                data.getCreatedAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get heatmap statistics
     */
    @GetMapping("/data/statistics")
    @Operation(summary = "Get heatmap statistics", description = "Get heatmap statistics for a type and date range")
    public ResponseEntity<HeatmapStatisticsResponse> getHeatmapStatistics(
            @Parameter(description = "Heatmap type") @RequestParam HeatmapType type,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        var stats = heatmapDataService.getHeatmapStatistics(type, startDate, endDate);
        
        var response = new HeatmapStatisticsResponse(
            stats.pointCount(),
            stats.avgIntensity(),
            stats.minIntensity(),
            stats.maxIntensity(),
            stats.intensityStddev(),
            stats.avgWeight(),
            stats.avgRadius()
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Heatmap Layer Endpoints
    
    /**
     * Generate heatmap layer
     */
    @PostMapping("/layers")
    @Operation(summary = "Generate heatmap layer", description = "Generate a new heatmap layer for visualization")
    public ResponseEntity<HeatmapLayerResponse> generateHeatmapLayer(
            @Valid @RequestBody HeatmapLayerRequest request) {
        
        var layerRequest = new HeatmapVisualizationService.HeatmapLayerRequest(
            request.name(),
            request.description(),
            request.heatmapType(),
            request.configurationId(),
            request.bounds() != null ? new HeatmapVisualizationService.Bounds(
                request.bounds().minLon(), request.bounds().minLat(),
                request.bounds().maxLon(), request.bounds().maxLat()
            ) : null,
            request.startDate(),
            request.endDate(),
            request.generationParameters(),
            request.isPublic(),
            request.expiresAt()
        );
        
        var layer = visualizationService.generateHeatmapLayer(layerRequest);
        
        var response = new HeatmapLayerResponse(
            layer.getId(),
            layer.getName(),
            layer.getDescription(),
            layer.getHeatmapType().name(),
            layer.getTileUrlTemplate(),
            layer.getMinZoom(),
            layer.getMaxZoom(),
            layer.getTileSize(),
            layer.getDataPointsCount(),
            layer.getIntensityMin(),
            layer.getIntensityMax(),
            layer.getIntensityAvg(),
            layer.getConfigurationId(),
            layer.getGenerationParameters(),
            layer.getFileSizeBytes(),
            layer.getIsPublic(),
            layer.getExpiresAt(),
            layer.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get heatmap layer
     */
    @GetMapping("/layers/{id}")
    @Operation(summary = "Get heatmap layer", description = "Get a specific heatmap layer")
    public ResponseEntity<HeatmapLayerResponse> getHeatmapLayer(@PathVariable Long id) {
        var layer = visualizationService.getHeatmapLayer(id);
        
        if (layer.isPresent()) {
            var layerData = layer.get();
            var response = new HeatmapLayerResponse(
                layerData.getId(),
                layerData.getName(),
                layerData.getDescription(),
                layerData.getHeatmapType().name(),
                layerData.getTileUrlTemplate(),
                layerData.getMinZoom(),
                layerData.getMaxZoom(),
                layerData.getTileSize(),
                layerData.getDataPointsCount(),
                layerData.getIntensityMin(),
                layerData.getIntensityMax(),
                layerData.getIntensityAvg(),
                layerData.getConfigurationId(),
                layerData.getGenerationParameters(),
                layerData.getFileSizeBytes(),
                layerData.getIsPublic(),
                layerData.getExpiresAt(),
                layerData.getCreatedAt()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get heatmap layers by type
     */
    @GetMapping("/layers/type/{type}")
    @Operation(summary = "Get heatmap layers by type", description = "Get heatmap layers by type")
    public ResponseEntity<List<HeatmapLayerResponse>> getHeatmapLayersByType(
            @PathVariable HeatmapType type) {
        
        var layers = visualizationService.getHeatmapLayersByType(type);
        
        List<HeatmapLayerResponse> responses = layers.stream()
            .map(layer -> new HeatmapLayerResponse(
                layer.getId(),
                layer.getName(),
                layer.getDescription(),
                layer.getHeatmapType().name(),
                layer.getTileUrlTemplate(),
                layer.getMinZoom(),
                layer.getMaxZoom(),
                layer.getTileSize(),
                layer.getDataPointsCount(),
                layer.getIntensityMin(),
                layer.getIntensityMax(),
                layer.getIntensityAvg(),
                layer.getConfigurationId(),
                layer.getGenerationParameters(),
                layer.getFileSizeBytes(),
                layer.getIsPublic(),
                layer.getExpiresAt(),
                layer.getCreatedAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get public heatmap layers
     */
    @GetMapping("/layers/public")
    @Operation(summary = "Get public heatmap layers", description = "Get all public heatmap layers")
    public ResponseEntity<List<HeatmapLayerResponse>> getPublicHeatmapLayers() {
        var layers = visualizationService.getPublicHeatmapLayers();
        
        List<HeatmapLayerResponse> responses = layers.stream()
            .map(layer -> new HeatmapLayerResponse(
                layer.getId(),
                layer.getName(),
                layer.getDescription(),
                layer.getHeatmapType().name(),
                layer.getTileUrlTemplate(),
                layer.getMinZoom(),
                layer.getMaxZoom(),
                layer.getTileSize(),
                layer.getDataPointsCount(),
                layer.getIntensityMin(),
                layer.getIntensityMax(),
                layer.getIntensityAvg(),
                layer.getConfigurationId(),
                layer.getGenerationParameters(),
                layer.getFileSizeBytes(),
                layer.getIsPublic(),
                layer.getExpiresAt(),
                layer.getCreatedAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get heatmap layers within bounds
     */
    @GetMapping("/layers/bounds")
    @Operation(summary = "Get heatmap layers in bounds", description = "Get heatmap layers within bounds")
    public ResponseEntity<List<HeatmapLayerResponse>> getHeatmapLayersInBounds(
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var layers = visualizationService.getHeatmapLayersInBounds(minLon, minLat, maxLon, maxLat);
        
        List<HeatmapLayerResponse> responses = layers.stream()
            .map(layer -> new HeatmapLayerResponse(
                layer.getId(),
                layer.getName(),
                layer.getDescription(),
                layer.getHeatmapType().name(),
                layer.getTileUrlTemplate(),
                layer.getMinZoom(),
                layer.getMaxZoom(),
                layer.getTileSize(),
                layer.getDataPointsCount(),
                layer.getIntensityMin(),
                layer.getIntensityMax(),
                layer.getIntensityAvg(),
                layer.getConfigurationId(),
                layer.getGenerationParameters(),
                layer.getFileSizeBytes(),
                layer.getIsPublic(),
                layer.getExpiresAt(),
                layer.getCreatedAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Generate heatmap tiles
     */
    @GetMapping("/tiles/{layerId}")
    @Operation(summary = "Generate heatmap tiles", description = "Generate heatmap tiles for visualization")
    public ResponseEntity<HeatmapTileDataResponse> generateHeatmapTiles(
            @PathVariable Long layerId,
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var tileRequest = new HeatmapVisualizationService.HeatmapTileRequest(
            layerId, minLon, minLat, maxLon, maxLat
        );
        
        var tileData = visualizationService.generateHeatmapTiles(tileRequest);
        
        var response = new HeatmapTileDataResponse(
            tileData.layerId(),
            tileData.minLon(), tileData.minLat(),
            tileData.maxLon(), tileData.maxLat(),
            tileData.points().stream()
                .map(point -> new HeatmapTilePointResponse(
                    point.longitude(),
                    point.latitude(),
                    point.intensity(),
                    point.weight(),
                    point.radius(),
                    point.category()
                ))
                .toList(),
            new HeatmapStatisticsResponse(
                tileData.statistics().pointCount(),
                tileData.statistics().avgIntensity(),
                tileData.statistics().minIntensity(),
                tileData.statistics().maxIntensity(),
                tileData.statistics().intensityStddev(),
                tileData.statistics().avgWeight(),
                tileData.statistics().avgRadius()
            ),
            tileData.heatmapType()
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Request/Response DTOs
    
    public record HeatmapDataRequest(
        double longitude,
        double latitude,
        HeatmapType heatmapType,
        double intensity,
        double weight,
        double radius,
        String category,
        String metadata,
        Long sourceId,
        String sourceType
    ) {}
    
    public record HeatmapDataResponse(
        Long id,
        double longitude,
        double latitude,
        String heatmapType,
        double intensity,
        double weight,
        double radius,
        String category,
        String metadata,
        Long sourceId,
        String sourceType,
        LocalDateTime createdAt
    ) {}
    
    public record HeatmapStatisticsResponse(
        Long pointCount,
        Double avgIntensity,
        Double minIntensity,
        Double maxIntensity,
        Double intensityStddev,
        Double avgWeight,
        Double avgRadius
    ) {}
    
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
    
    public record HeatmapLayerResponse(
        Long id,
        String name,
        String description,
        String heatmapType,
        String tileUrlTemplate,
        Integer minZoom,
        Integer maxZoom,
        Integer tileSize,
        Long dataPointsCount,
        Double intensityMin,
        Double intensityMax,
        Double intensityAvg,
        Long configurationId,
        String generationParameters,
        Long fileSizeBytes,
        Boolean isPublic,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
    ) {}
    
    public record HeatmapTileDataResponse(
        Long layerId,
        double minLon, double minLat, double maxLon, double maxLat,
        List<HeatmapTilePointResponse> points,
        HeatmapStatisticsResponse statistics,
        String heatmapType
    ) {}
    
    public record HeatmapTilePointResponse(
        double longitude,
        double latitude,
        double intensity,
        double weight,
        double radius,
        String category
    ) {}
}



