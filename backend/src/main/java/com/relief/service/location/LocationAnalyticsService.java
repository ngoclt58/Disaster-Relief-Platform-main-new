package com.relief.service.location;

import com.relief.domain.location.*;
import com.relief.repository.location.LocationHistoryRepository;
import com.relief.repository.location.LocationPatternRepository;
import com.relief.repository.location.LocationOptimizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for location analytics and pattern detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationAnalyticsService {
    
    private final LocationHistoryRepository locationHistoryRepository;
    private final LocationPatternRepository locationPatternRepository;
    private final LocationOptimizationRepository locationOptimizationRepository;
    private final GeometryFactory geometryFactory;
    
    /**
     * Record location history entry
     */
    @Transactional
    public LocationHistory recordLocationHistory(LocationHistoryRequest request) {
        log.info("Recording location history for entity: {} {}", request.entityType(), request.entityId());
        
        Point position = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        
        // Calculate distance from previous location
        Double distanceFromPrevious = calculateDistanceFromPrevious(
            request.entityType(), request.entityId(), request.latitude(), request.longitude()
        );
        
        // Determine if stationary
        Boolean isStationary = determineIfStationary(request.speed(), request.durationSeconds());
        
        // Determine if significant
        Boolean isSignificant = determineIfSignificant(request, distanceFromPrevious);
        
        LocationHistory locationHistory = LocationHistory.builder()
            .entityType(request.entityType())
            .entityId(request.entityId())
            .entityName(request.entityName())
            .position(position)
            .latitude(request.latitude())
            .longitude(request.longitude())
            .altitude(request.altitude())
            .heading(request.heading())
            .speed(request.speed())
            .accuracy(request.accuracy())
            .activityType(request.activityType())
            .activityDescription(request.activityDescription())
            .timestamp(request.timestamp())
            .durationSeconds(request.durationSeconds())
            .distanceFromPrevious(distanceFromPrevious)
            .isStationary(isStationary)
            .isSignificant(isSignificant)
            .locationContext(request.locationContext())
            .environmentalConditions(request.environmentalConditions())
            .metadata(request.metadata())
            .build();
            
        LocationHistory savedHistory = locationHistoryRepository.save(locationHistory);
        
        // Trigger pattern analysis
        analyzePatternsForEntity(request.entityType(), request.entityId());
        
        return savedHistory;
    }
    
    /**
     * Analyze patterns for an entity
     */
    @Transactional
    public void analyzePatternsForEntity(String entityType, Long entityId) {
        log.info("Analyzing patterns for entity: {} {}", entityType, entityId);
        
        // Get recent location history for the entity
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7); // Analyze last 7 days
        
        List<LocationHistory> locations = locationHistoryRepository.findByEntityAndTimestampBetween(
            entityType, entityId, startTime, endTime
        );
        
        if (locations.size() < 10) {
            log.debug("Not enough location data for pattern analysis: {}", locations.size());
            return;
        }
        
        // Detect movement patterns
        List<LocationPattern> patterns = detectMovementPatterns(locations);
        
        // Save patterns
        for (LocationPattern pattern : patterns) {
            locationPatternRepository.save(pattern);
        }
        
        // Generate optimizations
        generateOptimizations(patterns);
    }
    
    /**
     * Detect movement patterns from location history
     */
    private List<LocationPattern> detectMovementPatterns(List<LocationHistory> locations) {
        List<LocationPattern> patterns = new ArrayList<>();
        
        // Sort locations by timestamp
        locations.sort(Comparator.comparing(LocationHistory::getTimestamp));
        
        // Detect different types of patterns
        patterns.addAll(detectLinearMovementPatterns(locations));
        patterns.addAll(detectCircularMovementPatterns(locations));
        patterns.addAll(detectStationaryPatterns(locations));
        patterns.addAll(detectRoutePatterns(locations));
        patterns.addAll(detectSearchPatterns(locations));
        patterns.addAll(detectAnomalyPatterns(locations));
        
        return patterns;
    }
    
    /**
     * Detect linear movement patterns
     */
    private List<LocationPattern> detectLinearMovementPatterns(List<LocationHistory> locations) {
        List<LocationPattern> patterns = new ArrayList<>();
        
        // Group consecutive locations with similar heading
        List<List<LocationHistory>> linearSegments = groupLinearSegments(locations);
        
        for (List<LocationHistory> segment : linearSegments) {
            if (segment.size() >= 5) { // Minimum 5 points for a pattern
                LocationPattern pattern = createLinearPattern(segment);
                patterns.add(pattern);
            }
        }
        
        return patterns;
    }
    
    /**
     * Detect circular movement patterns
     */
    private List<LocationPattern> detectCircularMovementPatterns(List<LocationHistory> locations) {
        List<LocationPattern> patterns = new ArrayList<>();
        
        // Look for circular movements using centroid analysis
        List<List<LocationHistory>> circularSegments = groupCircularSegments(locations);
        
        for (List<LocationHistory> segment : circularSegments) {
            if (segment.size() >= 8) { // Minimum 8 points for circular pattern
                LocationPattern pattern = createCircularPattern(segment);
                patterns.add(pattern);
            }
        }
        
        return patterns;
    }
    
    /**
     * Detect stationary patterns
     */
    private List<LocationPattern> detectStationaryPatterns(List<LocationHistory> locations) {
        List<LocationPattern> patterns = new ArrayList<>();
        
        // Group stationary locations
        List<List<LocationHistory>> stationaryClusters = groupStationaryClusters(locations);
        
        for (List<LocationHistory> cluster : stationaryClusters) {
            if (cluster.size() >= 3) { // Minimum 3 points for stationary pattern
                LocationPattern pattern = createStationaryPattern(cluster);
                patterns.add(pattern);
            }
        }
        
        return patterns;
    }
    
    /**
     * Detect route patterns
     */
    private List<LocationPattern> detectRoutePatterns(List<LocationHistory> locations) {
        List<LocationPattern> patterns = new ArrayList<>();
        
        // Look for repeated routes
        Map<String, List<LocationHistory>> routeGroups = groupByRoute(locations);
        
        for (Map.Entry<String, List<LocationHistory>> entry : routeGroups.entrySet()) {
            List<LocationHistory> route = entry.getValue();
            if (route.size() >= 10) { // Minimum 10 points for route pattern
                LocationPattern pattern = createRoutePattern(route, entry.getKey());
                patterns.add(pattern);
            }
        }
        
        return patterns;
    }
    
    /**
     * Detect search patterns
     */
    private List<LocationPattern> detectSearchPatterns(List<LocationHistory> locations) {
        List<LocationPattern> patterns = new ArrayList<>();
        
        // Look for grid, spiral, or systematic search patterns
        List<List<LocationHistory>> searchSegments = groupSearchSegments(locations);
        
        for (List<LocationHistory> segment : searchSegments) {
            if (segment.size() >= 6) { // Minimum 6 points for search pattern
                LocationPattern pattern = createSearchPattern(segment);
                patterns.add(pattern);
            }
        }
        
        return patterns;
    }
    
    /**
     * Detect anomaly patterns
     */
    private List<LocationPattern> detectAnomalyPatterns(List<LocationHistory> locations) {
        List<LocationPattern> patterns = new ArrayList<>();
        
        // Look for unusual movements or deviations
        List<LocationHistory> anomalies = detectAnomalies(locations);
        
        if (!anomalies.isEmpty()) {
            LocationPattern pattern = createAnomalyPattern(anomalies);
            patterns.add(pattern);
        }
        
        return patterns;
    }
    
    /**
     * Generate optimizations based on patterns
     */
    @Transactional
    public void generateOptimizations(List<LocationPattern> patterns) {
        for (LocationPattern pattern : patterns) {
            List<LocationOptimization> optimizations = generateOptimizationsForPattern(pattern);
            
            for (LocationOptimization optimization : optimizations) {
                locationOptimizationRepository.save(optimization);
            }
        }
    }
    
    /**
     * Generate optimizations for a specific pattern
     */
    private List<LocationOptimization> generateOptimizationsForPattern(LocationPattern pattern) {
        List<LocationOptimization> optimizations = new ArrayList<>();
        
        switch (pattern.getPatternType()) {
            case LINEAR_MOVEMENT:
                optimizations.addAll(generateRouteOptimizations(pattern));
                break;
            case STATIONARY_CLUSTER:
                optimizations.addAll(generateStationaryOptimizations(pattern));
                break;
            case OPTIMIZED_ROUTE:
                optimizations.addAll(generateRouteOptimizations(pattern));
                break;
            case SEARCH_GRID:
                optimizations.addAll(generateSearchOptimizations(pattern));
                break;
            default:
                optimizations.addAll(generateGenericOptimizations(pattern));
        }
        
        return optimizations;
    }
    
    /**
     * Generate route optimizations
     */
    private List<LocationOptimization> generateRouteOptimizations(LocationPattern pattern) {
        List<LocationOptimization> optimizations = new ArrayList<>();
        
        // Calculate current efficiency
        double currentEfficiency = calculateRouteEfficiency(pattern);
        
        if (currentEfficiency < 0.8) { // If efficiency is below 80%
            LocationOptimization optimization = LocationOptimization.builder()
                .locationPattern(pattern)
                .optimizationType(OptimizationType.ROUTE_OPTIMIZATION)
                .optimizationName("Route Optimization for " + pattern.getPatternName())
                .description("Optimize the route to improve efficiency and reduce travel time")
                .currentEfficiency(currentEfficiency)
                .projectedEfficiency(0.9) // Project 90% efficiency
                .timeSavingsSeconds(calculateTimeSavings(pattern))
                .distanceSavingsMeters(calculateDistanceSavings(pattern))
                .priority(OptimizationPriority.HIGH)
                .status(OptimizationStatus.PENDING)
                .implementationDifficulty(ImplementationDifficulty.MEDIUM)
                .riskLevel(RiskLevel.LOW)
                .isImplemented(false)
                .build();
                
            optimizations.add(optimization);
        }
        
        return optimizations;
    }
    
    /**
     * Generate stationary optimizations
     */
    private List<LocationOptimization> generateStationaryOptimizations(LocationPattern pattern) {
        List<LocationOptimization> optimizations = new ArrayList<>();
        
        // Analyze stationary time and suggest optimizations
        long totalStationaryTime = pattern.getDurationSeconds();
        
        if (totalStationaryTime > 3600) { // More than 1 hour stationary
            LocationOptimization optimization = LocationOptimization.builder()
                .locationPattern(pattern)
                .optimizationType(OptimizationType.WORKFLOW_OPTIMIZATION)
                .optimizationName("Reduce Stationary Time")
                .description("Optimize workflow to reduce unnecessary stationary time")
                .currentEfficiency(0.6)
                .projectedEfficiency(0.8)
                .timeSavingsSeconds(totalStationaryTime / 2)
                .priority(OptimizationPriority.MEDIUM)
                .status(OptimizationStatus.PENDING)
                .implementationDifficulty(ImplementationDifficulty.EASY)
                .riskLevel(RiskLevel.LOW)
                .isImplemented(false)
                .build();
                
            optimizations.add(optimization);
        }
        
        return optimizations;
    }
    
    /**
     * Generate search optimizations
     */
    private List<LocationOptimization> generateSearchOptimizations(LocationPattern pattern) {
        List<LocationOptimization> optimizations = new ArrayList<>();
        
        // Analyze search pattern efficiency
        double searchEfficiency = calculateSearchEfficiency(pattern);
        
        if (searchEfficiency < 0.7) {
            LocationOptimization optimization = LocationOptimization.builder()
                .locationPattern(pattern)
                .optimizationType(OptimizationType.SEARCH_PATTERN_OPTIMIZATION)
                .optimizationName("Optimize Search Pattern")
                .description("Improve search pattern to increase coverage efficiency")
                .currentEfficiency(searchEfficiency)
                .projectedEfficiency(0.85)
                .priority(OptimizationPriority.HIGH)
                .status(OptimizationStatus.PENDING)
                .implementationDifficulty(ImplementationDifficulty.MEDIUM)
                .riskLevel(RiskLevel.MEDIUM)
                .isImplemented(false)
                .build();
                
            optimizations.add(optimization);
        }
        
        return optimizations;
    }
    
    /**
     * Generate generic optimizations
     */
    private List<LocationOptimization> generateGenericOptimizations(LocationPattern pattern) {
        List<LocationOptimization> optimizations = new ArrayList<>();
        
        // Generic efficiency optimization
        LocationOptimization optimization = LocationOptimization.builder()
            .locationPattern(pattern)
            .optimizationType(OptimizationType.PERFORMANCE_IMPROVEMENT)
            .optimizationName("General Performance Improvement")
            .description("General optimization suggestions for this movement pattern")
            .currentEfficiency(0.7)
            .projectedEfficiency(0.85)
            .priority(OptimizationPriority.MEDIUM)
            .status(OptimizationStatus.PENDING)
            .implementationDifficulty(ImplementationDifficulty.MEDIUM)
            .riskLevel(RiskLevel.LOW)
            .isImplemented(false)
            .build();
            
        optimizations.add(optimization);
        
        return optimizations;
    }
    
    // Helper methods for pattern detection and analysis
    
    private Double calculateDistanceFromPrevious(String entityType, Long entityId, double latitude, double longitude) {
        // Get the last location for this entity
        List<LocationHistory> recentLocations = locationHistoryRepository.findByEntityTypeAndEntityId(entityType, entityId);
        
        if (recentLocations.isEmpty()) {
            return 0.0;
        }
        
        LocationHistory lastLocation = recentLocations.get(recentLocations.size() - 1);
        return calculateDistance(
            lastLocation.getLatitude(), lastLocation.getLongitude(),
            latitude, longitude
        );
    }
    
    private Boolean determineIfStationary(Double speed, Integer durationSeconds) {
        return speed != null && speed < 0.5 && durationSeconds != null && durationSeconds > 300; // 5 minutes
    }
    
    private Boolean determineIfSignificant(LocationHistoryRequest request, Double distanceFromPrevious) {
        return distanceFromPrevious != null && distanceFromPrevious > 100; // More than 100 meters
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for calculating distance between two points
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Convert to meters
    }
    
    private double calculateRouteEfficiency(LocationPattern pattern) {
        // Simple efficiency calculation based on distance and time
        double totalDistance = pattern.getDistanceMeters();
        long totalTime = pattern.getDurationSeconds();
        double averageSpeed = pattern.getAverageSpeed();
        
        // Calculate efficiency as a ratio of actual speed to optimal speed
        double optimalSpeed = 10.0; // Assume 10 m/s is optimal
        return Math.min(averageSpeed / optimalSpeed, 1.0);
    }
    
    private long calculateTimeSavings(LocationPattern pattern) {
        // Estimate 20% time savings
        return (long) (pattern.getDurationSeconds() * 0.2);
    }
    
    private double calculateDistanceSavings(LocationPattern pattern) {
        // Estimate 15% distance savings
        return pattern.getDistanceMeters() * 0.15;
    }
    
    private double calculateSearchEfficiency(LocationPattern pattern) {
        // Calculate search efficiency based on coverage area
        double totalDistance = pattern.getDistanceMeters();
        double coverageArea = Math.PI * Math.pow(totalDistance / (2 * Math.PI), 2);
        double optimalCoverage = totalDistance * 100; // Assume 100m width
        
        return Math.min(coverageArea / optimalCoverage, 1.0);
    }
    
    // Pattern creation methods (simplified implementations)
    private List<List<LocationHistory>> groupLinearSegments(List<LocationHistory> locations) {
        // Simplified linear segment grouping
        return List.of(locations);
    }
    
    private List<List<LocationHistory>> groupCircularSegments(List<LocationHistory> locations) {
        // Simplified circular segment grouping
        return List.of();
    }
    
    private List<List<LocationHistory>> groupStationaryClusters(List<LocationHistory> locations) {
        // Simplified stationary cluster grouping
        return locations.stream()
            .filter(loc -> loc.getIsStationary() != null && loc.getIsStationary())
            .map(List::of)
            .collect(Collectors.toList());
    }
    
    private Map<String, List<LocationHistory>> groupByRoute(List<LocationHistory> locations) {
        // Simplified route grouping
        Map<String, List<LocationHistory>> routes = new HashMap<>();
        routes.put("route_1", locations);
        return routes;
    }
    
    private List<List<LocationHistory>> groupSearchSegments(List<LocationHistory> locations) {
        // Simplified search segment grouping
        return List.of();
    }
    
    private List<LocationHistory> detectAnomalies(List<LocationHistory> locations) {
        // Simplified anomaly detection
        return locations.stream()
            .filter(loc -> loc.getSpeed() != null && loc.getSpeed() > 50) // Speed > 50 m/s is anomalous
            .collect(Collectors.toList());
    }
    
    private LocationPattern createLinearPattern(List<LocationHistory> segment) {
        return createBasePattern(segment, PatternType.LINEAR_MOVEMENT, "Linear Movement Pattern");
    }
    
    private LocationPattern createCircularPattern(List<LocationHistory> segment) {
        return createBasePattern(segment, PatternType.CIRCULAR_MOVEMENT, "Circular Movement Pattern");
    }
    
    private LocationPattern createStationaryPattern(List<LocationHistory> cluster) {
        return createBasePattern(cluster, PatternType.STATIONARY_CLUSTER, "Stationary Cluster Pattern");
    }
    
    private LocationPattern createRoutePattern(List<LocationHistory> route, String routeName) {
        return createBasePattern(route, PatternType.COMMUTE_ROUTE, "Route Pattern: " + routeName);
    }
    
    private LocationPattern createSearchPattern(List<LocationHistory> segment) {
        return createBasePattern(segment, PatternType.SEARCH_GRID, "Search Grid Pattern");
    }
    
    private LocationPattern createAnomalyPattern(List<LocationHistory> anomalies) {
        return createBasePattern(anomalies, PatternType.ANOMALY_DETECTED, "Anomaly Pattern");
    }
    
    private LocationPattern createBasePattern(List<LocationHistory> locations, PatternType patternType, String patternName) {
        LocationHistory first = locations.get(0);
        LocationHistory last = locations.get(locations.size() - 1);
        
        double totalDistance = locations.stream()
            .mapToDouble(loc -> loc.getDistanceFromPrevious() != null ? loc.getDistanceFromPrevious() : 0.0)
            .sum();
        
        double averageSpeed = locations.stream()
            .mapToDouble(loc -> loc.getSpeed() != null ? loc.getSpeed() : 0.0)
            .average()
            .orElse(0.0);
        
        double maxSpeed = locations.stream()
            .mapToDouble(loc -> loc.getSpeed() != null ? loc.getSpeed() : 0.0)
            .max()
            .orElse(0.0);
        
        long durationSeconds = ChronoUnit.SECONDS.between(first.getTimestamp(), last.getTimestamp());
        
        return LocationPattern.builder()
            .entityType(first.getEntityType())
            .entityId(first.getEntityId())
            .patternType(patternType)
            .patternName(patternName)
            .patternDescription("Detected " + patternType.name().toLowerCase() + " pattern")
            .startTime(first.getTimestamp())
            .endTime(last.getTimestamp())
            .durationSeconds(durationSeconds)
            .distanceMeters(totalDistance)
            .averageSpeed(averageSpeed)
            .maxSpeed(maxSpeed)
            .confidenceScore(0.8) // Default confidence
            .frequency(1)
            .isRecurring(false)
            .isOptimal(false)
            .build();
    }
    
    // Data classes
    public record LocationHistoryRequest(
        String entityType,
        Long entityId,
        String entityName,
        double latitude,
        double longitude,
        Double altitude,
        Double heading,
        Double speed,
        Double accuracy,
        ActivityType activityType,
        String activityDescription,
        LocalDateTime timestamp,
        Integer durationSeconds,
        String locationContext,
        String environmentalConditions,
        String metadata
    ) {}
}



