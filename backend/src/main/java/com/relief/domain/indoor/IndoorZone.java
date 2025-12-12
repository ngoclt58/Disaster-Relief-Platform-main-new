package com.relief.domain.indoor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;

/**
 * Represents a zone or area within an indoor map
 */
@Entity
@Table(name = "indoor_zones", indexes = {
    @Index(name = "idx_indoor_zone_map", columnList = "indoor_map_id"),
    @Index(name = "idx_indoor_zone_type", columnList = "zone_type"),
    @Index(name = "idx_indoor_zone_geom", columnList = "geometry"),
    @Index(name = "idx_indoor_zone_accessible", columnList = "is_accessible")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorZone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indoor_map_id", nullable = false)
    private IndoorMap indoorMap;
    
    @Column(name = "zone_id", nullable = false)
    private String zoneId; // Unique identifier within the map
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "geometry", nullable = false, columnDefinition = "geometry(Geometry, 4326)")
    private Geometry geometry;
    
    @Column(name = "zone_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private IndoorZoneType zoneType;
    
    @Column(name = "is_accessible", nullable = false)
    private Boolean isAccessible;
    
    @Column(name = "capacity")
    private Integer capacity; // Maximum capacity for the zone
    
    @Column(name = "current_occupancy")
    private Integer currentOccupancy; // Current number of people
    
    @Column(name = "is_restricted", nullable = false)
    private Boolean isRestricted;
    
    @Column(name = "restriction_type")
    private String restrictionType; // Type of restriction
    
    @Column(name = "access_level")
    private String accessLevel; // Required access level
    
    @Column(name = "is_emergency_shelter", nullable = false)
    private Boolean isEmergencyShelter;
    
    @Column(name = "is_medical_facility", nullable = false)
    private Boolean isMedicalFacility;
    
    @Column(name = "is_evacuation_zone", nullable = false)
    private Boolean isEvacuationZone;
    
    @Column(name = "floor_level")
    private Integer floorLevel; // Floor level for multi-story buildings
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional zone data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isAccessible == null) {
            isAccessible = true;
        }
        if (isRestricted == null) {
            isRestricted = false;
        }
        if (isEmergencyShelter == null) {
            isEmergencyShelter = false;
        }
        if (isMedicalFacility == null) {
            isMedicalFacility = false;
        }
        if (isEvacuationZone == null) {
            isEvacuationZone = false;
        }
        if (currentOccupancy == null) {
            currentOccupancy = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



