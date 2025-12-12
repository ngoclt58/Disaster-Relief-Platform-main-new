package com.relief.repository.location;

import com.relief.domain.location.LocationHistory;
import com.relief.domain.location.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for location history
 */
@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    
    /**
     * Find location history by entity
     */
    List<LocationHistory> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    /**
     * Find location history by entity type
     */
    List<LocationHistory> findByEntityType(String entityType);
    
    /**
     * Find location history by activity type
     */
    List<LocationHistory> findByActivityType(ActivityType activityType);
    
    /**
     * Find location history by entity and activity type
     */
    List<LocationHistory> findByEntityTypeAndEntityIdAndActivityType(String entityType, Long entityId, ActivityType activityType);
    
    /**
     * Find location history by time range
     */
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.timestamp BETWEEN :startTime AND :endTime ORDER BY lh.timestamp")
    List<LocationHistory> findByTimestampBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find location history by entity and time range
     */
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.entityType = :entityType AND lh.entityId = :entityId AND lh.timestamp BETWEEN :startTime AND :endTime ORDER BY lh.timestamp")
    List<LocationHistory> findByEntityAndTimestampBetween(
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find location history within bounds
     */
    @Query(value = """
        SELECT * FROM location_history 
        WHERE ST_Within(position, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND timestamp BETWEEN :startTime AND :endTime
        ORDER BY timestamp DESC
        """, nativeQuery = true)
    List<LocationHistory> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find location history near a point
     */
    @Query(value = """
        SELECT * FROM location_history 
        WHERE ST_DWithin(position, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius)
        AND timestamp BETWEEN :startTime AND :endTime
        ORDER BY ST_Distance(position, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
        """, nativeQuery = true)
    List<LocationHistory> findNearPoint(
        @Param("longitude") double longitude,
        @Param("latitude") double latitude,
        @Param("radius") double radius,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find stationary locations
     */
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.isStationary = true AND lh.timestamp BETWEEN :startTime AND :endTime")
    List<LocationHistory> findStationaryLocations(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find significant locations
     */
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.isSignificant = true AND lh.timestamp BETWEEN :startTime AND :endTime")
    List<LocationHistory> findSignificantLocations(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find locations by speed range
     */
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.speed BETWEEN :minSpeed AND :maxSpeed AND lh.timestamp BETWEEN :startTime AND :endTime")
    List<LocationHistory> findBySpeedRange(
        @Param("minSpeed") double minSpeed,
        @Param("maxSpeed") double maxSpeed,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find locations by accuracy range
     */
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.accuracy BETWEEN :minAccuracy AND :maxAccuracy AND lh.timestamp BETWEEN :startTime AND :endTime")
    List<LocationHistory> findByAccuracyRange(
        @Param("minAccuracy") double minAccuracy,
        @Param("maxAccuracy") double maxAccuracy,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Get location history statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_locations,
            COUNT(CASE WHEN is_stationary = true THEN 1 END) as stationary_locations,
            COUNT(CASE WHEN is_significant = true THEN 1 END) as significant_locations,
            AVG(speed) as avg_speed,
            MAX(speed) as max_speed,
            AVG(accuracy) as avg_accuracy,
            COUNT(DISTINCT entity_type) as unique_entity_types,
            COUNT(DISTINCT entity_id) as unique_entities
        FROM location_history 
        WHERE timestamp BETWEEN :startTime AND :endTime
        """, nativeQuery = true)
    LocationHistoryStatistics getLocationHistoryStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Get activity type statistics
     */
    @Query(value = """
        SELECT 
            activity_type,
            COUNT(*) as count,
            AVG(speed) as avg_speed,
            AVG(duration_seconds) as avg_duration,
            AVG(accuracy) as avg_accuracy
        FROM location_history 
        WHERE timestamp BETWEEN :startTime AND :endTime
        GROUP BY activity_type
        ORDER BY count DESC
        """, nativeQuery = true)
    List<ActivityTypeStatistics> getActivityTypeStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Get entity movement statistics
     */
    @Query(value = """
        SELECT 
            entity_type,
            entity_id,
            entity_name,
            COUNT(*) as location_count,
            AVG(speed) as avg_speed,
            MAX(speed) as max_speed,
            SUM(distance_from_previous) as total_distance,
            AVG(accuracy) as avg_accuracy,
            MIN(timestamp) as first_seen,
            MAX(timestamp) as last_seen
        FROM location_history 
        WHERE timestamp BETWEEN :startTime AND :endTime
        GROUP BY entity_type, entity_id, entity_name
        ORDER BY total_distance DESC
        """, nativeQuery = true)
    List<EntityMovementStatistics> getEntityMovementStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Get hourly movement patterns
     */
    @Query(value = """
        SELECT 
            EXTRACT(HOUR FROM timestamp) as hour,
            COUNT(*) as location_count,
            AVG(speed) as avg_speed,
            COUNT(CASE WHEN is_stationary = true THEN 1 END) as stationary_count,
            COUNT(CASE WHEN is_stationary = false THEN 1 END) as moving_count
        FROM location_history 
        WHERE timestamp BETWEEN :startTime AND :endTime
        GROUP BY EXTRACT(HOUR FROM timestamp)
        ORDER BY hour
        """, nativeQuery = true)
    List<HourlyMovementStatistics> getHourlyMovementStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Interface for location history statistics projection
     */
    interface LocationHistoryStatistics {
        Long getTotalLocations();
        Long getStationaryLocations();
        Long getSignificantLocations();
        Double getAvgSpeed();
        Double getMaxSpeed();
        Double getAvgAccuracy();
        Long getUniqueEntityTypes();
        Long getUniqueEntities();
    }
    
    /**
     * Interface for activity type statistics projection
     */
    interface ActivityTypeStatistics {
        String getActivityType();
        Long getCount();
        Double getAvgSpeed();
        Double getAvgDuration();
        Double getAvgAccuracy();
    }
    
    /**
     * Interface for entity movement statistics projection
     */
    interface EntityMovementStatistics {
        String getEntityType();
        Long getEntityId();
        String getEntityName();
        Long getLocationCount();
        Double getAvgSpeed();
        Double getMaxSpeed();
        Double getTotalDistance();
        Double getAvgAccuracy();
        LocalDateTime getFirstSeen();
        LocalDateTime getLastSeen();
    }
    
    /**
     * Interface for hourly movement statistics projection
     */
    interface HourlyMovementStatistics {
        Integer getHour();
        Long getLocationCount();
        Double getAvgSpeed();
        Long getStationaryCount();
        Long getMovingCount();
    }
}



