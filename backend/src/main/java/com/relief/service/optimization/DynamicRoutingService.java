package com.relief.service.optimization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for AI-powered optimal delivery routes
 * Considers traffic, weather, and priority in route optimization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicRoutingService {

    private static final Logger log = LoggerFactory.getLogger(DynamicRoutingService.class);

    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    private final Map<String, RouteOptimization> optimizations = new ConcurrentHashMap<>();

    /**
     * Create an optimized route
     */
    public Route createOptimizedRoute(
            String originLat,
            String originLon,
            List<String> destinations,
            Map<String, Object> constraints) {

        RouteOptimization optimization = optimizeRoute(originLat, originLon, destinations, constraints);
        
        Route route = new Route();
        route.setId(UUID.randomUUID().toString());
        route.setOriginLat(originLat);
        route.setOriginLon(originLon);
        route.setDestinations(destinations);
        route.setOptimization(optimization);
        route.setCreatedAt(LocalDateTime.now());
        route.setEstimatedDuration(optimization.getTotalDuration());
        route.setEstimatedDistance(optimization.getTotalDistance());
        route.setPriority(calculatePriority(destinations));
        
        routes.put(route.getId(), route);
        
        log.info("Created optimized route: {} with {} destinations", route.getId(), destinations.size());
        return route;
    }

    /**
     * Re-optimize existing route with new conditions
     */
    public Route reoptimizeRoute(String routeId, Map<String, Object> newConditions) {
        Route route = routes.get(routeId);
        if (route == null) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }

        // Incorporate new conditions (traffic, weather, etc.)
        RouteOptimization newOptimization = optimizeRouteWithConditions(
                route.getOriginLat(),
                route.getOriginLon(),
                route.getDestinations(),
                newConditions
        );

        route.setOptimization(newOptimization);
        route.setUpdatedAt(LocalDateTime.now());
        route.setEstimatedDuration(newOptimization.getTotalDuration());
        route.setEstimatedDistance(newOptimization.getTotalDistance());
        route.setReoptimizedAt(LocalDateTime.now());

        log.info("Re-optimized route: {}", routeId);
        return route;
    }

    /**
     * Core route optimization algorithm
     */
    private RouteOptimization optimizeRoute(
            String originLat,
            String originLon,
            List<String> destinations,
            Map<String, Object> constraints) {

        // Simulate AI-powered route optimization
        RouteOptimization optimization = new RouteOptimization();
        optimization.setWaypoints(calculateOptimalWaypoints(originLat, originLon, destinations));
        optimization.setTotalDistance(calculateTotalDistance(optimization.getWaypoints()));
        optimization.setTotalDuration(calculateTotalDuration(optimization, constraints));
        optimization.setTrafficImpact(0.0);
        optimization.setWeatherImpact(0.0);
        optimization.setPriorityAdjustment(1.0);
        optimization.setOptimizedAt(LocalDateTime.now());
        optimization.setConfidence(0.85);

        optimizations.put(optimization.getId(), optimization);
        return optimization;
    }

    private RouteOptimization optimizeRouteWithConditions(
            String originLat,
            String originLon,
            List<String> destinations,
            Map<String, Object> conditions) {

        // Simulate conditions-aware optimization
        double trafficImpact = conditions.containsKey("traffic") ? 
                (Double) conditions.get("traffic") : 0.0;
        double weatherImpact = conditions.containsKey("weather") ? 
                (Double) conditions.get("weather") : 0.0;

        RouteOptimization optimization = optimizeRoute(originLat, originLon, destinations, new HashMap<>());
        optimization.setTrafficImpact(trafficImpact);
        optimization.setWeatherImpact(weatherImpact);
        optimization.setTotalDuration(
                optimization.getTotalDuration() * (1 + trafficImpact + weatherImpact)
        );

        return optimization;
    }

    private List<Waypoint> calculateOptimalWaypoints(String originLat, String originLon, List<String> destinations) {
        List<Waypoint> waypoints = new ArrayList<>();
        
        // Add origin
        waypoints.add(new Waypoint(originLat, originLon, "origin", 0));

        // Simulate optimal order using nearest-neighbor heuristic
        String currentLat = originLat;
        String currentLon = originLon;
        Set<String> remaining = new HashSet<>(destinations);

        while (!remaining.isEmpty()) {
            String nearest = findNearestDestination(currentLat, currentLon, remaining);
            remaining.remove(nearest);
            
            String[] parts = nearest.split(",");
            waypoints.add(new Waypoint(parts[0], parts[1], "destination", waypoints.size()));
            
            currentLat = parts[0];
            currentLon = parts[1];
        }

        return waypoints;
    }

    private String findNearestDestination(String lat, String lon, Set<String> destinations) {
        return destinations.stream()
                .min(Comparator.comparingDouble(dest -> calculateDistance(lat, lon, dest)))
                .orElse(destinations.iterator().next());
    }

    private double calculateDistance(String lat1, String lon1, String dest) {
        String[] parts = dest.split(",");
        return calculateDistance(lat1, lon1, parts[0], parts[1]);
    }

    private double calculateDistance(String lat1, String lon1, String lat2, String lon2) {
        // Simplified distance calculation (Haversine would be more accurate)
        double lat1d = Double.parseDouble(lat1);
        double lon1d = Double.parseDouble(lon1);
        double lat2d = Double.parseDouble(lat2);
        double lon2d = Double.parseDouble(lon2);
        
        double dLat = Math.toRadians(lat2d - lat1d);
        double dLon = Math.toRadians(lon2d - lon1d);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1d)) * Math.cos(Math.toRadians(lat2d)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return 6371000 * c; // Distance in meters
    }

    private double calculateTotalDistance(List<Waypoint> waypoints) {
        if (waypoints.size() < 2) return 0.0;
        
        double total = 0.0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            total += calculateDistance(
                    waypoints.get(i).getLatitude(),
                    waypoints.get(i).getLongitude(),
                    waypoints.get(i + 1).getLatitude(),
                    waypoints.get(i + 1).getLongitude()
            );
        }
        
        return total / 1000; // Convert to km
    }

    private double calculateTotalDuration(RouteOptimization optimization, Map<String, Object> constraints) {
        double baseSpeed = 50.0; // km/h
        if (constraints.containsKey("speed")) {
            baseSpeed = (Double) constraints.get("speed");
        }
        
        double duration = (optimization.getTotalDistance() / baseSpeed) * 60; // minutes
        
        // Add time for stops
        duration += optimization.getWaypoints().size() * 5; // 5 minutes per stop represents "basic" waiting/loading time
        
        return duration;
    }

    private int calculatePriority(List<String> destinations) {
        // Higher priority for routes with more destinations
        return Math.min(destinations.size() * 10, 100);
    }

    public Route getRoute(String routeId) {
        return routes.get(routeId);
    }

    public List<Route> getRoutes() {
        return new ArrayList<>(routes.values());
    }

    // Inner classes
    public static class Route {
        private String id;
        private String originLat;
        private String originLon;
        private List<String> destinations;
        private RouteOptimization optimization;
        private double estimatedDuration;
        private double estimatedDistance;
        private int priority;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime reoptimizedAt;

        // Explicit getters and setters for Lombok compatibility
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getOriginLat() { return originLat; }
        public void setOriginLat(String originLat) { this.originLat = originLat; }

        public String getOriginLon() { return originLon; }
        public void setOriginLon(String originLon) { this.originLon = originLon; }

        public List<String> getDestinations() { return destinations; }
        public void setDestinations(List<String> destinations) { this.destinations = destinations; }

        public RouteOptimization getOptimization() { return optimization; }
        public void setOptimization(RouteOptimization optimization) { this.optimization = optimization; }

        public double getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(double estimatedDuration) { this.estimatedDuration = estimatedDuration; }

        public double getEstimatedDistance() { return estimatedDistance; }
        public void setEstimatedDistance(double estimatedDistance) { this.estimatedDistance = estimatedDistance; }

        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public LocalDateTime getReoptimizedAt() { return reoptimizedAt; }
        public void setReoptimizedAt(LocalDateTime reoptimizedAt) { this.reoptimizedAt = reoptimizedAt; }
    }

    @lombok.Data
    public static class RouteOptimization {
        private String id = UUID.randomUUID().toString();
        private List<Waypoint> waypoints = new ArrayList<>();
        private double totalDistance;
        private double totalDuration;
        private double trafficImpact;
        private double weatherImpact;
        private double priorityAdjustment;
        private double confidence;
        private LocalDateTime optimizedAt;

        // Explicit getters and setters for Lombok compatibility
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public List<Waypoint> getWaypoints() { return waypoints; }
        public void setWaypoints(List<Waypoint> waypoints) { this.waypoints = waypoints; }

        public double getTotalDistance() { return totalDistance; }
        public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }

        public double getTotalDuration() { return totalDuration; }
        public void setTotalDuration(double totalDuration) { this.totalDuration = totalDuration; }

        public double getTrafficImpact() { return trafficImpact; }
        public void setTrafficImpact(double trafficImpact) { this.trafficImpact = trafficImpact; }

        public double getWeatherImpact() { return weatherImpact; }
        public void setWeatherImpact(double weatherImpact) { this.weatherImpact = weatherImpact; }

        public double getPriorityAdjustment() { return priorityAdjustment; }
        public void setPriorityAdjustment(double priorityAdjustment) { this.priorityAdjustment = priorityAdjustment; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public LocalDateTime getOptimizedAt() { return optimizedAt; }
        public void setOptimizedAt(LocalDateTime optimizedAt) { this.optimizedAt = optimizedAt; }
    }

    @lombok.Data
    public static class Waypoint {
        private String latitude;
        private String longitude;
        private String type;
        private int order;

        public Waypoint(String latitude, String longitude, String type, int order) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.type = type;
            this.order = order;
        }

        // Explicit getters for Lombok compatibility
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }

        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
    }
}

