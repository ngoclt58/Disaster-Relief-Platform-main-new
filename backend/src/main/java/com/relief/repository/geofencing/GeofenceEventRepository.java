package com.relief.repository.geofencing;

import com.relief.domain.geofencing.GeofenceEvent;
import com.relief.domain.geofencing.GeofenceEventSeverity;
import com.relief.domain.geofencing.GeofenceEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for geofence events
 */
@Repository
public interface GeofenceEventRepository extends JpaRepository<GeofenceEvent, Long> {
    
    /**
     * Find events by geofence
     */
    List<GeofenceEvent> findByGeofenceId(Long geofenceId);
    
    /**
     * Find events by geofence and type
     */
    List<GeofenceEvent> findByGeofenceIdAndEventType(Long geofenceId, GeofenceEventType eventType);
    
    /**
     * Find events by type
     */
    List<GeofenceEvent> findByEventType(GeofenceEventType eventType);
    
    /**
     * Find events by severity
     */
    List<GeofenceEvent> findBySeverity(GeofenceEventSeverity severity);
    
    /**
     * Find unprocessed events
     */
    List<GeofenceEvent> findByIsProcessedFalse();
    
    /**
     * Find events by entity
     */
    List<GeofenceEvent> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    /**
     * Find events by date range
     */
    @Query("SELECT e FROM GeofenceEvent e WHERE e.occurredAt BETWEEN :startDate AND :endDate ORDER BY e.occurredAt DESC")
    List<GeofenceEvent> findByOccurredAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find events by geofence and date range
     */
    @Query("SELECT e FROM GeofenceEvent e WHERE e.geofence.id = :geofenceId AND e.occurredAt BETWEEN :startDate AND :endDate ORDER BY e.occurredAt DESC")
    List<GeofenceEvent> findByGeofenceIdAndOccurredAtBetween(
        @Param("geofenceId") Long geofenceId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find events within bounds
     */
    @Query(value = """
        SELECT * FROM geofence_events 
        WHERE ST_Within(location, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        ORDER BY occurred_at DESC
        """, nativeQuery = true)
    List<GeofenceEvent> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find recent events by geofence
     */
    @Query("SELECT e FROM GeofenceEvent e WHERE e.geofence.id = :geofenceId AND e.occurredAt >= :since ORDER BY e.occurredAt DESC")
    List<GeofenceEvent> findRecentByGeofenceId(
        @Param("geofenceId") Long geofenceId,
        @Param("since") LocalDateTime since
    );
    
    /**
     * Count events by geofence and type
     */
    @Query("SELECT COUNT(e) FROM GeofenceEvent e WHERE e.geofence.id = :geofenceId AND e.eventType = :eventType")
    Long countByGeofenceIdAndEventType(@Param("geofenceId") Long geofenceId, @Param("eventType") GeofenceEventType eventType);
    
    /**
     * Count events by geofence and severity
     */
    @Query("SELECT COUNT(e) FROM GeofenceEvent e WHERE e.geofence.id = :geofenceId AND e.severity = :severity")
    Long countByGeofenceIdAndSeverity(@Param("geofenceId") Long geofenceId, @Param("severity") GeofenceEventSeverity severity);
    
    /**
     * Get event statistics for a geofence
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_events,
            COUNT(CASE WHEN event_type = 'ENTRY' THEN 1 END) as entry_events,
            COUNT(CASE WHEN event_type = 'EXIT' THEN 1 END) as exit_events,
            COUNT(CASE WHEN event_type = 'VIOLATION' THEN 1 END) as violation_events,
            COUNT(CASE WHEN severity = 'CRITICAL' THEN 1 END) as critical_events,
            COUNT(CASE WHEN severity = 'HIGH' THEN 1 END) as high_severity_events,
            COUNT(CASE WHEN is_processed = false THEN 1 END) as unprocessed_events,
            AVG(confidence_score) as avg_confidence_score
        FROM geofence_events 
        WHERE geofence_id = :geofenceId
        AND occurred_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    GeofenceEventStatistics getEventStatistics(
        @Param("geofenceId") Long geofenceId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for geofence event statistics projection
     */
    interface GeofenceEventStatistics {
        Long getTotalEvents();
        Long getEntryEvents();
        Long getExitEvents();
        Long getViolationEvents();
        Long getCriticalEvents();
        Long getHighSeverityEvents();
        Long getUnprocessedEvents();
        Double getAvgConfidenceScore();
    }
}



