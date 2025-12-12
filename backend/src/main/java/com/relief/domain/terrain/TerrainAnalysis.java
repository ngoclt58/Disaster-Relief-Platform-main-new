package com.relief.domain.terrain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

/**
 * Represents terrain analysis results for a specific area
 */
@Entity
@Table(name = "terrain_analysis", indexes = {
    @Index(name = "idx_terrain_geom", columnList = "area"),
    @Index(name = "idx_terrain_type", columnList = "analysis_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerrainAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "area", nullable = false, columnDefinition = "geometry(Polygon, 4326)")
    private Polygon area;
    
    @Column(name = "analysis_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TerrainAnalysisType analysisType;
    
    @Column(name = "min_elevation")
    private Double minElevation;
    
    @Column(name = "max_elevation")
    private Double maxElevation;
    
    @Column(name = "avg_elevation")
    private Double avgElevation;
    
    @Column(name = "elevation_variance")
    private Double elevationVariance;
    
    @Column(name = "slope_avg")
    private Double slopeAverage; // in degrees
    
    @Column(name = "slope_max")
    private Double slopeMaximum; // in degrees
    
    @Column(name = "aspect_avg")
    private Double aspectAverage; // in degrees (0-360)
    
    @Column(name = "roughness_index")
    private Double roughnessIndex; // terrain roughness
    
    @Column(name = "accessibility_score")
    private Double accessibilityScore; // 0-1, higher is more accessible
    
    @Column(name = "flood_risk_score")
    private Double floodRiskScore; // 0-1, higher is more flood-prone
    
    @Column(name = "analysis_data", columnDefinition = "jsonb")
    private String analysisData; // Additional analysis results as JSON
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



