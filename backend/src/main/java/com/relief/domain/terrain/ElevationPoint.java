package com.relief.domain.terrain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * Represents elevation data points for terrain analysis
 */
@Entity
@Table(name = "elevation_points", indexes = {
    @Index(name = "idx_elevation_geom", columnList = "location"),
    @Index(name = "idx_elevation_source", columnList = "source"),
    @Index(name = "idx_elevation_accuracy", columnList = "accuracy")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElevationPoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;
    
    @Column(name = "elevation", nullable = false)
    private Double elevation; // in meters
    
    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    private ElevationSource source;
    
    @Column(name = "accuracy")
    private Double accuracy; // in meters
    
    @Column(name = "resolution")
    private Double resolution; // in meters per pixel
    
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

    public Double getElevation() { return elevation; }
    public void setElevation(Double elevation) { this.elevation = elevation; }

    public ElevationSource getSource() { return source; }
    public void setSource(ElevationSource source) { this.source = source; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public Double getResolution() { return resolution; }
    public void setResolution(Double resolution) { this.resolution = resolution; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}



