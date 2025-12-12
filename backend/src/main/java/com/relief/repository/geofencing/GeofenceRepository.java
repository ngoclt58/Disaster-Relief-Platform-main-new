package com.relief.repository.geofencing;

import com.relief.domain.geofencing.Geofence;
import com.relief.domain.geofencing.GeofencePriority;
import com.relief.domain.geofencing.GeofenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for geofence data
 */
@Repository
public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
    
    /**
     * Find geofences containing a point
     */
    @Query(value = """
        SELECT * FROM geofences 
        WHERE ST_Contains(boundary, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
        AND is_active = true
        ORDER BY priority DESC, created_at DESC
        """, nativeQuery = true)
    List<Geofence> findContainingPoint(@Param("longitude") double longitude, @Param("latitude") double latitude);
    
    /**
     * Find geofences intersecting with a geometry
     */
    @Query(value = """
        SELECT * FROM geofences 
        WHERE ST_Intersects(boundary, :geometry)
        AND is_active = true
        ORDER BY priority DESC, created_at DESC
        """, nativeQuery = true)
    List<Geofence> findIntersecting(@Param("geometry") String geometry);
    
    /**
     * Find geofences by type
     */
    List<Geofence> findByGeofenceType(GeofenceType geofenceType);
    
    /**
     * Find active geofences by type
     */
    List<Geofence> findByGeofenceTypeAndIsActiveTrue(GeofenceType geofenceType);
    
    /**
     * Find geofences by priority
     */
    List<Geofence> findByPriority(GeofencePriority priority);
    
    /**
     * Find active geofences by priority
     */
    List<Geofence> findByPriorityAndIsActiveTrue(GeofencePriority priority);
    
    /**
     * Find active geofences
     */
    List<Geofence> findByIsActiveTrue();
    
    /**
     * Find geofences by creator
     */
    List<Geofence> findByCreatedBy(String createdBy);
    
    /**
     * Find geofences by name containing
     */
    List<Geofence> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find geofences that need checking
     */
    @Query("SELECT g FROM Geofence g WHERE g.isActive = true AND " +
           "(g.lastCheckedAt IS NULL OR g.lastCheckedAt < :checkTime)")
    List<Geofence> findNeedingCheck(@Param("checkTime") LocalDateTime checkTime);
    
    /**
     * Find geofences within bounds
     */
    @Query(value = """
        SELECT * FROM geofences 
        WHERE ST_Intersects(boundary, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND is_active = true
        ORDER BY priority DESC, created_at DESC
        """, nativeQuery = true)
    List<Geofence> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find geofences by type and bounds
     */
    @Query(value = """
        SELECT * FROM geofences 
        WHERE geofence_type = :geofenceType
        AND ST_Intersects(boundary, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND is_active = true
        ORDER BY priority DESC, created_at DESC
        """, nativeQuery = true)
    List<Geofence> findByTypeAndBounds(
        @Param("geofenceType") String geofenceType,
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Get geofence statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_geofences,
            COUNT(CASE WHEN is_active = true THEN 1 END) as active_geofences,
            COUNT(CASE WHEN geofence_type = 'DISASTER_ZONE' THEN 1 END) as disaster_zones,
            COUNT(CASE WHEN geofence_type = 'EVACUATION_ZONE' THEN 1 END) as evacuation_zones,
            COUNT(CASE WHEN geofence_type = 'RESOURCE_DEPOT' THEN 1 END) as resource_depots,
            COUNT(CASE WHEN priority = 'CRITICAL' THEN 1 END) as critical_geofences,
            COUNT(CASE WHEN priority = 'HIGH' THEN 1 END) as high_priority_geofences,
            AVG(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - last_checked_at))/60) as avg_minutes_since_last_check
        FROM geofences 
        WHERE created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    GeofenceStatistics getStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for geofence statistics projection
     */
    interface GeofenceStatistics {
        Long getTotalGeofences();
        Long getActiveGeofences();
        Long getDisasterZones();
        Long getEvacuationZones();
        Long getResourceDepots();
        Long getCriticalGeofences();
        Long getHighPriorityGeofences();
        Double getAvgMinutesSinceLastCheck();
    }
}



