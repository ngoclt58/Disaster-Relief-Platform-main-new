package com.relief.repository.terrain;

import com.relief.domain.terrain.TerrainAnalysis;
import com.relief.domain.terrain.TerrainAnalysisType;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for terrain analysis data
 */
@Repository
public interface TerrainAnalysisRepository extends JpaRepository<TerrainAnalysis, Long> {
    
    /**
     * Find terrain analysis by area intersection
     */
    @Query(value = """
        SELECT * FROM terrain_analysis 
        WHERE ST_Intersects(area, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
        ORDER BY ST_Area(area) ASC
        """, nativeQuery = true)
    List<TerrainAnalysis> findByPointIntersection(@Param("lon") double lon, @Param("lat") double lat);
    
    /**
     * Find terrain analysis by area intersection with polygon
     */
    @Query(value = """
        SELECT * FROM terrain_analysis 
        WHERE ST_Intersects(area, :polygon)
        ORDER BY ST_Area(area) ASC
        """, nativeQuery = true)
    List<TerrainAnalysis> findByPolygonIntersection(@Param("polygon") Polygon polygon);
    
    /**
     * Find terrain analysis by type
     */
    List<TerrainAnalysis> findByAnalysisType(TerrainAnalysisType analysisType);
    
    /**
     * Find terrain analysis by accessibility score range
     */
    @Query("SELECT ta FROM TerrainAnalysis ta WHERE ta.accessibilityScore BETWEEN :minScore AND :maxScore")
    List<TerrainAnalysis> findByAccessibilityScoreRange(
        @Param("minScore") double minScore,
        @Param("maxScore") double maxScore
    );
    
    /**
     * Find terrain analysis by flood risk score range
     */
    @Query("SELECT ta FROM TerrainAnalysis ta WHERE ta.floodRiskScore BETWEEN :minScore AND :maxScore")
    List<TerrainAnalysis> findByFloodRiskScoreRange(
        @Param("minScore") double minScore,
        @Param("maxScore") double maxScore
    );
    
    /**
     * Find the most recent terrain analysis for a point
     */
    @Query(value = """
        SELECT * FROM terrain_analysis 
        WHERE ST_Intersects(area, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
        ORDER BY created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<TerrainAnalysis> findMostRecentForPoint(@Param("lon") double lon, @Param("lat") double lat);
}



