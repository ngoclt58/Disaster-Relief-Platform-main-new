package com.relief.domain.heatmap;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

/**
 * Represents a generated heatmap layer for visualization
 */
@Entity
@Table(name = "heatmap_layers", indexes = {
    @Index(name = "idx_heatmap_layer_geom", columnList = "bounds"),
    @Index(name = "idx_heatmap_layer_type", columnList = "heatmap_type"),
    @Index(name = "idx_heatmap_layer_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapLayer {
    
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
    
    @Column(name = "bounds", nullable = false, columnDefinition = "geometry(Polygon, 4326)")
    private Polygon bounds;
    
    @Column(name = "tile_url_template", nullable = false)
    private String tileUrlTemplate; // URL template for map tiles
    
    @Column(name = "min_zoom", nullable = false)
    private Integer minZoom;
    
    @Column(name = "max_zoom", nullable = false)
    private Integer maxZoom;
    
    @Column(name = "tile_size", nullable = false)
    private Integer tileSize; // Tile size in pixels
    
    @Column(name = "data_points_count", nullable = false)
    private Long dataPointsCount;
    
    @Column(name = "intensity_min", nullable = false)
    private Double intensityMin;
    
    @Column(name = "intensity_max", nullable = false)
    private Double intensityMax;
    
    @Column(name = "intensity_avg", nullable = false)
    private Double intensityAvg;
    
    @Column(name = "configuration_id")
    private Long configurationId; // Reference to heatmap configuration
    
    @Column(name = "generation_parameters", columnDefinition = "jsonb")
    private String generationParameters; // Parameters used for generation
    
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Optional expiration time
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isPublic == null) {
            isPublic = false;
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

    public Polygon getBounds() { return bounds; }
    public void setBounds(Polygon bounds) { this.bounds = bounds; }

    public String getTileUrlTemplate() { return tileUrlTemplate; }
    public void setTileUrlTemplate(String tileUrlTemplate) { this.tileUrlTemplate = tileUrlTemplate; }

    public Integer getMinZoom() { return minZoom; }
    public void setMinZoom(Integer minZoom) { this.minZoom = minZoom; }

    public Integer getMaxZoom() { return maxZoom; }
    public void setMaxZoom(Integer maxZoom) { this.maxZoom = maxZoom; }

    public Integer getTileSize() { return tileSize; }
    public void setTileSize(Integer tileSize) { this.tileSize = tileSize; }

    public Long getDataPointsCount() { return dataPointsCount; }
    public void setDataPointsCount(Long dataPointsCount) { this.dataPointsCount = dataPointsCount; }

    public Double getIntensityMin() { return intensityMin; }
    public void setIntensityMin(Double intensityMin) { this.intensityMin = intensityMin; }

    public Double getIntensityMax() { return intensityMax; }
    public void setIntensityMax(Double intensityMax) { this.intensityMax = intensityMax; }

    public Double getIntensityAvg() { return intensityAvg; }
    public void setIntensityAvg(Double intensityAvg) { this.intensityAvg = intensityAvg; }

    public Long getConfigurationId() { return configurationId; }
    public void setConfigurationId(Long configurationId) { this.configurationId = configurationId; }

    public String getGenerationParameters() { return generationParameters; }
    public void setGenerationParameters(String generationParameters) { this.generationParameters = generationParameters; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    public Boolean isPublic() { return isPublic; }
    public void setPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

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
        private Polygon bounds;
        private String tileUrlTemplate;
        private Integer minZoom;
        private Integer maxZoom;
        private Integer tileSize;
        private Long dataPointsCount;
        private Double intensityMin;
        private Double intensityMax;
        private Double intensityAvg;
        private Long configurationId;
        private String generationParameters;
        private Long fileSizeBytes;
        private Boolean isPublic;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder heatmapType(HeatmapType heatmapType) { this.heatmapType = heatmapType; return this; }
        public Builder bounds(Polygon bounds) { this.bounds = bounds; return this; }
        public Builder tileUrlTemplate(String tileUrlTemplate) { this.tileUrlTemplate = tileUrlTemplate; return this; }
        public Builder minZoom(Integer minZoom) { this.minZoom = minZoom; return this; }
        public Builder maxZoom(Integer maxZoom) { this.maxZoom = maxZoom; return this; }
        public Builder tileSize(Integer tileSize) { this.tileSize = tileSize; return this; }
        public Builder dataPointsCount(Long dataPointsCount) { this.dataPointsCount = dataPointsCount; return this; }
        public Builder intensityMin(Double intensityMin) { this.intensityMin = intensityMin; return this; }
        public Builder intensityMax(Double intensityMax) { this.intensityMax = intensityMax; return this; }
        public Builder intensityAvg(Double intensityAvg) { this.intensityAvg = intensityAvg; return this; }
        public Builder configurationId(Long configurationId) { this.configurationId = configurationId; return this; }
        public Builder generationParameters(String generationParameters) { this.generationParameters = generationParameters; return this; }
        public Builder fileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; return this; }
        public Builder isPublic(Boolean isPublic) { this.isPublic = isPublic; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public HeatmapLayer build() {
            HeatmapLayer layer = new HeatmapLayer();
            layer.setId(id);
            layer.setName(name);
            layer.setDescription(description);
            layer.setHeatmapType(heatmapType);
            layer.setBounds(bounds);
            layer.setTileUrlTemplate(tileUrlTemplate);
            layer.setMinZoom(minZoom);
            layer.setMaxZoom(maxZoom);
            layer.setTileSize(tileSize);
            layer.setDataPointsCount(dataPointsCount);
            layer.setIntensityMin(intensityMin);
            layer.setIntensityMax(intensityMax);
            layer.setIntensityAvg(intensityAvg);
            layer.setConfigurationId(configurationId);
            layer.setGenerationParameters(generationParameters);
            layer.setFileSizeBytes(fileSizeBytes);
            layer.setIsPublic(isPublic);
            layer.setExpiresAt(expiresAt);
            layer.setCreatedAt(createdAt);
            layer.setUpdatedAt(updatedAt);
            return layer;
        }
    }
}



