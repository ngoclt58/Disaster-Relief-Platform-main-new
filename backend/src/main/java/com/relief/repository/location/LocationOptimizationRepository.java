package com.relief.repository.location;

import com.relief.domain.location.LocationOptimization;
import com.relief.domain.location.OptimizationType;
import com.relief.domain.location.OptimizationPriority;
import com.relief.domain.location.OptimizationStatus;
import com.relief.domain.location.ImplementationDifficulty;
import com.relief.domain.location.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for location optimizations
 */
@Repository
public interface LocationOptimizationRepository extends JpaRepository<LocationOptimization, Long> {
    
    /**
     * Find optimizations by pattern
     */
    List<LocationOptimization> findByLocationPatternId(Long patternId);
    
    /**
     * Find optimizations by optimization type
     */
    List<LocationOptimization> findByOptimizationType(OptimizationType optimizationType);
    
    /**
     * Find optimizations by priority
     */
    List<LocationOptimization> findByPriority(OptimizationPriority priority);
    
    /**
     * Find optimizations by status
     */
    List<LocationOptimization> findByStatus(OptimizationStatus status);
    
    /**
     * Find optimizations by implementation difficulty
     */
    List<LocationOptimization> findByImplementationDifficulty(ImplementationDifficulty difficulty);
    
    /**
     * Find optimizations by risk level
     */
    List<LocationOptimization> findByRiskLevel(RiskLevel riskLevel);
    
    /**
     * Find implemented optimizations
     */
    List<LocationOptimization> findByIsImplementedTrue();
    
    /**
     * Find optimizations by pattern and type
     */
    List<LocationOptimization> findByLocationPatternIdAndOptimizationType(Long patternId, OptimizationType optimizationType);
    
    /**
     * Find optimizations by pattern and status
     */
    List<LocationOptimization> findByLocationPatternIdAndStatus(Long patternId, OptimizationStatus status);
    
    /**
     * Find optimizations by efficiency range
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.currentEfficiency BETWEEN :minEfficiency AND :maxEfficiency")
    List<LocationOptimization> findByCurrentEfficiencyRange(@Param("minEfficiency") double minEfficiency, @Param("maxEfficiency") double maxEfficiency);
    
    /**
     * Find optimizations by projected efficiency range
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.projectedEfficiency BETWEEN :minEfficiency AND :maxEfficiency")
    List<LocationOptimization> findByProjectedEfficiencyRange(@Param("minEfficiency") double minEfficiency, @Param("maxEfficiency") double maxEfficiency);
    
    /**
     * Find optimizations by time savings range
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.timeSavingsSeconds BETWEEN :minTime AND :maxTime")
    List<LocationOptimization> findByTimeSavingsRange(@Param("minTime") long minTime, @Param("maxTime") long maxTime);
    
    /**
     * Find optimizations by distance savings range
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.distanceSavingsMeters BETWEEN :minDistance AND :maxDistance")
    List<LocationOptimization> findByDistanceSavingsRange(@Param("minDistance") double minDistance, @Param("maxDistance") double maxDistance);
    
    /**
     * Find optimizations by cost-benefit ratio range
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.costBenefitRatio BETWEEN :minRatio AND :maxRatio")
    List<LocationOptimization> findByCostBenefitRatioRange(@Param("minRatio") double minRatio, @Param("maxRatio") double maxRatio);
    
    /**
     * Find optimizations by implementation date range
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.implementationDate BETWEEN :startDate AND :endDate")
    List<LocationOptimization> findByImplementationDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find optimizations by creation date range
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.createdAt BETWEEN :startDate AND :endDate")
    List<LocationOptimization> findByCreatedAtRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find high-priority optimizations
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.priority IN ('CRITICAL', 'HIGH') AND lo.status = 'PENDING' ORDER BY lo.priority DESC, lo.createdAt ASC")
    List<LocationOptimization> findHighPriorityPending();
    
    /**
     * Find optimizations ready for implementation
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.status = 'APPROVED' AND lo.implementationDifficulty IN ('VERY_EASY', 'EASY') ORDER BY lo.priority DESC")
    List<LocationOptimization> findReadyForImplementation();
    
    /**
     * Find optimizations by actual efficiency gain range
     */
    @Query("SELECT lo FROM LocationOptimization lo WHERE lo.actualEfficiencyGain BETWEEN :minGain AND :maxGain")
    List<LocationOptimization> findByActualEfficiencyGainRange(@Param("minGain") double minGain, @Param("maxGain") double maxGain);
    
    /**
     * Get optimization statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_optimizations,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_optimizations,
            COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_optimizations,
            COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress_optimizations,
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_optimizations,
            COUNT(CASE WHEN status = 'IMPLEMENTED' THEN 1 END) as implemented_optimizations,
            COUNT(CASE WHEN is_implemented = true THEN 1 END) as implemented_count,
            AVG(current_efficiency) as avg_current_efficiency,
            AVG(projected_efficiency) as avg_projected_efficiency,
            AVG(COALESCE(actual_efficiency_gain, 0)) as avg_actual_efficiency_gain,
            SUM(COALESCE(time_savings_seconds, 0)) as total_time_savings,
            SUM(COALESCE(distance_savings_meters, 0)) as total_distance_savings
        FROM location_optimizations 
        WHERE created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    LocationOptimizationStatistics getOptimizationStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get optimization statistics by type
     */
    @Query(value = """
        SELECT 
            optimization_type,
            COUNT(*) as optimization_count,
            AVG(current_efficiency) as avg_current_efficiency,
            AVG(projected_efficiency) as avg_projected_efficiency,
            AVG(COALESCE(actual_efficiency_gain, 0)) as avg_actual_efficiency_gain,
            COUNT(CASE WHEN is_implemented = true THEN 1 END) as implemented_count
        FROM location_optimizations 
        WHERE created_at BETWEEN :startDate AND :endDate
        GROUP BY optimization_type
        ORDER BY optimization_count DESC
        """, nativeQuery = true)
    List<LocationOptimizationTypeStatistics> getOptimizationStatisticsByType(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get optimization statistics by priority
     */
    @Query(value = """
        SELECT 
            priority,
            COUNT(*) as optimization_count,
            AVG(current_efficiency) as avg_current_efficiency,
            AVG(projected_efficiency) as avg_projected_efficiency,
            COUNT(CASE WHEN is_implemented = true THEN 1 END) as implemented_count,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count
        FROM location_optimizations 
        WHERE created_at BETWEEN :startDate AND :endDate
        GROUP BY priority
        ORDER BY 
            CASE priority 
                WHEN 'CRITICAL' THEN 1 
                WHEN 'HIGH' THEN 2 
                WHEN 'MEDIUM' THEN 3 
                WHEN 'LOW' THEN 4 
                WHEN 'BACKGROUND' THEN 5 
            END
        """, nativeQuery = true)
    List<LocationOptimizationPriorityStatistics> getOptimizationStatisticsByPriority(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for optimization statistics projection
     */
    interface LocationOptimizationStatistics {
        Long getTotalOptimizations();
        Long getPendingOptimizations();
        Long getApprovedOptimizations();
        Long getInProgressOptimizations();
        Long getCompletedOptimizations();
        Long getImplementedOptimizations();
        Long getImplementedCount();
        Double getAvgCurrentEfficiency();
        Double getAvgProjectedEfficiency();
        Double getAvgActualEfficiencyGain();
        Long getTotalTimeSavings();
        Double getTotalDistanceSavings();
    }
    
    /**
     * Interface for optimization type statistics projection
     */
    interface LocationOptimizationTypeStatistics {
        String getOptimizationType();
        Long getOptimizationCount();
        Double getAvgCurrentEfficiency();
        Double getAvgProjectedEfficiency();
        Double getAvgActualEfficiencyGain();
        Long getImplementedCount();
    }
    
    /**
     * Interface for optimization priority statistics projection
     */
    interface LocationOptimizationPriorityStatistics {
        String getPriority();
        Long getOptimizationCount();
        Double getAvgCurrentEfficiency();
        Double getAvgProjectedEfficiency();
        Long getImplementedCount();
        Long getPendingCount();
    }
}



