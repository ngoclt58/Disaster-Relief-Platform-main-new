package com.relief.repository.indoor;

import com.relief.domain.indoor.IndoorPosition;
import com.relief.domain.indoor.PositioningMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for indoor positions
 */
@Repository
public interface IndoorPositionRepository extends JpaRepository<IndoorPosition, Long> {
    
    /**
     * Find positions by indoor map
     */
    List<IndoorPosition> findByIndoorMapId(Long indoorMapId);
    
    /**
     * Find positions by entity
     */
    List<IndoorPosition> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    /**
     * Find positions by entity type
     */
    List<IndoorPosition> findByEntityType(String entityType);
    
    /**
     * Find positions by positioning method
     */
    List<IndoorPosition> findByPositioningMethod(PositioningMethod positioningMethod);
    
    /**
     * Find valid positions
     */
    List<IndoorPosition> findByIsValidTrue();
    
    /**
     * Find positions by floor level
     */
    List<IndoorPosition> findByFloorLevel(Integer floorLevel);
    
    /**
     * Find positions by map and floor
     */
    List<IndoorPosition> findByIndoorMapIdAndFloorLevel(Long indoorMapId, Integer floorLevel);
    
    /**
     * Find positions by map and positioning method
     */
    List<IndoorPosition> findByIndoorMapIdAndPositioningMethod(Long indoorMapId, PositioningMethod positioningMethod);
    
    /**
     * Find positions by map, floor and positioning method
     */
    List<IndoorPosition> findByIndoorMapIdAndFloorLevelAndPositioningMethod(Long indoorMapId, Integer floorLevel, PositioningMethod positioningMethod);
    
    /**
     * Find positions by map and entity
     */
    List<IndoorPosition> findByIndoorMapIdAndEntityTypeAndEntityId(Long indoorMapId, String entityType, Long entityId);
    
    /**
     * Find latest position for an entity
     */
    @Query("SELECT p FROM IndoorPosition p WHERE p.entityType = :entityType AND p.entityId = :entityId ORDER BY p.timestamp DESC")
    List<IndoorPosition> findLatestPositionByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    /**
     * Find positions within bounds
     */
    @Query(value = """
        SELECT * FROM indoor_positions 
        WHERE ST_Within(position, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND indoor_map_id = :indoorMapId
        ORDER BY timestamp DESC
        """, nativeQuery = true)
    List<IndoorPosition> findWithinBounds(
        @Param("indoorMapId") Long indoorMapId,
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find positions near a point
     */
    @Query(value = """
        SELECT * FROM indoor_positions 
        WHERE ST_DWithin(position, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius)
        AND indoor_map_id = :indoorMapId
        ORDER BY ST_Distance(position, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
        """, nativeQuery = true)
    List<IndoorPosition> findNearPoint(
        @Param("indoorMapId") Long indoorMapId,
        @Param("longitude") double longitude,
        @Param("latitude") double latitude,
        @Param("radius") double radius
    );
    
    /**
     * Find positions by date range
     */
    @Query("SELECT p FROM IndoorPosition p WHERE p.timestamp BETWEEN :startDate AND :endDate ORDER BY p.timestamp DESC")
    List<IndoorPosition> findByTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find positions by map and date range
     */
    @Query("SELECT p FROM IndoorPosition p WHERE p.indoorMap.id = :indoorMapId AND p.timestamp BETWEEN :startDate AND :endDate ORDER BY p.timestamp DESC")
    List<IndoorPosition> findByIndoorMapIdAndTimestampBetween(
        @Param("indoorMapId") Long indoorMapId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get position statistics for a map
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_positions,
            COUNT(CASE WHEN is_valid = true THEN 1 END) as valid_positions,
            COUNT(CASE WHEN positioning_method = 'WIFI_FINGERPRINTING' THEN 1 END) as wifi_positions,
            COUNT(CASE WHEN positioning_method = 'BLUETOOTH_BEACONS' THEN 1 END) as bluetooth_positions,
            COUNT(CASE WHEN positioning_method = 'UWB' THEN 1 END) as uwb_positions,
            AVG(accuracy) as avg_accuracy,
            AVG(speed) as avg_speed
        FROM indoor_positions 
        WHERE indoor_map_id = :indoorMapId
        AND timestamp BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    IndoorPositionStatistics getPositionStatistics(
        @Param("indoorMapId") Long indoorMapId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for indoor position statistics projection
     */
    interface IndoorPositionStatistics {
        Long getTotalPositions();
        Long getValidPositions();
        Long getWifiPositions();
        Long getBluetoothPositions();
        Long getUwbPositions();
        Double getAvgAccuracy();
        Double getAvgSpeed();
    }
}