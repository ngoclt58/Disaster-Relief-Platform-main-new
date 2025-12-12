package com.relief.domain.indoor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an indoor map for GPS-denied environments
 */
@Entity
@Table(name = "indoor_maps", indexes = {
    @Index(name = "idx_indoor_map_name", columnList = "name"),
    @Index(name = "idx_indoor_map_facility", columnList = "facility_id"),
    @Index(name = "idx_indoor_map_floor", columnList = "floor_number"),
    @Index(name = "idx_indoor_map_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorMap {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;
    
    @Column(name = "facility_name", nullable = false)
    private String facilityName;
    
    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;
    
    @Column(name = "floor_name")
    private String floorName;
    
    @Column(name = "map_bounds", nullable = false, columnDefinition = "geometry(Geometry, 4326)")
    private Geometry mapBounds;
    
    @Column(name = "map_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private IndoorMapType mapType;
    
    @Column(name = "coordinate_system", nullable = false)
    private String coordinateSystem; // e.g., "local", "utm", "custom"
    
    @Column(name = "scale_factor", nullable = false)
    private Double scaleFactor; // Pixels per meter
    
    @Column(name = "map_image_url")
    private String mapImageUrl;
    
    @Column(name = "map_data", columnDefinition = "jsonb")
    private String mapData; // JSON data for map features
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "indoorMap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndoorNode> nodes;
    
    @OneToMany(mappedBy = "indoorMap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndoorEdge> edges;
    
    @OneToMany(mappedBy = "indoorMap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndoorZone> zones;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (scaleFactor == null) {
            scaleFactor = 1.0;
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

    public Long getFacilityId() { return facilityId; }
    public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public String getFloorName() { return floorName; }
    public void setFloorName(String floorName) { this.floorName = floorName; }

    public Geometry getMapBounds() { return mapBounds; }
    public void setMapBounds(Geometry mapBounds) { this.mapBounds = mapBounds; }

    public IndoorMapType getMapType() { return mapType; }
    public void setMapType(IndoorMapType mapType) { this.mapType = mapType; }

    public String getCoordinateSystem() { return coordinateSystem; }
    public void setCoordinateSystem(String coordinateSystem) { this.coordinateSystem = coordinateSystem; }

    public Double getScaleFactor() { return scaleFactor; }
    public void setScaleFactor(Double scaleFactor) { this.scaleFactor = scaleFactor; }

    public String getMapImageUrl() { return mapImageUrl; }
    public void setMapImageUrl(String mapImageUrl) { this.mapImageUrl = mapImageUrl; }

    public String getMapData() { return mapData; }
    public void setMapData(String mapData) { this.mapData = mapData; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<IndoorNode> getNodes() { return nodes; }
    public void setNodes(List<IndoorNode> nodes) { this.nodes = nodes; }

    public List<IndoorEdge> getEdges() { return edges; }
    public void setEdges(List<IndoorEdge> edges) { this.edges = edges; }

    public List<IndoorZone> getZones() { return zones; }
    public void setZones(List<IndoorZone> zones) { this.zones = zones; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private Long facilityId;
        private String facilityName;
        private Integer floorNumber;
        private String floorName;
        private Geometry mapBounds;
        private IndoorMapType mapType;
        private String coordinateSystem;
        private Double scaleFactor;
        private String mapImageUrl;
        private String mapData;
        private Boolean isActive;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<IndoorNode> nodes;
        private List<IndoorEdge> edges;
        private List<IndoorZone> zones;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder facilityId(Long facilityId) { this.facilityId = facilityId; return this; }
        public Builder facilityName(String facilityName) { this.facilityName = facilityName; return this; }
        public Builder floorNumber(Integer floorNumber) { this.floorNumber = floorNumber; return this; }
        public Builder floorName(String floorName) { this.floorName = floorName; return this; }
        public Builder mapBounds(Geometry mapBounds) { this.mapBounds = mapBounds; return this; }
        public Builder mapType(IndoorMapType mapType) { this.mapType = mapType; return this; }
        public Builder coordinateSystem(String coordinateSystem) { this.coordinateSystem = coordinateSystem; return this; }
        public Builder scaleFactor(Double scaleFactor) { this.scaleFactor = scaleFactor; return this; }
        public Builder mapImageUrl(String mapImageUrl) { this.mapImageUrl = mapImageUrl; return this; }
        public Builder mapData(String mapData) { this.mapData = mapData; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder nodes(List<IndoorNode> nodes) { this.nodes = nodes; return this; }
        public Builder edges(List<IndoorEdge> edges) { this.edges = edges; return this; }
        public Builder zones(List<IndoorZone> zones) { this.zones = zones; return this; }

        public IndoorMap build() {
            IndoorMap map = new IndoorMap();
            map.setId(id);
            map.setName(name);
            map.setDescription(description);
            map.setFacilityId(facilityId);
            map.setFacilityName(facilityName);
            map.setFloorNumber(floorNumber);
            map.setFloorName(floorName);
            map.setMapBounds(mapBounds);
            map.setMapType(mapType);
            map.setCoordinateSystem(coordinateSystem);
            map.setScaleFactor(scaleFactor);
            map.setMapImageUrl(mapImageUrl);
            map.setMapData(mapData);
            map.setIsActive(isActive);
            map.setCreatedBy(createdBy);
            map.setCreatedAt(createdAt);
            map.setUpdatedAt(updatedAt);
            map.setNodes(nodes);
            map.setEdges(edges);
            map.setZones(zones);
            return map;
        }
    }
}



