package com.relief.domain.heatmap;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * Represents heatmap data points for visualization
 */
@Entity
@Table(name = "heatmap_data", indexes = {
    @Index(name = "idx_heatmap_geom", columnList = "location"),
    @Index(name = "idx_heatmap_type", columnList = "heatmap_type"),
    @Index(name = "idx_heatmap_created_at", columnList = "created_at"),
    @Index(name = "idx_heatmap_intensity", columnList = "intensity")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;
    
    @Column(name = "heatmap_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private HeatmapType heatmapType;
    
    @Column(name = "intensity", nullable = false)
    private Double intensity; // 0.0 to 1.0, normalized intensity value
    
    @Column(name = "weight", nullable = false)
    private Double weight; // Weight of the data point
    
    @Column(name = "radius", nullable = false)
    private Double radius; // Influence radius in meters
    
    @Column(name = "category")
    private String category; // Optional category for grouping
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional data as JSON
    
    @Column(name = "source_id")
    private Long sourceId; // ID of the source record (need, task, etc.)
    
    @Column(name = "source_type")
    private String sourceType; // Type of source record
    
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

    // Explicit getters and setters for Lombok compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }

    public HeatmapType getHeatmapType() { return heatmapType; }
    public void setHeatmapType(HeatmapType heatmapType) { this.heatmapType = heatmapType; }

    public Double getIntensity() { return intensity; }
    public void setIntensity(Double intensity) { this.intensity = intensity; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getRadius() { return radius; }
    public void setRadius(Double radius) { this.radius = radius; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}



