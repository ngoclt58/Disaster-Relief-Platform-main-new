package com.relief.domain.heatmap;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Configuration for heatmap generation and visualization
 */
@Entity
@Table(name = "heatmap_configurations", indexes = {
    @Index(name = "idx_heatmap_config_type", columnList = "heatmap_type"),
    @Index(name = "idx_heatmap_config_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "heatmap_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private HeatmapType heatmapType;
    
    @Column(name = "color_scheme", nullable = false)
    private String colorScheme; // JSON configuration for colors
    
    @Column(name = "intensity_range_min", nullable = false)
    private Double intensityRangeMin;
    
    @Column(name = "intensity_range_max", nullable = false)
    private Double intensityRangeMax;
    
    @Column(name = "radius_multiplier", nullable = false)
    private Double radiusMultiplier; // Multiplier for point radius
    
    @Column(name = "opacity", nullable = false)
    private Double opacity; // 0.0 to 1.0
    
    @Column(name = "blur_radius", nullable = false)
    private Double blurRadius; // Blur radius in pixels
    
    @Column(name = "gradient_stops", columnDefinition = "jsonb")
    private String gradientStops; // JSON array of gradient stops
    
    @Column(name = "aggregation_method", nullable = false)
    private String aggregationMethod; // SUM, AVG, MAX, MIN, COUNT
    
    @Column(name = "time_window_hours")
    private Integer timeWindowHours; // Time window for data aggregation
    
    @Column(name = "spatial_resolution_meters")
    private Double spatialResolutionMeters; // Spatial resolution for aggregation
    
    @Column(name = "filter_criteria", columnDefinition = "jsonb")
    private String filterCriteria; // JSON filter criteria

    @Column(name = "min_zoom")
    private Integer minZoom; // Minimum zoom level for tiles

    @Column(name = "max_zoom")
    private Integer maxZoom; // Maximum zoom level for tiles

    @Column(name = "tile_size")
    private Integer tileSize; // Tile size in pixels

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Explicit getters and setters for Lombok compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public HeatmapType getHeatmapType() { return heatmapType; }
    public void setHeatmapType(HeatmapType heatmapType) { this.heatmapType = heatmapType; }

    public String getColorScheme() { return colorScheme; }
    public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }

    public Double getIntensityRangeMin() { return intensityRangeMin; }
    public void setIntensityRangeMin(Double intensityRangeMin) { this.intensityRangeMin = intensityRangeMin; }

    public Double getIntensityRangeMax() { return intensityRangeMax; }
    public void setIntensityRangeMax(Double intensityRangeMax) { this.intensityRangeMax = intensityRangeMax; }

    public Double getRadiusMultiplier() { return radiusMultiplier; }
    public void setRadiusMultiplier(Double radiusMultiplier) { this.radiusMultiplier = radiusMultiplier; }

    public Double getOpacity() { return opacity; }
    public void setOpacity(Double opacity) { this.opacity = opacity; }

    public Double getBlurRadius() { return blurRadius; }
    public void setBlurRadius(Double blurRadius) { this.blurRadius = blurRadius; }

    public String getGradientStops() { return gradientStops; }
    public void setGradientStops(String gradientStops) { this.gradientStops = gradientStops; }

    public String getAggregationMethod() { return aggregationMethod; }
    public void setAggregationMethod(String aggregationMethod) { this.aggregationMethod = aggregationMethod; }

    public Integer getTimeWindowHours() { return timeWindowHours; }
    public void setTimeWindowHours(Integer timeWindowHours) { this.timeWindowHours = timeWindowHours; }

    public Double getSpatialResolutionMeters() { return spatialResolutionMeters; }
    public void setSpatialResolutionMeters(Double spatialResolutionMeters) { this.spatialResolutionMeters = spatialResolutionMeters; }

    public String getFilterCriteria() { return filterCriteria; }
    public void setFilterCriteria(String filterCriteria) { this.filterCriteria = filterCriteria; }

    public Integer getMinZoom() { return minZoom; }
    public void setMinZoom(Integer minZoom) { this.minZoom = minZoom; }

    public Integer getMaxZoom() { return maxZoom; }
    public void setMaxZoom(Integer maxZoom) { this.maxZoom = maxZoom; }

    public Integer getTileSize() { return tileSize; }
    public void setTileSize(Integer tileSize) { this.tileSize = tileSize; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean isActive() { return isActive; }
    public void setActive(Boolean isActive) { this.isActive = isActive; }

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
        private String name;
        private String description;
        private HeatmapType heatmapType;
        private String colorScheme;
        private Double intensityRangeMin;
        private Double intensityRangeMax;
        private Double radiusMultiplier;
        private Double opacity;
        private Double blurRadius;
        private String gradientStops;
        private String aggregationMethod;
        private Integer timeWindowHours;
        private Double spatialResolutionMeters;
        private String filterCriteria;
        private Integer minZoom;
        private Integer maxZoom;
        private Integer tileSize;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder heatmapType(HeatmapType heatmapType) {
            this.heatmapType = heatmapType;
            return this;
        }

        public Builder colorScheme(String colorScheme) {
            this.colorScheme = colorScheme;
            return this;
        }

        public Builder intensityRangeMin(Double intensityRangeMin) {
            this.intensityRangeMin = intensityRangeMin;
            return this;
        }

        public Builder intensityRangeMax(Double intensityRangeMax) {
            this.intensityRangeMax = intensityRangeMax;
            return this;
        }

        public Builder radiusMultiplier(Double radiusMultiplier) {
            this.radiusMultiplier = radiusMultiplier;
            return this;
        }

        public Builder opacity(Double opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder blurRadius(Double blurRadius) {
            this.blurRadius = blurRadius;
            return this;
        }

        public Builder gradientStops(String gradientStops) {
            this.gradientStops = gradientStops;
            return this;
        }

        public Builder aggregationMethod(String aggregationMethod) {
            this.aggregationMethod = aggregationMethod;
            return this;
        }

        public Builder timeWindowHours(Integer timeWindowHours) {
            this.timeWindowHours = timeWindowHours;
            return this;
        }

        public Builder spatialResolutionMeters(Double spatialResolutionMeters) {
            this.spatialResolutionMeters = spatialResolutionMeters;
            return this;
        }

        public Builder filterCriteria(String filterCriteria) {
            this.filterCriteria = filterCriteria;
            return this;
        }

        public Builder minZoom(Integer minZoom) {
            this.minZoom = minZoom;
            return this;
        }

        public Builder maxZoom(Integer maxZoom) {
            this.maxZoom = maxZoom;
            return this;
        }

        public Builder tileSize(Integer tileSize) {
            this.tileSize = tileSize;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public HeatmapConfiguration build() {
            HeatmapConfiguration config = new HeatmapConfiguration();
            config.setId(id);
            config.setName(name);
            config.setDescription(description);
            config.setHeatmapType(heatmapType);
            config.setColorScheme(colorScheme);
            config.setIntensityRangeMin(intensityRangeMin);
            config.setIntensityRangeMax(intensityRangeMax);
            config.setRadiusMultiplier(radiusMultiplier);
            config.setOpacity(opacity);
            config.setBlurRadius(blurRadius);
            config.setGradientStops(gradientStops);
            config.setAggregationMethod(aggregationMethod);
            config.setTimeWindowHours(timeWindowHours);
            config.setSpatialResolutionMeters(spatialResolutionMeters);
            config.setFilterCriteria(filterCriteria);
            config.setMinZoom(minZoom);
            config.setMaxZoom(maxZoom);
            config.setTileSize(tileSize);
            config.setIsActive(isActive);
            config.setCreatedAt(createdAt);
            config.setUpdatedAt(updatedAt);
            return config;
        }
    }
}



