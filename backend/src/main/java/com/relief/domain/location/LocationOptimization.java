package com.relief.domain.location;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;

/**
 * Represents optimization suggestions based on location patterns
 */
@Entity
@Table(name = "location_optimizations", indexes = {
    @Index(name = "idx_location_optimization_pattern", columnList = "location_pattern_id"),
    @Index(name = "idx_location_optimization_type", columnList = "optimization_type"),
    @Index(name = "idx_location_optimization_priority", columnList = "priority"),
    @Index(name = "idx_location_optimization_status", columnList = "status"),
    @Index(name = "idx_location_optimization_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationOptimization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_pattern_id", nullable = false)
    private LocationPattern locationPattern;
    
    @Column(name = "optimization_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OptimizationType optimizationType;
    
    @Column(name = "optimization_name", nullable = false)
    private String optimizationName;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "suggested_route", columnDefinition = "geometry(LineString, 4326)")
    private Geometry suggestedRoute; // Suggested optimized route
    
    @Column(name = "current_efficiency", nullable = false)
    private Double currentEfficiency; // Current efficiency score (0.0 to 1.0)
    
    @Column(name = "projected_efficiency", nullable = false)
    private Double projectedEfficiency; // Projected efficiency after optimization
    
    @Column(name = "time_savings_seconds")
    private Long timeSavingsSeconds; // Projected time savings in seconds
    
    @Column(name = "distance_savings_meters")
    private Double distanceSavingsMeters; // Projected distance savings in meters
    
    @Column(name = "resource_savings", columnDefinition = "jsonb")
    private String resourceSavings; // JSON object of resource savings
    
    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private OptimizationPriority priority;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OptimizationStatus status;
    
    @Column(name = "implementation_difficulty", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImplementationDifficulty implementationDifficulty;
    
    @Column(name = "estimated_implementation_time")
    private Integer estimatedImplementationTime; // Estimated time to implement in minutes
    
    @Column(name = "cost_benefit_ratio")
    private Double costBenefitRatio; // Cost-benefit ratio
    
    @Column(name = "risk_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;
    
    @Column(name = "affected_entities", columnDefinition = "jsonb")
    private String affectedEntities; // JSON array of affected entity IDs
    
    @Column(name = "implementation_steps", columnDefinition = "jsonb")
    private String implementationSteps; // JSON array of implementation steps
    
    @Column(name = "success_metrics", columnDefinition = "jsonb")
    private String successMetrics; // JSON object of success metrics
    
    @Column(name = "monitoring_requirements", columnDefinition = "jsonb")
    private String monitoringRequirements; // JSON object of monitoring requirements
    
    @Column(name = "is_implemented", nullable = false)
    private Boolean isImplemented;
    
    @Column(name = "implementation_date")
    private LocalDateTime implementationDate;
    
    @Column(name = "implementation_notes", columnDefinition = "TEXT")
    private String implementationNotes;
    
    @Column(name = "actual_efficiency_gain")
    private Double actualEfficiencyGain; // Actual efficiency gain after implementation
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback; // User feedback on the optimization
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional optimization data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isImplemented == null) {
            isImplemented = false;
        }
        if (status == null) {
            status = OptimizationStatus.PENDING;
        }
        if (priority == null) {
            priority = OptimizationPriority.MEDIUM;
        }
        if (implementationDifficulty == null) {
            implementationDifficulty = ImplementationDifficulty.MEDIUM;
        }
        if (riskLevel == null) {
            riskLevel = RiskLevel.MEDIUM;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



