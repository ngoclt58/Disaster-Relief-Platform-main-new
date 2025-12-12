package com.relief.repository.location;

import com.relief.domain.location.LocationPattern;
import com.relief.domain.location.PatternType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for location patterns
 */
@Repository
public interface LocationPatternRepository extends JpaRepository<LocationPattern, Long> {
    
    /**
     * Find patterns by entity
     */
    List<LocationPattern> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    /**
     * Find patterns by entity type
     */
    List<LocationPattern> findByEntityType(String entityType);
    
    /**
     * Find patterns by pattern type
     */
    List<LocationPattern> findByPatternType(PatternType patternType);
    
    /**
     * Find patterns by entity and pattern type
     */
    List<LocationPattern> findByEntityTypeAndEntityIdAndPatternType(String entityType, Long entityId, PatternType patternType);
    
    /**
     * Find recurring patterns
     */
    List<LocationPattern> findByIsRecurringTrue();
    
    /**
     * Find optimal patterns
     */
    List<LocationPattern> findByIsOptimalTrue();
    
    /**
     * Find patterns by confidence score range
     */
    @Query("SELECT lp FROM LocationPattern lp WHERE lp.confidenceScore BETWEEN :minConfidence AND :maxConfidence")
    List<LocationPattern> findByConfidenceScoreRange(@Param("minConfidence") double minConfidence, @Param("maxConfidence") double maxConfidence);
    
    /**
     * Find patterns by time range
     */
    @Query("SELECT lp FROM LocationPattern lp WHERE lp.startTime BETWEEN :startTime AND :endTime ORDER BY lp.startTime")
    List<LocationPattern> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find patterns by entity and time range
     */
    @Query("SELECT lp FROM LocationPattern lp WHERE lp.entityType = :entityType AND lp.entityId = :entityId AND lp.startTime BETWEEN :startTime AND :endTime ORDER BY lp.startTime")
    List<LocationPattern> findByEntityAndTimeRange(
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find patterns by frequency
     */
    @Query("SELECT lp FROM LocationPattern lp WHERE lp.frequency >= :minFrequency ORDER BY lp.frequency DESC")
    List<LocationPattern> findByMinFrequency(@Param("minFrequency") int minFrequency);
    
    /**
     * Find patterns by average speed range
     */
    @Query("SELECT lp FROM LocationPattern lp WHERE lp.averageSpeed BETWEEN :minSpeed AND :maxSpeed")
    List<LocationPattern> findByAverageSpeedRange(@Param("minSpeed") double minSpeed, @Param("maxSpeed") double maxSpeed);
    
    /**
     * Find patterns by distance range
     */
    @Query("SELECT lp FROM LocationPattern lp WHERE lp.distanceMeters BETWEEN :minDistance AND :maxDistance")
    List<LocationPattern> findByDistanceRange(@Param("minDistance") double minDistance, @Param("maxDistance") double maxDistance);
    
    /**
     * Get pattern statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_patterns,
            COUNT(CASE WHEN pattern_type = 'LINEAR_MOVEMENT' THEN 1 END) as linear_patterns,
            COUNT(CASE WHEN pattern_type = 'CIRCULAR_MOVEMENT' THEN 1 END) as circular_patterns,
            COUNT(CASE WHEN pattern_type = 'STATIONARY_CLUSTER' THEN 1 END) as stationary_patterns,
            COUNT(CASE WHEN pattern_type = 'COMMUTE_ROUTE' THEN 1 END) as route_patterns,
            COUNT(CASE WHEN pattern_type = 'SEARCH_GRID' THEN 1 END) as search_patterns,
            COUNT(CASE WHEN is_recurring = true THEN 1 END) as recurring_patterns,
            COUNT(CASE WHEN is_optimal = true THEN 1 END) as optimal_patterns,
            AVG(confidence_score) as avg_confidence,
            AVG(average_speed) as avg_speed,
            AVG(distance_meters) as avg_distance
        FROM location_patterns 
        WHERE created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    LocationPatternStatistics getPatternStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get pattern statistics by entity type
     */
    @Query(value = """
        SELECT 
            entity_type,
            COUNT(*) as pattern_count,
            AVG(confidence_score) as avg_confidence,
            AVG(average_speed) as avg_speed,
            AVG(distance_meters) as avg_distance,
            COUNT(CASE WHEN is_recurring = true THEN 1 END) as recurring_count
        FROM location_patterns 
        WHERE created_at BETWEEN :startDate AND :endDate
        GROUP BY entity_type
        ORDER BY pattern_count DESC
        """, nativeQuery = true)
    List<LocationPatternEntityStatistics> getPatternStatisticsByEntityType(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get pattern statistics by pattern type
     */
    @Query(value = """
        SELECT 
            pattern_type,
            COUNT(*) as pattern_count,
            AVG(confidence_score) as avg_confidence,
            AVG(average_speed) as avg_speed,
            AVG(distance_meters) as avg_distance,
            AVG(duration_seconds) as avg_duration
        FROM location_patterns 
        WHERE created_at BETWEEN :startDate AND :endDate
        GROUP BY pattern_type
        ORDER BY pattern_count DESC
        """, nativeQuery = true)
    List<LocationPatternTypeStatistics> getPatternStatisticsByType(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for pattern statistics projection
     */
    interface LocationPatternStatistics {
        Long getTotalPatterns();
        Long getLinearPatterns();
        Long getCircularPatterns();
        Long getStationaryPatterns();
        Long getRoutePatterns();
        Long getSearchPatterns();
        Long getRecurringPatterns();
        Long getOptimalPatterns();
        Double getAvgConfidence();
        Double getAvgSpeed();
        Double getAvgDistance();
    }
    
    /**
     * Interface for entity type statistics projection
     */
    interface LocationPatternEntityStatistics {
        String getEntityType();
        Long getPatternCount();
        Double getAvgConfidence();
        Double getAvgSpeed();
        Double getAvgDistance();
        Long getRecurringCount();
    }
    
    /**
     * Interface for pattern type statistics projection
     */
    interface LocationPatternTypeStatistics {
        String getPatternType();
        Long getPatternCount();
        Double getAvgConfidence();
        Double getAvgSpeed();
        Double getAvgDistance();
        Double getAvgDuration();
    }
}



