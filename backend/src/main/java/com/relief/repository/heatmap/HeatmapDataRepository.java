package com.relief.repository.heatmap;

import com.relief.domain.heatmap.HeatmapData;
import com.relief.domain.heatmap.HeatmapType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for heatmap data points
 */
@Repository
public interface HeatmapDataRepository extends JpaRepository<HeatmapData, Long> {
    
    /**
     * Find heatmap data within a bounding box
     */
    @Query(value = """
        SELECT * FROM heatmap_data 
        WHERE ST_Within(location, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        ORDER BY intensity DESC
        """, nativeQuery = true)
    List<HeatmapData> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find heatmap data by type
     */
    List<HeatmapData> findByHeatmapType(HeatmapType heatmapType);
    
    /**
     * Find heatmap data by type and bounds
     */
    @Query(value = """
        SELECT * FROM heatmap_data 
        WHERE heatmap_type = :heatmapType
        AND ST_Within(location, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        ORDER BY intensity DESC
        """, nativeQuery = true)
    List<HeatmapData> findByTypeAndBounds(
        @Param("heatmapType") String heatmapType,
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find heatmap data by category
     */
    List<HeatmapData> findByCategory(String category);
    
    /**
     * Find heatmap data by intensity range
     */
    @Query("SELECT h FROM HeatmapData h WHERE h.intensity BETWEEN :minIntensity AND :maxIntensity ORDER BY h.intensity DESC")
    List<HeatmapData> findByIntensityRange(
        @Param("minIntensity") double minIntensity,
        @Param("maxIntensity") double maxIntensity
    );
    
    /**
     * Find heatmap data by date range
     */
    @Query("SELECT h FROM HeatmapData h WHERE h.createdAt BETWEEN :startDate AND :endDate ORDER BY h.createdAt DESC")
    List<HeatmapData> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find heatmap data by source
     */
    List<HeatmapData> findBySourceIdAndSourceType(Long sourceId, String sourceType);
    
    /**
     * Get heatmap statistics for a type
     */
    @Query(value = """
        SELECT 
            COUNT(*) as point_count,
            AVG(intensity) as avg_intensity,
            MIN(intensity) as min_intensity,
            MAX(intensity) as max_intensity,
            STDDEV(intensity) as intensity_stddev,
            AVG(weight) as avg_weight,
            AVG(radius) as avg_radius
        FROM heatmap_data 
        WHERE heatmap_type = :heatmapType
        AND created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    HeatmapStatistics getStatisticsForType(
        @Param("heatmapType") String heatmapType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get heatmap statistics for bounds
     */
    @Query(value = """
        SELECT 
            COUNT(*) as point_count,
            AVG(intensity) as avg_intensity,
            MIN(intensity) as min_intensity,
            MAX(intensity) as max_intensity,
            STDDEV(intensity) as intensity_stddev,
            AVG(weight) as avg_weight,
            AVG(radius) as avg_radius
        FROM heatmap_data 
        WHERE ST_Within(location, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    HeatmapStatistics getStatisticsForBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get aggregated heatmap data for visualization
     */
    @Query(value = """
        SELECT 
            ST_X(location) as longitude,
            ST_Y(location) as latitude,
            intensity,
            weight,
            radius,
            category
        FROM heatmap_data 
        WHERE heatmap_type = :heatmapType
        AND ST_Within(location, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND created_at BETWEEN :startDate AND :endDate
        ORDER BY intensity DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getAggregatedData(
        @Param("heatmapType") String heatmapType,
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("limit") int limit
    );
    
    /**
     * Interface for heatmap statistics projection
     */
    interface HeatmapStatistics {
        Long getPointCount();
        Double getAvgIntensity();
        Double getMinIntensity();
        Double getMaxIntensity();
        Double getIntensityStddev();
        Double getAvgWeight();
        Double getAvgRadius();
    }
}



