package com.relief.service.heatmap;

import com.relief.domain.heatmap.*;
import com.relief.repository.heatmap.HeatmapDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for heatmap data management and processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HeatmapDataService {

    private static final Logger log = LoggerFactory.getLogger(HeatmapDataService.class);

    private final HeatmapDataRepository heatmapDataRepository;
    private final GeometryFactory geometryFactory;
    
    /**
     * Add heatmap data point
     */
    @Transactional
    public HeatmapData addHeatmapData(HeatmapDataRequest request) {
        log.info("Adding heatmap data point for type: {}", request.heatmapType());
        
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        
        HeatmapData heatmapData = HeatmapData.builder()
            .location(location)
            .heatmapType(request.heatmapType())
            .intensity(request.intensity())
            .weight(request.weight())
            .radius(request.radius())
            .category(request.category())
            .metadata(request.metadata())
            .sourceId(request.sourceId())
            .sourceType(request.sourceType())
            .build();
            
        return heatmapDataRepository.save(heatmapData);
    }
    
    /**
     * Bulk add heatmap data points
     */
    @Transactional
    public List<HeatmapData> bulkAddHeatmapData(List<HeatmapDataRequest> requests) {
        log.info("Bulk adding {} heatmap data points", requests.size());
        
        List<HeatmapData> heatmapDataList = requests.stream()
            .map(request -> {
                Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
                return HeatmapData.builder()
                    .location(location)
                    .heatmapType(request.heatmapType())
                    .intensity(request.intensity())
                    .weight(request.weight())
                    .radius(request.radius())
                    .category(request.category())
                    .metadata(request.metadata())
                    .sourceId(request.sourceId())
                    .sourceType(request.sourceType())
                    .build();
            })
            .toList();
            
        return heatmapDataRepository.saveAll(heatmapDataList);
    }
    
    /**
     * Get heatmap data within bounds
     */
    public List<HeatmapData> getHeatmapDataInBounds(double minLon, double minLat, 
                                                   double maxLon, double maxLat) {
        return heatmapDataRepository.findWithinBounds(minLon, minLat, maxLon, maxLat);
    }
    
    /**
     * Get heatmap data by type
     */
    public List<HeatmapData> getHeatmapDataByType(HeatmapType heatmapType) {
        return heatmapDataRepository.findByHeatmapType(heatmapType);
    }
    
    /**
     * Get heatmap data by type and bounds
     */
    public List<HeatmapData> getHeatmapDataByTypeAndBounds(HeatmapType heatmapType,
                                                          double minLon, double minLat,
                                                          double maxLon, double maxLat) {
        return heatmapDataRepository.findByTypeAndBounds(
            heatmapType.name(), minLon, minLat, maxLon, maxLat
        );
    }
    
    /**
     * Get heatmap data by category
     */
    public List<HeatmapData> getHeatmapDataByCategory(String category) {
        return heatmapDataRepository.findByCategory(category);
    }
    
    /**
     * Get heatmap data by intensity range
     */
    public List<HeatmapData> getHeatmapDataByIntensityRange(double minIntensity, double maxIntensity) {
        return heatmapDataRepository.findByIntensityRange(minIntensity, maxIntensity);
    }
    
    /**
     * Get heatmap data by date range
     */
    public List<HeatmapData> getHeatmapDataByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return heatmapDataRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    /**
     * Get heatmap data by source
     */
    public List<HeatmapData> getHeatmapDataBySource(Long sourceId, String sourceType) {
        return heatmapDataRepository.findBySourceIdAndSourceType(sourceId, sourceType);
    }
    
    /**
     * Get heatmap statistics for a type
     */
    public HeatmapStatistics getHeatmapStatistics(HeatmapType heatmapType, 
                                                 LocalDateTime startDate, LocalDateTime endDate) {
        var stats = heatmapDataRepository.getStatisticsForType(
            heatmapType.name(), startDate, endDate
        );
        
        return new HeatmapStatistics(
            stats.getPointCount(),
            stats.getAvgIntensity(),
            stats.getMinIntensity(),
            stats.getMaxIntensity(),
            stats.getIntensityStddev(),
            stats.getAvgWeight(),
            stats.getAvgRadius()
        );
    }
    
    /**
     * Get heatmap statistics for bounds
     */
    public HeatmapStatistics getHeatmapStatisticsForBounds(double minLon, double minLat,
                                                          double maxLon, double maxLat,
                                                          LocalDateTime startDate, LocalDateTime endDate) {
        var stats = heatmapDataRepository.getStatisticsForBounds(
            minLon, minLat, maxLon, maxLat, startDate, endDate
        );
        
        return new HeatmapStatistics(
            stats.getPointCount(),
            stats.getAvgIntensity(),
            stats.getMinIntensity(),
            stats.getMaxIntensity(),
            stats.getIntensityStddev(),
            stats.getAvgWeight(),
            stats.getAvgRadius()
        );
    }
    
    /**
     * Get aggregated heatmap data for visualization
     */
    public List<HeatmapDataPoint> getAggregatedHeatmapData(HeatmapType heatmapType,
                                                          double minLon, double minLat,
                                                          double maxLon, double maxLat,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          int limit) {
        var data = heatmapDataRepository.getAggregatedData(
            heatmapType.name(), minLon, minLat, maxLon, maxLat, startDate, endDate, limit
        );
        
        return data.stream()
            .map(row -> new HeatmapDataPoint(
                (Double) row[0], // longitude
                (Double) row[1], // latitude
                (Double) row[2], // intensity
                (Double) row[3], // weight
                (Double) row[4], // radius
                (String) row[5]  // category
            ))
            .toList();
    }
    
    /**
     * Generate heatmap data from needs
     */
    @Transactional
    public List<HeatmapData> generateFromNeeds(List<NeedData> needs) {
        log.info("Generating heatmap data from {} needs", needs.size());
        
        List<HeatmapDataRequest> requests = needs.stream()
            .map(need -> new HeatmapDataRequest(
                need.longitude(),
                need.latitude(),
                HeatmapType.NEEDS_DENSITY,
                calculateNeedIntensity(need),
                calculateNeedWeight(need),
                calculateNeedRadius(need),
                need.category(),
                need.metadata(),
                need.id(),
                "NEED"
            ))
            .toList();
            
        return bulkAddHeatmapData(requests);
    }
    
    /**
     * Generate heatmap data from tasks
     */
    @Transactional
    public List<HeatmapData> generateFromTasks(List<TaskData> tasks) {
        log.info("Generating heatmap data from {} tasks", tasks.size());
        
        List<HeatmapDataRequest> requests = tasks.stream()
            .map(task -> new HeatmapDataRequest(
                task.longitude(),
                task.latitude(),
                HeatmapType.TASK_CONCENTRATION,
                calculateTaskIntensity(task),
                calculateTaskWeight(task),
                calculateTaskRadius(task),
                task.category(),
                task.metadata(),
                task.id(),
                "TASK"
            ))
            .toList();
            
        return bulkAddHeatmapData(requests);
    }
    
    /**
     * Generate heatmap data from resources
     */
    @Transactional
    public List<HeatmapData> generateFromResources(List<ResourceData> resources) {
        log.info("Generating heatmap data from {} resources", resources.size());
        
        List<HeatmapDataRequest> requests = resources.stream()
            .map(resource -> new HeatmapDataRequest(
                resource.longitude(),
                resource.latitude(),
                HeatmapType.RESOURCE_DISTRIBUTION,
                calculateResourceIntensity(resource),
                calculateResourceWeight(resource),
                calculateResourceRadius(resource),
                resource.category(),
                resource.metadata(),
                resource.id(),
                "RESOURCE"
            ))
            .toList();
            
        return bulkAddHeatmapData(requests);
    }
    
    /**
     * Calculate need intensity based on priority and urgency
     */
    private double calculateNeedIntensity(NeedData need) {
        double baseIntensity = 0.5;
        
        // Adjust based on priority
        switch (need.priority()) {
            case "CRITICAL" -> baseIntensity += 0.4;
            case "HIGH" -> baseIntensity += 0.3;
            case "MEDIUM" -> baseIntensity += 0.2;
            case "LOW" -> baseIntensity += 0.1;
        }
        
        // Adjust based on urgency
        if (need.isUrgent()) {
            baseIntensity += 0.2;
        }
        
        return Math.min(1.0, baseIntensity);
    }
    
    /**
     * Calculate need weight based on quantity and people affected
     */
    private double calculateNeedWeight(NeedData need) {
        double weight = 1.0;
        
        if (need.quantity() != null) {
            weight += Math.log(need.quantity() + 1) * 0.1;
        }
        
        if (need.peopleAffected() != null) {
            weight += Math.log(need.peopleAffected() + 1) * 0.2;
        }
        
        return weight;
    }
    
    /**
     * Calculate need radius based on area of effect
     */
    private double calculateNeedRadius(NeedData need) {
        return need.areaOfEffect() != null ? need.areaOfEffect() : 1000.0; // Default 1km
    }
    
    /**
     * Calculate task intensity based on status and priority
     */
    private double calculateTaskIntensity(TaskData task) {
        double baseIntensity = 0.3;
        
        // Adjust based on status
        switch (task.status()) {
            case "IN_PROGRESS" -> baseIntensity += 0.4;
            case "PENDING" -> baseIntensity += 0.3;
            case "COMPLETED" -> baseIntensity += 0.1;
            case "CANCELLED" -> baseIntensity += 0.0;
        }
        
        // Adjust based on priority
        switch (task.priority()) {
            case "CRITICAL" -> baseIntensity += 0.3;
            case "HIGH" -> baseIntensity += 0.2;
            case "MEDIUM" -> baseIntensity += 0.1;
            case "LOW" -> baseIntensity += 0.0;
        }
        
        return Math.min(1.0, baseIntensity);
    }
    
    /**
     * Calculate task weight based on complexity and resources
     */
    private double calculateTaskWeight(TaskData task) {
        double weight = 1.0;
        
        if (task.estimatedDuration() != null) {
            weight += task.estimatedDuration() * 0.01; // Duration in hours
        }
        
        if (task.requiredResources() != null) {
            weight += task.requiredResources().size() * 0.1;
        }
        
        return weight;
    }
    
    /**
     * Calculate task radius based on scope
     */
    private double calculateTaskRadius(TaskData task) {
        return task.scope() != null ? task.scope() : 500.0; // Default 500m
    }
    
    /**
     * Calculate resource intensity based on availability and type
     */
    private double calculateResourceIntensity(ResourceData resource) {
        double baseIntensity = 0.5;
        
        // Adjust based on availability
        if (resource.availability() != null) {
            baseIntensity = resource.availability() / 100.0; // Convert percentage to 0-1
        }
        
        // Adjust based on type
        switch (resource.type()) {
            case "EMERGENCY" -> baseIntensity += 0.3;
            case "MEDICAL" -> baseIntensity += 0.2;
            case "FOOD" -> baseIntensity += 0.1;
            case "SHELTER" -> baseIntensity += 0.2;
        }
        
        return Math.min(1.0, baseIntensity);
    }
    
    /**
     * Calculate resource weight based on quantity and capacity
     */
    private double calculateResourceWeight(ResourceData resource) {
        double weight = 1.0;
        
        if (resource.quantity() != null) {
            weight += Math.log(resource.quantity() + 1) * 0.1;
        }
        
        if (resource.capacity() != null) {
            weight += Math.log(resource.capacity() + 1) * 0.1;
        }
        
        return weight;
    }
    
    /**
     * Calculate resource radius based on coverage area
     */
    private double calculateResourceRadius(ResourceData resource) {
        return resource.coverageArea() != null ? resource.coverageArea() : 2000.0; // Default 2km
    }
    
    // Data classes
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
    
    public record HeatmapDataPoint(
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
    
    public record NeedData(
        Long id,
        double longitude,
        double latitude,
        String priority,
        boolean isUrgent,
        Integer quantity,
        Integer peopleAffected,
        Double areaOfEffect,
        String category,
        String metadata
    ) {}
    
    public record TaskData(
        Long id,
        double longitude,
        double latitude,
        String status,
        String priority,
        Integer estimatedDuration,
        List<String> requiredResources,
        Double scope,
        String category,
        String metadata
    ) {}
    
    public record ResourceData(
        Long id,
        double longitude,
        double latitude,
        String type,
        Integer availability,
        Integer quantity,
        Integer capacity,
        Double coverageArea,
        String category,
        String metadata
    ) {}
}



