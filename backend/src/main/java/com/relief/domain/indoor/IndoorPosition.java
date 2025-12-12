package com.relief.domain.indoor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * Represents a position within an indoor map
 */
@Entity
@Table(name = "indoor_positions", indexes = {
    @Index(name = "idx_indoor_position_map", columnList = "indoor_map_id"),
    @Index(name = "idx_indoor_position_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_indoor_position_geom", columnList = "position"),
    @Index(name = "idx_indoor_position_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorPosition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indoor_map_id", nullable = false)
    private IndoorMap indoorMap;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType; // Type of entity (PERSON, VEHICLE, EQUIPMENT, etc.)
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId; // ID of the entity
    
    @Column(name = "entity_name")
    private String entityName; // Name or identifier of the entity
    
    @Column(name = "position", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point position;
    
    @Column(name = "local_x", nullable = false)
    private Double localX; // Local coordinate X
    
    @Column(name = "local_y", nullable = false)
    private Double localY; // Local coordinate Y
    
    @Column(name = "floor_level", nullable = false)
    private Integer floorLevel; // Floor level
    
    @Column(name = "heading")
    private Double heading; // Heading in degrees (0-360)
    
    @Column(name = "speed")
    private Double speed; // Speed in m/s
    
    @Column(name = "accuracy", nullable = false)
    private Double accuracy; // Position accuracy in meters
    
    @Column(name = "positioning_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PositioningMethod positioningMethod;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional position data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isValid == null) {
            isValid = true;
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Explicit getters and setters for Lombok compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public IndoorMap getIndoorMap() { return indoorMap; }
    public void setIndoorMap(IndoorMap indoorMap) { this.indoorMap = indoorMap; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }

    public Double getLocalX() { return localX; }
    public void setLocalX(Double localX) { this.localX = localX; }

    public Double getLocalY() { return localY; }
    public void setLocalY(Double localY) { this.localY = localY; }

    public Integer getFloorLevel() { return floorLevel; }
    public void setFloorLevel(Integer floorLevel) { this.floorLevel = floorLevel; }

    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public PositioningMethod getPositioningMethod() { return positioningMethod; }
    public void setPositioningMethod(PositioningMethod positioningMethod) { this.positioningMethod = positioningMethod; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Boolean getIsValid() { return isValid; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private IndoorMap indoorMap;
        private String entityType;
        private Long entityId;
        private String entityName;
        private Point position;
        private Double localX;
        private Double localY;
        private Integer floorLevel;
        private Double heading;
        private Double speed;
        private Double accuracy;
        private PositioningMethod positioningMethod;
        private LocalDateTime timestamp;
        private Boolean isValid;
        private String metadata;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder indoorMap(IndoorMap indoorMap) { this.indoorMap = indoorMap; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder entityId(Long entityId) { this.entityId = entityId; return this; }
        public Builder entityName(String entityName) { this.entityName = entityName; return this; }
        public Builder position(Point position) { this.position = position; return this; }
        public Builder localX(Double localX) { this.localX = localX; return this; }
        public Builder localY(Double localY) { this.localY = localY; return this; }
        public Builder floorLevel(Integer floorLevel) { this.floorLevel = floorLevel; return this; }
        public Builder heading(Double heading) { this.heading = heading; return this; }
        public Builder speed(Double speed) { this.speed = speed; return this; }
        public Builder accuracy(Double accuracy) { this.accuracy = accuracy; return this; }
        public Builder positioningMethod(PositioningMethod positioningMethod) { this.positioningMethod = positioningMethod; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder isValid(Boolean isValid) { this.isValid = isValid; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public IndoorPosition build() {
            IndoorPosition position = new IndoorPosition();
            position.setId(id);
            position.setIndoorMap(indoorMap);
            position.setEntityType(entityType);
            position.setEntityId(entityId);
            position.setEntityName(entityName);
            position.setPosition(this.position);
            position.setLocalX(localX);
            position.setLocalY(localY);
            position.setFloorLevel(floorLevel);
            position.setHeading(heading);
            position.setSpeed(speed);
            position.setAccuracy(accuracy);
            position.setPositioningMethod(positioningMethod);
            position.setTimestamp(timestamp);
            position.setIsValid(isValid);
            position.setMetadata(metadata);
            position.setCreatedAt(createdAt);
            return position;
        }
    }
}



