package com.relief.repository.satellite;

import com.relief.domain.satellite.DamageAssessment;
import com.relief.domain.satellite.DamageSeverity;
import com.relief.domain.satellite.DamageType;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for damage assessment data
 */
@Repository
public interface DamageAssessmentRepository extends JpaRepository<DamageAssessment, Long> {
    
    /**
     * Find damage assessments within a bounding box
     */
    @Query(value = """
        SELECT * FROM damage_assessments 
        WHERE ST_Intersects(damage_area, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        ORDER BY assessed_at DESC
        """, nativeQuery = true)
    List<DamageAssessment> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find damage assessments by type
     */
    List<DamageAssessment> findByDamageType(DamageType damageType);
    
    /**
     * Find damage assessments by severity
     */
    List<DamageAssessment> findBySeverity(DamageSeverity severity);
    
    /**
     * Find damage assessments by confidence score
     */
    @Query("SELECT da FROM DamageAssessment da WHERE da.confidenceScore >= :minConfidence ORDER BY da.confidenceScore DESC")
    List<DamageAssessment> findByConfidenceScoreGreaterThanEqual(
        @Param("minConfidence") double minConfidence
    );
    
    /**
     * Find damage assessments by date range
     */
    @Query("SELECT da FROM DamageAssessment da WHERE da.assessedAt BETWEEN :startDate AND :endDate ORDER BY da.assessedAt DESC")
    List<DamageAssessment> findByAssessedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find damage assessments by satellite image
     */
    List<DamageAssessment> findBySatelliteImageId(Long satelliteImageId);
    
    /**
     * Find damage assessments by severity and type
     */
    List<DamageAssessment> findBySeverityAndDamageType(DamageSeverity severity, DamageType damageType);
    
    /**
     * Find damage assessments with high confidence
     */
    @Query("SELECT da FROM DamageAssessment da WHERE da.confidenceScore >= 0.8 ORDER BY da.confidenceScore DESC")
    List<DamageAssessment> findHighConfidenceAssessments();
    
    /**
     * Find damage assessments by affected area size
     */
    @Query("SELECT da FROM DamageAssessment da WHERE da.affectedAreaSqm >= :minArea ORDER BY da.affectedAreaSqm DESC")
    List<DamageAssessment> findByAffectedAreaSqmGreaterThanEqual(
        @Param("minArea") double minArea
    );
    
    /**
     * Find damage assessments by polygon intersection
     */
    @Query(value = """
        SELECT * FROM damage_assessments 
        WHERE ST_Intersects(damage_area, :polygon)
        ORDER BY assessed_at DESC
        """, nativeQuery = true)
    List<DamageAssessment> findByPolygonIntersection(@Param("polygon") Polygon polygon);
    
    /**
     * Get damage assessment statistics
     */
    @Query(value = """
        SELECT 
            damage_type,
            severity,
            COUNT(*) as count,
            AVG(confidence_score) as avg_confidence,
            AVG(damage_percentage) as avg_damage_percentage,
            SUM(affected_area_sqm) as total_affected_area
        FROM damage_assessments 
        WHERE assessed_at BETWEEN :startDate AND :endDate
        GROUP BY damage_type, severity
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Object[]> getDamageStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get damage assessment summary by area
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_assessments,
            AVG(confidence_score) as avg_confidence,
            AVG(damage_percentage) as avg_damage_percentage,
            SUM(affected_area_sqm) as total_affected_area,
            MAX(assessed_at) as latest_assessment
        FROM damage_assessments 
        WHERE ST_Intersects(damage_area, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND assessed_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    DamageAssessmentSummary getDamageSummary(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for damage assessment summary projection
     */
    interface DamageAssessmentSummary {
        Long getTotalAssessments();
        Double getAvgConfidence();
        Double getAvgDamagePercentage();
        Double getTotalAffectedArea();
        LocalDateTime getLatestAssessment();
    }
}



