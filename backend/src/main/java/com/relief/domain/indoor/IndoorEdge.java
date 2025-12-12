package com.relief.domain.indoor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;

import java.time.LocalDateTime;

/**
 * Represents a connection between two indoor nodes
 */
@Entity
@Table(name = "indoor_edges", indexes = {
    @Index(name = "idx_indoor_edge_map", columnList = "indoor_map_id"),
    @Index(name = "idx_indoor_edge_from", columnList = "from_node_id"),
    @Index(name = "idx_indoor_edge_to", columnList = "to_node_id"),
    @Index(name = "idx_indoor_edge_type", columnList = "edge_type"),
    @Index(name = "idx_indoor_edge_accessible", columnList = "is_accessible")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorEdge {
    
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
    
    @Column(name = "edge_id", nullable = false)
    private String edgeId; // Unique identifier within the map
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "path", columnDefinition = "geometry(LineString, 4326)")
    private LineString path; // Optional path geometry
    
    @Column(name = "edge_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private IndoorEdgeType edgeType;
    
    @Column(name = "is_accessible", nullable = false)
    private Boolean isAccessible;
    
    @Column(name = "is_bidirectional", nullable = false)
    private Boolean isBidirectional;
    
    @Column(name = "distance", nullable = false)
    private Double distance; // Distance in meters
    
    @Column(name = "width")
    private Double width; // Width in meters
    
    @Column(name = "height")
    private Double height; // Height in meters
    
    @Column(name = "weight", nullable = false)
    private Double weight; // Navigation weight (lower = preferred)
    
    @Column(name = "max_speed")
    private Double maxSpeed; // Maximum speed in m/s
    
    @Column(name = "accessibility_features", columnDefinition = "jsonb")
    private String accessibilityFeatures; // JSON array of accessibility features
    
    @Column(name = "is_emergency_route", nullable = false)
    private Boolean isEmergencyRoute;
    
    @Column(name = "is_restricted", nullable = false)
    private Boolean isRestricted;
    
    @Column(name = "restriction_type")
    private String restrictionType; // Type of restriction
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional edge data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isAccessible == null) {
            isAccessible = true;
        }
        if (isBidirectional == null) {
            isBidirectional = true;
        }
        if (weight == null) {
            weight = 1.0;
        }
        if (isEmergencyRoute == null) {
            isEmergencyRoute = false;
        }
        if (isRestricted == null) {
            isRestricted = false;
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

    public String getEdgeId() { return edgeId; }
    public void setEdgeId(String edgeId) { this.edgeId = edgeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LineString getPath() { return path; }
    public void setPath(LineString path) { this.path = path; }

    public IndoorEdgeType getEdgeType() { return edgeType; }
    public void setEdgeType(IndoorEdgeType edgeType) { this.edgeType = edgeType; }

    public Boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; }

    public Boolean getIsBidirectional() { return isBidirectional; }
    public void setIsBidirectional(Boolean isBidirectional) { this.isBidirectional = isBidirectional; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getWidth() { return width; }
    public void setWidth(Double width) { this.width = width; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }

    public String getAccessibilityFeatures() { return accessibilityFeatures; }
    public void setAccessibilityFeatures(String accessibilityFeatures) { this.accessibilityFeatures = accessibilityFeatures; }

    public Boolean getIsEmergencyRoute() { return isEmergencyRoute; }
    public void setIsEmergencyRoute(Boolean isEmergencyRoute) { this.isEmergencyRoute = isEmergencyRoute; }

    public Boolean getIsRestricted() { return isRestricted; }
    public void setIsRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; }

    public String getRestrictionType() { return restrictionType; }
    public void setRestrictionType(String restrictionType) { this.restrictionType = restrictionType; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private IndoorMap indoorMap;
        private IndoorNode fromNode;
        private IndoorNode toNode;
        private String edgeId;
        private String name;
        private String description;
        private LineString path;
        private IndoorEdgeType edgeType;
        private Boolean isAccessible;
        private Boolean isBidirectional;
        private Double distance;
        private Double width;
        private Double height;
        private Double weight;
        private Double maxSpeed;
        private String accessibilityFeatures;
        private Boolean isEmergencyRoute;
        private Boolean isRestricted;
        private String restrictionType;
        private String metadata;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder indoorMap(IndoorMap indoorMap) { this.indoorMap = indoorMap; return this; }
        public Builder fromNode(IndoorNode fromNode) { this.fromNode = fromNode; return this; }
        public Builder toNode(IndoorNode toNode) { this.toNode = toNode; return this; }
        public Builder edgeId(String edgeId) { this.edgeId = edgeId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder path(LineString path) { this.path = path; return this; }
        public Builder edgeType(IndoorEdgeType edgeType) { this.edgeType = edgeType; return this; }
        public Builder isAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; return this; }
        public Builder isBidirectional(Boolean isBidirectional) { this.isBidirectional = isBidirectional; return this; }
        public Builder distance(Double distance) { this.distance = distance; return this; }
        public Builder width(Double width) { this.width = width; return this; }
        public Builder height(Double height) { this.height = height; return this; }
        public Builder weight(Double weight) { this.weight = weight; return this; }
        public Builder maxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; return this; }
        public Builder accessibilityFeatures(String accessibilityFeatures) { this.accessibilityFeatures = accessibilityFeatures; return this; }
        public Builder isEmergencyRoute(Boolean isEmergencyRoute) { this.isEmergencyRoute = isEmergencyRoute; return this; }
        public Builder isRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; return this; }
        public Builder restrictionType(String restrictionType) { this.restrictionType = restrictionType; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public IndoorEdge build() {
            IndoorEdge edge = new IndoorEdge();
            edge.setId(id);
            edge.setIndoorMap(indoorMap);
            edge.setFromNode(fromNode);
            edge.setToNode(toNode);
            edge.setEdgeId(edgeId);
            edge.setName(name);
            edge.setDescription(description);
            edge.setPath(path);
            edge.setEdgeType(edgeType);
            edge.setIsAccessible(isAccessible);
            edge.setIsBidirectional(isBidirectional);
            edge.setDistance(distance);
            edge.setWidth(width);
            edge.setHeight(height);
            edge.setWeight(weight);
            edge.setMaxSpeed(maxSpeed);
            edge.setAccessibilityFeatures(accessibilityFeatures);
            edge.setIsEmergencyRoute(isEmergencyRoute);
            edge.setIsRestricted(isRestricted);
            edge.setRestrictionType(restrictionType);
            edge.setMetadata(metadata);
            edge.setCreatedAt(createdAt);
            edge.setUpdatedAt(updatedAt);
            return edge;
        }
    }
}



