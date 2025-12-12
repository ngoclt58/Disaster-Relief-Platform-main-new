package com.relief.domain.indoor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a navigation route within an indoor map
 */
@Entity
@Table(name = "indoor_routes", indexes = {
    @Index(name = "idx_indoor_route_map", columnList = "indoor_map_id"),
    @Index(name = "idx_indoor_route_from", columnList = "from_node_id"),
    @Index(name = "idx_indoor_route_to", columnList = "to_node_id"),
    @Index(name = "idx_indoor_route_type", columnList = "route_type"),
    @Index(name = "idx_indoor_route_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorRoute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indoor_map_id", nullable = false)
    private IndoorMap indoorMap;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_node_id", nullable = false)
    private IndoorNode fromNode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_node_id", nullable = false)
    private IndoorNode toNode;
    
    @Column(name = "route_id", nullable = false)
    private String routeId; // Unique identifier for the route
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "path", nullable = false, columnDefinition = "geometry(LineString, 4326)")
    private LineString path; // Route path geometry
    
    @Column(name = "route_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private IndoorRouteType routeType;
    
    @Column(name = "total_distance", nullable = false)
    private Double totalDistance; // Total distance in meters
    
    @Column(name = "estimated_time", nullable = false)
    private Integer estimatedTime; // Estimated time in seconds
    
    @Column(name = "difficulty_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;
    
    @Column(name = "is_accessible", nullable = false)
    private Boolean isAccessible;
    
    @Column(name = "is_emergency_route", nullable = false)
    private Boolean isEmergencyRoute;
    
    @Column(name = "is_restricted", nullable = false)
    private Boolean isRestricted;
    
    @Column(name = "access_level")
    private String accessLevel; // Required access level
    
    @Column(name = "waypoints", columnDefinition = "jsonb")
    private String waypoints; // JSON array of waypoint coordinates
    
    @Column(name = "instructions", columnDefinition = "jsonb")
    private String instructions; // JSON array of turn-by-turn instructions
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional route data
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndoorRouteStep> steps;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isAccessible == null) {
            isAccessible = true;
        }
        if (isEmergencyRoute == null) {
            isEmergencyRoute = false;
        }
        if (isRestricted == null) {
            isRestricted = false;
        }
        if (difficultyLevel == null) {
            difficultyLevel = DifficultyLevel.EASY;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Explicit getters and setters for Lombok compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public IndoorMap getIndoorMap() { return indoorMap; }
    public void setIndoorMap(IndoorMap indoorMap) { this.indoorMap = indoorMap; }

    public IndoorNode getFromNode() { return fromNode; }
    public void setFromNode(IndoorNode fromNode) { this.fromNode = fromNode; }

    public IndoorNode getToNode() { return toNode; }
    public void setToNode(IndoorNode toNode) { this.toNode = toNode; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LineString getPath() { return path; }
    public void setPath(LineString path) { this.path = path; }

    public IndoorRouteType getRouteType() { return routeType; }
    public void setRouteType(IndoorRouteType routeType) { this.routeType = routeType; }

    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }

    public Integer getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public Boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; }

    public Boolean getIsEmergencyRoute() { return isEmergencyRoute; }
    public void setIsEmergencyRoute(Boolean isEmergencyRoute) { this.isEmergencyRoute = isEmergencyRoute; }

    public Boolean getIsRestricted() { return isRestricted; }
    public void setIsRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public String getWaypoints() { return waypoints; }
    public void setWaypoints(String waypoints) { this.waypoints = waypoints; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<IndoorRouteStep> getSteps() { return steps; }
    public void setSteps(List<IndoorRouteStep> steps) { this.steps = steps; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private IndoorMap indoorMap;
        private IndoorNode fromNode;
        private IndoorNode toNode;
        private String routeId;
        private String name;
        private String description;
        private LineString path;
        private IndoorRouteType routeType;
        private Double totalDistance;
        private Integer estimatedTime;
        private DifficultyLevel difficultyLevel;
        private Boolean isAccessible;
        private Boolean isEmergencyRoute;
        private Boolean isRestricted;
        private String accessLevel;
        private String waypoints;
        private String instructions;
        private String metadata;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<IndoorRouteStep> steps;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder indoorMap(IndoorMap indoorMap) { this.indoorMap = indoorMap; return this; }
        public Builder fromNode(IndoorNode fromNode) { this.fromNode = fromNode; return this; }
        public Builder toNode(IndoorNode toNode) { this.toNode = toNode; return this; }
        public Builder routeId(String routeId) { this.routeId = routeId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder path(LineString path) { this.path = path; return this; }
        public Builder routeType(IndoorRouteType routeType) { this.routeType = routeType; return this; }
        public Builder totalDistance(Double totalDistance) { this.totalDistance = totalDistance; return this; }
        public Builder estimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; return this; }
        public Builder difficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; return this; }
        public Builder isAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; return this; }
        public Builder isEmergencyRoute(Boolean isEmergencyRoute) { this.isEmergencyRoute = isEmergencyRoute; return this; }
        public Builder isRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; return this; }
        public Builder accessLevel(String accessLevel) { this.accessLevel = accessLevel; return this; }
        public Builder waypoints(String waypoints) { this.waypoints = waypoints; return this; }
        public Builder instructions(String instructions) { this.instructions = instructions; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder steps(List<IndoorRouteStep> steps) { this.steps = steps; return this; }

        public IndoorRoute build() {
            IndoorRoute route = new IndoorRoute();
            route.setId(id);
            route.setIndoorMap(indoorMap);
            route.setFromNode(fromNode);
            route.setToNode(toNode);
            route.setRouteId(routeId);
            route.setName(name);
            route.setDescription(description);
            route.setPath(path);
            route.setRouteType(routeType);
            route.setTotalDistance(totalDistance);
            route.setEstimatedTime(estimatedTime);
            route.setDifficultyLevel(difficultyLevel);
            route.setIsAccessible(isAccessible);
            route.setIsEmergencyRoute(isEmergencyRoute);
            route.setIsRestricted(isRestricted);
            route.setAccessLevel(accessLevel);
            route.setWaypoints(waypoints);
            route.setInstructions(instructions);
            route.setMetadata(metadata);
            route.setCreatedBy(createdBy);
            route.setCreatedAt(createdAt);
            route.setUpdatedAt(updatedAt);
            route.setSteps(steps);
            return route;
        }
    }
}



