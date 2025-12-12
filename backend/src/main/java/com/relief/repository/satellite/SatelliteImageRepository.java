package com.relief.repository.satellite;

import com.relief.domain.satellite.ProcessingStatus;
import com.relief.domain.satellite.SatelliteImage;
import com.relief.domain.satellite.SatelliteProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for satellite imagery data
 */
@Repository
public interface SatelliteImageRepository extends JpaRepository<SatelliteImage, Long> {
    
    /**
     * Find satellite images within a bounding box
     */
    @Query(value = """
        SELECT * FROM satellite_images 
        WHERE ST_Intersects(coverage_area, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        ORDER BY captured_at DESC
        """, nativeQuery = true)
    List<SatelliteImage> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find satellite images by provider
     */
    List<SatelliteImage> findByProvider(SatelliteProvider provider);
    
    /**
     * Find satellite images by processing status
     */
    List<SatelliteImage> findByProcessingStatus(ProcessingStatus status);
    
    /**
     * Find satellite images captured within a date range
     */
    @Query("SELECT si FROM SatelliteImage si WHERE si.capturedAt BETWEEN :startDate AND :endDate ORDER BY si.capturedAt DESC")
    List<SatelliteImage> findByCapturedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find satellite images by resolution range
     */
    @Query("SELECT si FROM SatelliteImage si WHERE si.resolutionMeters BETWEEN :minResolution AND :maxResolution ORDER BY si.resolutionMeters")
    List<SatelliteImage> findByResolutionMetersBetween(
        @Param("minResolution") double minResolution,
        @Param("maxResolution") double maxResolution
    );
    
    /**
     * Find satellite images with low cloud cover
     */
    @Query("SELECT si FROM SatelliteImage si WHERE si.cloudCoverPercentage <= :maxCloudCover ORDER BY si.cloudCoverPercentage")
    List<SatelliteImage> findByCloudCoverPercentageLessThanEqual(
        @Param("maxCloudCover") double maxCloudCover
    );
    
    /**
     * Find the most recent satellite image for an area
     */
    @Query(value = """
        SELECT * FROM satellite_images 
        WHERE ST_Intersects(coverage_area, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
        ORDER BY captured_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<SatelliteImage> findMostRecentForPoint(@Param("lon") double lon, @Param("lat") double lat);
    
    /**
     * Find satellite images by quality score
     */
    @Query("SELECT si FROM SatelliteImage si WHERE si.qualityScore >= :minQuality ORDER BY si.qualityScore DESC")
    List<SatelliteImage> findByQualityScoreGreaterThanEqual(
        @Param("minQuality") double minQuality
    );
    
    /**
     * Find satellite images for change detection (before and after)
     */
    @Query(value = """
        SELECT * FROM satellite_images 
        WHERE ST_Intersects(coverage_area, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND captured_at BETWEEN :startDate AND :endDate
        AND processing_status = 'COMPLETED'
        ORDER BY captured_at ASC
        """, nativeQuery = true)
    List<SatelliteImage> findForChangeDetection(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find satellite images with pagination
     */
    Page<SatelliteImage> findByProcessingStatusOrderByCapturedAtDesc(
        ProcessingStatus status, Pageable pageable
    );
    
    /**
     * Count satellite images by provider
     */
    @Query("SELECT si.provider, COUNT(si) FROM SatelliteImage si GROUP BY si.provider")
    List<Object[]> countByProvider();
    
    /**
     * Get satellite image statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_images,
            AVG(resolution_meters) as avg_resolution,
            AVG(cloud_cover_percentage) as avg_cloud_cover,
            AVG(quality_score) as avg_quality,
            MIN(captured_at) as earliest_capture,
            MAX(captured_at) as latest_capture
        FROM satellite_images 
        WHERE captured_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    SatelliteImageStatistics getStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for satellite image statistics projection
     */
    interface SatelliteImageStatistics {
        Long getTotalImages();
        Double getAvgResolution();
        Double getAvgCloudCover();
        Double getAvgQuality();
        LocalDateTime getEarliestCapture();
        LocalDateTime getLatestCapture();
    }
}



