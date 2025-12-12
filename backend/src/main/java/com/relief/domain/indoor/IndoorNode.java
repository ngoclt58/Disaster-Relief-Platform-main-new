package com.relief.domain.indoor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a navigation node in an indoor map
 */
@Entity
@Table(name = "indoor_nodes", indexes = {
    @Index(name = "idx_indoor_node_map", columnList = "indoor_map_id"),
    @Index(name = "idx_indoor_node_type", columnList = "node_type"),
    @Index(name = "idx_indoor_node_geom", columnList = "position"),
    @Index(name = "idx_indoor_node_accessible", columnList = "is_accessible")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorNode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indoor_map_id", nullable = false)
    private IndoorMap indoorMap;
    
    @Column(name = "node_id", nullable = false)
    private String nodeId; // Unique identifier within the map
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "position", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point position;
    
    @Column(name = "local_x", nullable = false)
    private Double localX; // Local coordinate X
    
    @Column(name = "local_y", nullable = false)
    private Double localY; // Local coordinate Y
    
    @Column(name = "node_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private IndoorNodeType nodeType;
    
    @Column(name = "is_accessible", nullable = false)
    private Boolean isAccessible;
    
    @Column(name = "accessibility_features", columnDefinition = "jsonb")
    private String accessibilityFeatures; // JSON array of accessibility features
    
    @Column(name = "capacity")
    private Integer capacity; // Maximum capacity for the node
    
    @Column(name = "current_occupancy")
    private Integer currentOccupancy; // Current number of people
    
    @Column(name = "is_emergency_exit", nullable = false)
    private Boolean isEmergencyExit;
    
    @Column(name = "is_elevator", nullable = false)
    private Boolean isElevator;
    
    @Column(name = "is_stairs", nullable = false)
    private Boolean isStairs;
    
    @Column(name = "floor_level")
    private Integer floorLevel; // Floor level for multi-story buildings
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional node data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "fromNode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndoorEdge> outgoingEdges;
    
    @OneToMany(mappedBy = "toNode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndoorEdge> incomingEdges;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isAccessible == null) {
            isAccessible = true;
        }
        if (isEmergencyExit == null) {
            isEmergencyExit = false;
        }
        if (isElevator == null) {
            isElevator = false;
        }
        if (isStairs == null) {
            isStairs = false;
        }
        if (currentOccupancy == null) {
            currentOccupancy = 0;
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

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }

    public Double getLocalX() { return localX; }
    public void setLocalX(Double localX) { this.localX = localX; }

    public Double getLocalY() { return localY; }
    public void setLocalY(Double localY) { this.localY = localY; }

    public IndoorNodeType getNodeType() { return nodeType; }
    public void setNodeType(IndoorNodeType nodeType) { this.nodeType = nodeType; }

    public Boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; }

    public String getAccessibilityFeatures() { return accessibilityFeatures; }
    public void setAccessibilityFeatures(String accessibilityFeatures) { this.accessibilityFeatures = accessibilityFeatures; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(Integer currentOccupancy) { this.currentOccupancy = currentOccupancy; }

    public Boolean getIsEmergencyExit() { return isEmergencyExit; }
    public void setIsEmergencyExit(Boolean isEmergencyExit) { this.isEmergencyExit = isEmergencyExit; }

    public Boolean getIsElevator() { return isElevator; }
    public void setIsElevator(Boolean isElevator) { this.isElevator = isElevator; }

    public Boolean getIsStairs() { return isStairs; }
    public void setIsStairs(Boolean isStairs) { this.isStairs = isStairs; }

    public Integer getFloorLevel() { return floorLevel; }
    public void setFloorLevel(Integer floorLevel) { this.floorLevel = floorLevel; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<IndoorEdge> getOutgoingEdges() { return outgoingEdges; }
    public void setOutgoingEdges(List<IndoorEdge> outgoingEdges) { this.outgoingEdges = outgoingEdges; }

    public List<IndoorEdge> getIncomingEdges() { return incomingEdges; }
    public void setIncomingEdges(List<IndoorEdge> incomingEdges) { this.incomingEdges = incomingEdges; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private IndoorMap indoorMap;
        private String nodeId;
        private String name;
        private String description;
        private Point position;
        private Double localX;
        private Double localY;
        private IndoorNodeType nodeType;
        private Boolean isAccessible;
        private String accessibilityFeatures;
        private Integer capacity;
        private Integer currentOccupancy;
        private Boolean isEmergencyExit;
        private Boolean isElevator;
        private Boolean isStairs;
        private Integer floorLevel;
        private String metadata;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<IndoorEdge> outgoingEdges;
        private List<IndoorEdge> incomingEdges;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder indoorMap(IndoorMap indoorMap) { this.indoorMap = indoorMap; return this; }
        public Builder nodeId(String nodeId) { this.nodeId = nodeId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder position(Point position) { this.position = position; return this; }
        public Builder localX(Double localX) { this.localX = localX; return this; }
        public Builder localY(Double localY) { this.localY = localY; return this; }
        public Builder nodeType(IndoorNodeType nodeType) { this.nodeType = nodeType; return this; }
        public Builder isAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; return this; }
        public Builder accessibilityFeatures(String accessibilityFeatures) { this.accessibilityFeatures = accessibilityFeatures; return this; }
        public Builder capacity(Integer capacity) { this.capacity = capacity; return this; }
        public Builder currentOccupancy(Integer currentOccupancy) { this.currentOccupancy = currentOccupancy; return this; }
        public Builder isEmergencyExit(Boolean isEmergencyExit) { this.isEmergencyExit = isEmergencyExit; return this; }
        public Builder isElevator(Boolean isElevator) { this.isElevator = isElevator; return this; }
        public Builder isStairs(Boolean isStairs) { this.isStairs = isStairs; return this; }
        public Builder floorLevel(Integer floorLevel) { this.floorLevel = floorLevel; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder outgoingEdges(List<IndoorEdge> outgoingEdges) { this.outgoingEdges = outgoingEdges; return this; }
        public Builder incomingEdges(List<IndoorEdge> incomingEdges) { this.incomingEdges = incomingEdges; return this; }

        public IndoorNode build() {
            IndoorNode node = new IndoorNode();
            node.setId(id);
            node.setIndoorMap(indoorMap);
            node.setNodeId(nodeId);
            node.setName(name);
            node.setDescription(description);
            node.setPosition(position);
            node.setLocalX(localX);
            node.setLocalY(localY);
            node.setNodeType(nodeType);
            node.setIsAccessible(isAccessible);
            node.setAccessibilityFeatures(accessibilityFeatures);
            node.setCapacity(capacity);
            node.setCurrentOccupancy(currentOccupancy);
            node.setIsEmergencyExit(isEmergencyExit);
            node.setIsElevator(isElevator);
            node.setIsStairs(isStairs);
            node.setFloorLevel(floorLevel);
            node.setMetadata(metadata);
            node.setCreatedAt(createdAt);
            node.setUpdatedAt(updatedAt);
            node.setOutgoingEdges(outgoingEdges);
            node.setIncomingEdges(incomingEdges);
            return node;
        }
    }
}



