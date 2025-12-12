package com.relief.repository.heatmap;

import com.relief.domain.heatmap.HeatmapLayer;
import com.relief.domain.heatmap.HeatmapType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for heatmap layers
 */
@Repository
public interface HeatmapLayerRepository extends JpaRepository<HeatmapLayer, Long> {
    
    /**
     * Find heatmap layers by type
     */
    List<HeatmapLayer> findByHeatmapType(HeatmapType heatmapType);
    
    /**
     * Find public heatmap layers
     */
    List<HeatmapLayer> findByIsPublicTrue();
    
    /**
     * Find public heatmap layers by type
     */
    List<HeatmapLayer> findByHeatmapTypeAndIsPublicTrue(HeatmapType heatmapType);
    
    /**
     * Find heatmap layers within bounds
     */
    @Query(value = """
        SELECT * FROM heatmap_layers 
        WHERE ST_Intersects(bounds, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND is_public = true
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<HeatmapLayer> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find heatmap layers by type and bounds
     */
    @Query(value = """
        SELECT * FROM heatmap_layers 
        WHERE heatmap_type = :heatmapType
        AND ST_Intersects(bounds, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND is_public = true
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<HeatmapLayer> findByTypeAndBounds(
        @Param("heatmapType") String heatmapType,
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find heatmap layers by name
     */
    Optional<HeatmapLayer> findByName(String name);
    
    /**
     * Find heatmap layers by name containing
     */
    List<HeatmapLayer> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find heatmap layers created after date
     */
    List<HeatmapLayer> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find heatmap layers by configuration
     */
    List<HeatmapLayer> findByConfigurationId(Long configurationId);
    
    /**
     * Find non-expired heatmap layers
     */
    @Query("SELECT h FROM HeatmapLayer h WHERE h.expiresAt IS NULL OR h.expiresAt > :currentTime")
    List<HeatmapLayer> findNonExpired(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find expired heatmap layers
     */
    @Query("SELECT h FROM HeatmapLayer h WHERE h.expiresAt IS NOT NULL AND h.expiresAt <= :currentTime")
    List<HeatmapLayer> findExpired(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Get heatmap layer statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_layers,
            AVG(data_points_count) as avg_data_points,
            AVG(intensity_max) as avg_max_intensity,
            AVG(file_size_bytes) as avg_file_size,
            MIN(created_at) as earliest_created,
            MAX(created_at) as latest_created
        FROM heatmap_layers 
        WHERE created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    HeatmapLayerStatistics getLayerStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for heatmap layer statistics projection
     */
    interface HeatmapLayerStatistics {
        Long getTotalLayers();
        Double getAvgDataPoints();
        Double getAvgMaxIntensity();
        Double getAvgFileSize();
        LocalDateTime getEarliestCreated();
        LocalDateTime getLatestCreated();
    }
}



