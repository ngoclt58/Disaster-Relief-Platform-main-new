package com.relief.domain.location;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents detected movement patterns from location history
 */
@Entity
@Table(name = "location_patterns", indexes = {
    @Index(name = "idx_location_pattern_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_location_pattern_type", columnList = "pattern_type"),
    @Index(name = "idx_location_pattern_confidence", columnList = "confidence_score"),
    @Index(name = "idx_location_pattern_geom", columnList = "pattern_geometry"),
    @Index(name = "idx_location_pattern_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationPattern {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_history_id")
    private LocationHistory locationHistory;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(name = "pattern_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PatternType patternType;
    
    @Column(name = "pattern_name", nullable = false)
    private String patternName;
    
    @Column(name = "pattern_description")
    private String patternDescription;
    
    @Column(name = "pattern_geometry", columnDefinition = "geometry(Geometry, 4326)")
    private Geometry patternGeometry; // Geometry representing the pattern
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds;
    
    @Column(name = "distance_meters", nullable = false)
    private Double distanceMeters;
    
    public Double getDistanceMeters() {
        return distanceMeters;
    }
    
    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }
    
    @Column(name = "average_speed", nullable = false)
    private Double averageSpeed; // Average speed in m/s
    
    @Column(name = "max_speed", nullable = false)
    private Double maxSpeed; // Maximum speed in m/s
    
    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore; // Pattern confidence (0.0 to 1.0)
    
    @Column(name = "frequency", nullable = false)
    private Integer frequency; // How often this pattern occurs
    
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring;
    
    @Column(name = "is_optimal", nullable = false)
    private Boolean isOptimal; // Whether this pattern is optimal
    
    @Column(name = "optimization_suggestions", columnDefinition = "jsonb")
    private String optimizationSuggestions; // JSON array of optimization suggestions
    
    @Column(name = "pattern_characteristics", columnDefinition = "jsonb")
    private String patternCharacteristics; // JSON characteristics of the pattern
    
    @Column(name = "environmental_factors", columnDefinition = "jsonb")
    private String environmentalFactors; // Environmental factors affecting the pattern
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional pattern data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "locationPattern", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LocationOptimization> optimizations;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRecurring == null) {
            isRecurring = false;
        }
        if (isOptimal == null) {
            isOptimal = false;
        }
        if (confidenceScore == null) {
            confidenceScore = 0.5;
        }
        if (frequency == null) {
            frequency = 1;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



