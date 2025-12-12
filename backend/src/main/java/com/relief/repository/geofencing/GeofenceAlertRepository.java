package com.relief.repository.geofencing;

import com.relief.domain.geofencing.GeofenceAlert;
import com.relief.domain.geofencing.GeofenceAlertSeverity;
import com.relief.domain.geofencing.GeofenceAlertStatus;
import com.relief.domain.geofencing.GeofenceAlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for geofence alerts
 */
@Repository
public interface GeofenceAlertRepository extends JpaRepository<GeofenceAlert, Long> {
    
    /**
     * Find alerts by geofence
     */
    List<GeofenceAlert> findByGeofenceId(Long geofenceId);
    
    /**
     * Find alerts by status
     */
    List<GeofenceAlert> findByStatus(GeofenceAlertStatus status);
    
    /**
     * Find active alerts
     */
    List<GeofenceAlert> findByStatusIn(List<GeofenceAlertStatus> statuses);
    
    /**
     * Find alerts by severity
     */
    List<GeofenceAlert> findBySeverity(GeofenceAlertSeverity severity);
    
    /**
     * Find alerts by type
     */
    List<GeofenceAlert> findByAlertType(GeofenceAlertType alertType);
    
    /**
     * Find alerts by geofence and status
     */
    List<GeofenceAlert> findByGeofenceIdAndStatus(Long geofenceId, GeofenceAlertStatus status);
    
    /**
     * Find alerts by assigned user
     */
    List<GeofenceAlert> findByAssignedTo(String assignedTo);
    
    /**
     * Find alerts by creator
     */
    List<GeofenceAlert> findByGeofenceCreatedBy(String createdBy);
    
    /**
     * Find alerts by date range
     */
    @Query("SELECT a FROM GeofenceAlert a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<GeofenceAlert> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find alerts by geofence and date range
     */
    @Query("SELECT a FROM GeofenceAlert a WHERE a.geofence.id = :geofenceId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<GeofenceAlert> findByGeofenceIdAndCreatedAtBetween(
        @Param("geofenceId") Long geofenceId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find unacknowledged alerts
     */
    @Query("SELECT a FROM GeofenceAlert a WHERE a.status = 'ACTIVE' AND a.acknowledgedAt IS NULL ORDER BY a.createdAt ASC")
    List<GeofenceAlert> findUnacknowledgedAlerts();
    
    /**
     * Find unresolved alerts
     */
    @Query("SELECT a FROM GeofenceAlert a WHERE a.status IN ('ACTIVE', 'ACKNOWLEDGED', 'IN_PROGRESS') ORDER BY a.createdAt ASC")
    List<GeofenceAlert> findUnresolvedAlerts();
    
    /**
     * Find alerts requiring escalation
     */
    @Query("SELECT a FROM GeofenceAlert a WHERE a.status = 'ACTIVE' AND a.createdAt < :escalationTime ORDER BY a.createdAt ASC")
    List<GeofenceAlert> findAlertsRequiringEscalation(@Param("escalationTime") LocalDateTime escalationTime);
    
    /**
     * Count alerts by status
     */
    @Query("SELECT COUNT(a) FROM GeofenceAlert a WHERE a.status = :status")
    Long countByStatus(@Param("status") GeofenceAlertStatus status);
    
    /**
     * Count alerts by geofence and status
     */
    @Query("SELECT COUNT(a) FROM GeofenceAlert a WHERE a.geofence.id = :geofenceId AND a.status = :status")
    Long countByGeofenceIdAndStatus(@Param("geofenceId") Long geofenceId, @Param("status") GeofenceAlertStatus status);
    
    /**
     * Count alerts by severity
     */
    @Query("SELECT COUNT(a) FROM GeofenceAlert a WHERE a.severity = :severity")
    Long countBySeverity(@Param("severity") GeofenceAlertSeverity severity);
    
    /**
     * Get alert statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_alerts,
            COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_alerts,
            COUNT(CASE WHEN status = 'ACKNOWLEDGED' THEN 1 END) as acknowledged_alerts,
            COUNT(CASE WHEN status = 'RESOLVED' THEN 1 END) as resolved_alerts,
            COUNT(CASE WHEN severity = 'CRITICAL' THEN 1 END) as critical_alerts,
            COUNT(CASE WHEN severity = 'HIGH' THEN 1 END) as high_severity_alerts,
            COUNT(CASE WHEN acknowledged_at IS NULL THEN 1 END) as unacknowledged_alerts,
            AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as avg_resolution_hours
        FROM geofence_alerts 
        WHERE created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    GeofenceAlertStatistics getAlertStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get alert statistics for a geofence
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_alerts,
            COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_alerts,
            COUNT(CASE WHEN status = 'ACKNOWLEDGED' THEN 1 END) as acknowledged_alerts,
            COUNT(CASE WHEN status = 'RESOLVED' THEN 1 END) as resolved_alerts,
            COUNT(CASE WHEN severity = 'CRITICAL' THEN 1 END) as critical_alerts,
            COUNT(CASE WHEN severity = 'HIGH' THEN 1 END) as high_severity_alerts,
            COUNT(CASE WHEN acknowledged_at IS NULL THEN 1 END) as unacknowledged_alerts,
            AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as avg_resolution_hours
        FROM geofence_alerts 
        WHERE geofence_id = :geofenceId
        AND created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    GeofenceAlertStatistics getAlertStatisticsForGeofence(
        @Param("geofenceId") Long geofenceId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for geofence alert statistics projection
     */
    interface GeofenceAlertStatistics {
        Long getTotalAlerts();
        Long getActiveAlerts();
        Long getAcknowledgedAlerts();
        Long getResolvedAlerts();
        Long getCriticalAlerts();
        Long getHighSeverityAlerts();
        Long getUnacknowledgedAlerts();
        Double getAvgResolutionHours();
    }
}



