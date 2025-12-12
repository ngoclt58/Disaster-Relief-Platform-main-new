package com.relief.service.terrain;

import com.relief.domain.terrain.ElevationPoint;
import com.relief.repository.terrain.ElevationPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for terrain-aware routing algorithms
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TerrainRoutingService {
    
    private final ElevationPointRepository elevationPointRepository;
    private final ElevationService elevationService;
    private final GeometryFactory geometryFactory;
    
    /**
     * Calculate terrain-aware route between two points
     */
    public TerrainRoute calculateTerrainRoute(double startLon, double startLat, 
                                            double endLon, double endLat,
                                            TerrainRoutingOptions options) {
        log.info("Calculating terrain route from ({}, {}) to ({}, {})", 
                startLon, startLat, endLon, endLat);
        
        // Get elevation data along the route
        List<ElevationPoint> elevationPoints = getElevationPointsAlongRoute(
            startLon, startLat, endLon, endLat, options.searchRadius()
        );
        
        if (elevationPoints.isEmpty()) {
            // Fallback to straight-line distance if no elevation data
            return createStraightLineRoute(startLon, startLat, endLon, endLat);
        }
        
        // Calculate route segments with terrain considerations
        List<RouteSegment> segments = calculateRouteSegments(
            startLon, startLat, endLon, endLat, elevationPoints, options
        );
        
        // Calculate total metrics
        double totalDistance = segments.stream().mapToDouble(RouteSegment::distance).sum();
        double totalElevationGain = segments.stream().mapToDouble(RouteSegment::elevationGain).sum();
        double totalElevationLoss = segments.stream().mapToDouble(RouteSegment::elevationLoss).sum();
        double maxSlope = segments.stream().mapToDouble(RouteSegment::slope).max().orElse(0);
        double avgSlope = segments.stream().mapToDouble(RouteSegment::slope).average().orElse(0);
        
        // Calculate terrain difficulty score
        double difficultyScore = calculateDifficultyScore(segments, options);
        
        // Calculate accessibility score
        double accessibilityScore = calculateRouteAccessibility(segments, options);
        
        return TerrainRoute.builder()
            .startPoint(new Coordinate(startLon, startLat))
            .endPoint(new Coordinate(endLon, endLat))
            .segments(segments)
            .totalDistance(totalDistance)
            .totalElevationGain(totalElevationGain)
            .totalElevationLoss(totalElevationLoss)
            .maxSlope(maxSlope)
            .avgSlope(avgSlope)
            .difficultyScore(difficultyScore)
            .accessibilityScore(accessibilityScore)
            .isAccessible(accessibilityScore >= options.minAccessibilityScore())
            .build();
    }
    
    /**
     * Find alternative routes considering terrain constraints
     */
    public List<TerrainRoute> findAlternativeRoutes(double startLon, double startLat,
                                                  double endLon, double endLat,
                                                  TerrainRoutingOptions options) {
        List<TerrainRoute> routes = new ArrayList<>();
        
        // Calculate direct route
        TerrainRoute directRoute = calculateTerrainRoute(startLon, startLat, endLon, endLat, options);
        routes.add(directRoute);
        
        // Find waypoints to create alternative routes
        List<Coordinate> waypoints = findWaypoints(startLon, startLat, endLon, endLat, options);
        
        for (Coordinate waypoint : waypoints) {
            TerrainRoute route1 = calculateTerrainRoute(startLon, startLat, 
                waypoint.x, waypoint.y, options);
            TerrainRoute route2 = calculateTerrainRoute(waypoint.x, waypoint.y, 
                endLon, endLat, options);
            
            if (route1.isAccessible() && route2.isAccessible()) {
                TerrainRoute combinedRoute = combineRoutes(route1, route2);
                routes.add(combinedRoute);
            }
        }
        
        // Sort by accessibility score and distance
        return routes.stream()
            .sorted((r1, r2) -> {
                int accessibilityCompare = Double.compare(r2.accessibilityScore(), r1.accessibilityScore());
                if (accessibilityCompare != 0) return accessibilityCompare;
                return Double.compare(r1.totalDistance(), r2.totalDistance());
            })
            .limit(options.maxAlternativeRoutes())
            .collect(Collectors.toList());
    }
    
    /**
     * Get elevation points along a route
     */
    private List<ElevationPoint> getElevationPointsAlongRoute(double startLon, double startLat,
                                                            double endLon, double endLat,
                                                            double searchRadius) {
        // Create a buffer around the route line
        LineString routeLine = geometryFactory.createLineString(new Coordinate[]{
            new Coordinate(startLon, startLat),
            new Coordinate(endLon, endLat)
        });
        
        // Expand the line to create a search area
        Geometry searchArea = routeLine.buffer(searchRadius / 111000.0); // Convert meters to degrees
        
        Envelope envelope = searchArea.getEnvelopeInternal();
        
        return elevationPointRepository.findWithinBounds(
            envelope.getMinX(), envelope.getMinY(),
            envelope.getMaxX(), envelope.getMaxY(),
            (startLon + endLon) / 2, (startLat + endLat) / 2
        );
    }
    
    /**
     * Calculate route segments with terrain considerations
     */
    private List<RouteSegment> calculateRouteSegments(double startLon, double startLat,
                                                    double endLon, double endLat,
                                                    List<ElevationPoint> elevationPoints,
                                                    TerrainRoutingOptions options) {
        List<RouteSegment> segments = new ArrayList<>();
        
        // Sort elevation points by distance from start
        elevationPoints.sort((p1, p2) -> {
            double dist1 = calculateDistance(startLon, startLat, 
                p1.getLocation().getX(), p1.getLocation().getY());
            double dist2 = calculateDistance(startLon, startLat, 
                p2.getLocation().getX(), p2.getLocation().getY());
            return Double.compare(dist1, dist2);
        });
        
        // Create segments between consecutive points
        double prevLon = startLon;
        double prevLat = startLat;
        double prevElevation = elevationService.getElevationAtPoint(startLon, startLat).orElse(0.0);
        
        for (ElevationPoint point : elevationPoints) {
            double currLon = point.getLocation().getX();
            double currLat = point.getLocation().getY();
            double currElevation = point.getElevation();
            
            double distance = calculateDistance(prevLon, prevLat, currLon, currLat);
            double slope = elevationService.calculateSlope(prevLon, prevLat, prevElevation,
                currLon, currLat, currElevation);
            double elevationGain = Math.max(0, currElevation - prevElevation);
            double elevationLoss = Math.max(0, prevElevation - currElevation);
            
            segments.add(new RouteSegment(
                new Coordinate(prevLon, prevLat),
                new Coordinate(currLon, currLat),
                distance, slope, elevationGain, elevationLoss
            ));
            
            prevLon = currLon;
            prevLat = currLat;
            prevElevation = currElevation;
        }
        
        // Add final segment to end point
        double finalElevation = elevationService.getElevationAtPoint(endLon, endLat).orElse(0.0);
        double finalDistance = calculateDistance(prevLon, prevLat, endLon, endLat);
        double finalSlope = elevationService.calculateSlope(prevLon, prevLat, prevElevation,
            endLon, endLat, finalElevation);
        double finalElevationGain = Math.max(0, finalElevation - prevElevation);
        double finalElevationLoss = Math.max(0, prevElevation - finalElevation);
        
        segments.add(new RouteSegment(
            new Coordinate(prevLon, prevLat),
            new Coordinate(endLon, endLat),
            finalDistance, finalSlope, finalElevationGain, finalElevationLoss
        ));
        
        return segments;
    }
    
    /**
     * Find waypoints for alternative routes
     */
    private List<Coordinate> findWaypoints(double startLon, double startLat,
                                         double endLon, double endLat,
                                         TerrainRoutingOptions options) {
        List<Coordinate> waypoints = new ArrayList<>();
        
        // Calculate midpoint
        double midLon = (startLon + endLon) / 2;
        double midLat = (startLat + endLat) / 2;
        
        // Add perpendicular waypoints
        double bearing = calculateBearing(startLon, startLat, endLon, endLat);
        double perpBearing1 = (bearing + 90) % 360;
        double perpBearing2 = (bearing - 90 + 360) % 360;
        
        double waypointOffsetDistance = options.waypointOffsetDistance();
        
        waypoints.add(calculateDestination(midLon, midLat, perpBearing1, waypointOffsetDistance));
        waypoints.add(calculateDestination(midLon, midLat, perpBearing2, waypointOffsetDistance));
        
        return waypoints;
    }
    
    /**
     * Calculate difficulty score for a route
     */
    private double calculateDifficultyScore(List<RouteSegment> segments, TerrainRoutingOptions options) {
        double totalDifficulty = 0;
        double totalDistance = 0;
        
        for (RouteSegment segment : segments) {
            double segmentDifficulty = 1.0; // Base difficulty
            
            // Increase difficulty for steep slopes
            if (Math.abs(segment.slope()) > options.maxSlope()) {
                segmentDifficulty += 2.0;
            } else if (Math.abs(segment.slope()) > options.maxSlope() / 2) {
                segmentDifficulty += 1.0;
            }
            
            // Increase difficulty for elevation changes
            if (segment.elevationGain() > 100) {
                segmentDifficulty += 1.0;
            }
            if (segment.elevationLoss() > 100) {
                segmentDifficulty += 0.5;
            }
            
            totalDifficulty += segmentDifficulty * segment.distance();
            totalDistance += segment.distance();
        }
        
        return totalDistance > 0 ? totalDifficulty / totalDistance : 0;
    }
    
    /**
     * Calculate accessibility score for a route
     */
    private double calculateRouteAccessibility(List<RouteSegment> segments, TerrainRoutingOptions options) {
        long accessibleSegments = segments.stream()
            .filter(segment -> Math.abs(segment.slope()) <= options.maxSlope())
            .count();
        
        return segments.isEmpty() ? 0 : (double) accessibleSegments / segments.size();
    }
    
    /**
     * Create a straight-line route (fallback)
     */
    private TerrainRoute createStraightLineRoute(double startLon, double startLat,
                                               double endLon, double endLat) {
        double distance = calculateDistance(startLon, startLat, endLon, endLat);
        
        RouteSegment segment = new RouteSegment(
            new Coordinate(startLon, startLat),
            new Coordinate(endLon, endLat),
            distance, 0, 0, 0
        );
        
        return TerrainRoute.builder()
            .startPoint(new Coordinate(startLon, startLat))
            .endPoint(new Coordinate(endLon, endLat))
            .segments(List.of(segment))
            .totalDistance(distance)
            .totalElevationGain(0)
            .totalElevationLoss(0)
            .maxSlope(0)
            .avgSlope(0)
            .difficultyScore(1.0)
            .accessibilityScore(1.0)
            .isAccessible(true)
            .build();
    }
    
    /**
     * Combine two routes
     */
    private TerrainRoute combineRoutes(TerrainRoute route1, TerrainRoute route2) {
        List<RouteSegment> combinedSegments = new ArrayList<>(route1.segments());
        combinedSegments.addAll(route2.segments());
        
        double totalDistance = route1.totalDistance() + route2.totalDistance();
        double totalElevationGain = route1.totalElevationGain() + route2.totalElevationGain();
        double totalElevationLoss = route1.totalElevationLoss() + route2.totalElevationLoss();
        double maxSlope = Math.max(route1.maxSlope(), route2.maxSlope());
        double avgSlope = (route1.avgSlope() * route1.totalDistance() + 
                          route2.avgSlope() * route2.totalDistance()) / totalDistance;
        
        return TerrainRoute.builder()
            .startPoint(route1.startPoint())
            .endPoint(route2.endPoint())
            .segments(combinedSegments)
            .totalDistance(totalDistance)
            .totalElevationGain(totalElevationGain)
            .totalElevationLoss(totalElevationLoss)
            .maxSlope(maxSlope)
            .avgSlope(avgSlope)
            .difficultyScore((route1.difficultyScore() + route2.difficultyScore()) / 2)
            .accessibilityScore(Math.min(route1.accessibilityScore(), route2.accessibilityScore()))
            .isAccessible(route1.isAccessible() && route2.isAccessible())
            .build();
    }
    
    /**
     * Calculate distance between two points
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Calculate bearing between two points
     */
    private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double y = Math.sin(deltaLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - 
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLon);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }
    
    /**
     * Calculate destination point given bearing and distance
     */
    private Coordinate calculateDestination(double lat, double lon, double bearing, double distance) {
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double bearingRad = Math.toRadians(bearing);
        double distanceRad = distance / 6371000; // Convert to radians
        
        double newLat = Math.asin(Math.sin(latRad) * Math.cos(distanceRad) +
                                 Math.cos(latRad) * Math.sin(distanceRad) * Math.cos(bearingRad));
        double newLon = lonRad + Math.atan2(Math.sin(bearingRad) * Math.sin(distanceRad) * Math.cos(latRad),
                                           Math.cos(distanceRad) - Math.sin(latRad) * Math.sin(newLat));
        
        return new Coordinate(Math.toDegrees(newLon), Math.toDegrees(newLat));
    }
    
    /**
     * Data classes for terrain routing
     */
    @lombok.Builder
    public record TerrainRoute(
        Coordinate startPoint,
        Coordinate endPoint,
        List<RouteSegment> segments,
        double totalDistance,
        double totalElevationGain,
        double totalElevationLoss,
        double maxSlope,
        double avgSlope,
        double difficultyScore,
        double accessibilityScore,
        boolean isAccessible
    ) {
        public static TerrainRouteBuilder builder() {
            return new TerrainRouteBuilder();
        }
    }
    
    @lombok.Builder
    public record RouteSegment(
        Coordinate startPoint,
        Coordinate endPoint,
        double distance,
        double slope,
        double elevationGain,
        double elevationLoss
    ) {
        public static RouteSegmentBuilder builder() {
            return new RouteSegmentBuilder();
        }
    }
    
    public record TerrainRoutingOptions(
        double searchRadius,
        double maxSlope,
        double minAccessibilityScore,
        double waypointOffsetDistance,
        int maxAlternativeRoutes
    ) {
        public static TerrainRoutingOptions defaultOptions() {
            return new TerrainRoutingOptions(1000, 15, 0.7, 500, 3);
        }
    }
}



