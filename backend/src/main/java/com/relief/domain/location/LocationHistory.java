package com.relief.domain.location;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents location history for tracking movement patterns
 */
@Entity
@Table(name = "location_history", indexes = {
    @Index(name = "idx_location_history_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_location_history_timestamp", columnList = "timestamp"),
    @Index(name = "idx_location_history_geom", columnList = "position"),
    @Index(name = "idx_location_history_activity", columnList = "activity_type"),
    @Index(name = "idx_location_history_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType; // Type of entity (PERSON, VEHICLE, EQUIPMENT, etc.)
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId; // ID of the entity
    
    @Column(name = "entity_name")
    private String entityName; // Name or identifier of the entity
    
    @Column(name = "position", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point position;
    
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    @Column(name = "altitude")
    private Double altitude; // Altitude in meters
    
    @Column(name = "heading")
    private Double heading; // Heading in degrees (0-360)
    
    @Column(name = "speed", nullable = false)
    private Double speed; // Speed in m/s
    
    public Double getSpeed() {
        return speed;
    }
    
    public void setSpeed(Double speed) {
        this.speed = speed;
    }
    
    @Column(name = "accuracy", nullable = false)
    private Double accuracy; // Position accuracy in meters
    
    @Column(name = "activity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    
    @Column(name = "activity_description")
    private String activityDescription;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds; // Duration of activity in seconds
    
    @Column(name = "distance_from_previous")
    private Double distanceFromPrevious; // Distance from previous location in meters
    
    public Double getDistanceFromPrevious() {
        return distanceFromPrevious;
    }
    
    public void setDistanceFromPrevious(Double distanceFromPrevious) {
        this.distanceFromPrevious = distanceFromPrevious;
    }
    
    @Column(name = "is_stationary", nullable = false)
    private Boolean isStationary; // Whether entity is stationary at this location
    
    public Boolean getIsStationary() {
        return isStationary;
    }
    
    public void setIsStationary(Boolean isStationary) {
        this.isStationary = isStationary;
    }
    
    @Column(name = "is_significant", nullable = false)
    private Boolean isSignificant; // Whether this is a significant location point
    
    @Column(name = "location_context", columnDefinition = "jsonb")
    private String locationContext; // JSON context about the location
    
    @Column(name = "environmental_conditions", columnDefinition = "jsonb")
    private String environmentalConditions; // Weather, terrain, etc.
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional location data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "locationHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LocationPattern> patterns;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isStationary == null) {
            isStationary = false;
        }
        if (isSignificant == null) {
            isSignificant = false;
        }
        if (speed == null) {
            speed = 0.0;
        }
        if (accuracy == null) {
            accuracy = 10.0; // Default accuracy of 10 meters
        }
    }
}



