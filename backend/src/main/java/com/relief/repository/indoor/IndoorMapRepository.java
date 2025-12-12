package com.relief.repository.indoor;

import com.relief.domain.indoor.IndoorMap;
import com.relief.domain.indoor.IndoorMapType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for indoor maps
 */
@Repository
public interface IndoorMapRepository extends JpaRepository<IndoorMap, Long> {
    
    /**
     * Find indoor maps by facility ID
     */
    List<IndoorMap> findByFacilityId(Long facilityId);
    
    /**
     * Find indoor maps by facility name
     */
    List<IndoorMap> findByFacilityName(String facilityName);
    
    /**
     * Find indoor maps by type
     */
    List<IndoorMap> findByMapType(IndoorMapType mapType);
    
    /**
     * Find active indoor maps
     */
    List<IndoorMap> findByIsActiveTrue();
    
    /**
     * Find indoor maps by floor number
     */
    List<IndoorMap> findByFloorNumber(Integer floorNumber);
    
    /**
     * Find indoor maps by facility and floor
     */
    List<IndoorMap> findByFacilityIdAndFloorNumber(Long facilityId, Integer floorNumber);
    
    /**
     * Find indoor maps by creator
     */
    List<IndoorMap> findByCreatedBy(String createdBy);
    
    /**
     * Find indoor maps by name containing
     */
    List<IndoorMap> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find indoor maps within bounds
     */
    @Query(value = """
        SELECT * FROM indoor_maps 
        WHERE ST_Intersects(map_bounds, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND is_active = true
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<IndoorMap> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find indoor maps by type and bounds
     */
    @Query(value = """
        SELECT * FROM indoor_maps 
        WHERE map_type = :mapType
        AND ST_Intersects(map_bounds, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND is_active = true
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<IndoorMap> findByTypeAndBounds(
        @Param("mapType") String mapType,
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find indoor maps containing a point
     */
    @Query(value = """
        SELECT * FROM indoor_maps 
        WHERE ST_Contains(map_bounds, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
        AND is_active = true
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<IndoorMap> findContainingPoint(@Param("longitude") double longitude, @Param("latitude") double latitude);
    
    /**
     * Get indoor map statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_maps,
            COUNT(CASE WHEN is_active = true THEN 1 END) as active_maps,
            COUNT(CASE WHEN map_type = 'EMERGENCY_SHELTER' THEN 1 END) as emergency_shelters,
            COUNT(CASE WHEN map_type = 'HOSPITAL' THEN 1 END) as hospitals,
            COUNT(CASE WHEN map_type = 'WAREHOUSE' THEN 1 END) as warehouses,
            COUNT(CASE WHEN map_type = 'OFFICE_BUILDING' THEN 1 END) as office_buildings,
            AVG(scale_factor) as avg_scale_factor
        FROM indoor_maps 
        WHERE created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    IndoorMapStatistics getStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for indoor map statistics projection
     */
    interface IndoorMapStatistics {
        Long getTotalMaps();
        Long getActiveMaps();
        Long getEmergencyShelters();
        Long getHospitals();
        Long getWarehouses();
        Long getOfficeBuildings();
        Double getAvgScaleFactor();
    }
}



