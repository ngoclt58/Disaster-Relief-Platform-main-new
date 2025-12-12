package com.relief.repository.offline;

import com.relief.domain.offline.OfflineMapCache;
import com.relief.domain.offline.OfflineMapType;
import com.relief.domain.offline.OfflineCacheStatus;
import com.relief.domain.offline.OfflineCachePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for offline map caches
 */
@Repository
public interface OfflineMapCacheRepository extends JpaRepository<OfflineMapCache, Long> {
    
    /**
     * Find caches by region ID
     */
    List<OfflineMapCache> findByRegionId(String regionId);
    
    /**
     * Find caches by status
     */
    List<OfflineMapCache> findByStatus(OfflineCacheStatus status);
    
    /**
     * Find caches by priority
     */
    List<OfflineMapCache> findByPriority(OfflineCachePriority priority);
    
    /**
     * Find caches by map type
     */
    List<OfflineMapCache> findByMapType(OfflineMapType mapType);
    
    /**
     * Find caches by region and status
     */
    List<OfflineMapCache> findByRegionIdAndStatus(String regionId, OfflineCacheStatus status);
    
    /**
     * Find caches by region and map type
     */
    List<OfflineMapCache> findByRegionIdAndMapType(String regionId, OfflineMapType mapType);
    
    /**
     * Find caches by creator
     */
    List<OfflineMapCache> findByCreatedBy(String createdBy);
    
    /**
     * Find caches by name containing
     */
    List<OfflineMapCache> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find caches by region name containing
     */
    List<OfflineMapCache> findByRegionNameContainingIgnoreCase(String regionName);
    
    /**
     * Find caches within bounds
     */
    @Query(value = """
        SELECT * FROM offline_map_caches 
        WHERE ST_Intersects(bounds, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        ORDER BY priority DESC, created_at DESC
        """, nativeQuery = true)
    List<OfflineMapCache> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find caches containing a point
     */
    @Query(value = """
        SELECT * FROM offline_map_caches 
        WHERE ST_Contains(bounds, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
        ORDER BY priority DESC, created_at DESC
        """, nativeQuery = true)
    List<OfflineMapCache> findContainingPoint(@Param("longitude") double longitude, @Param("latitude") double latitude);
    
    /**
     * Find caches by status and priority
     */
    List<OfflineMapCache> findByStatusAndPriority(OfflineCacheStatus status, OfflineCachePriority priority);
    
    /**
     * Find caches by expiration date
     */
    @Query("SELECT c FROM OfflineMapCache c WHERE c.expiresAt < :expirationDate")
    List<OfflineMapCache> findExpiredCaches(@Param("expirationDate") LocalDateTime expirationDate);
    
    /**
     * Find caches by last accessed date
     */
    @Query("SELECT c FROM OfflineMapCache c WHERE c.lastAccessedAt < :lastAccessedDate")
    List<OfflineMapCache> findStaleCaches(@Param("lastAccessedDate") LocalDateTime lastAccessedDate);
    
    /**
     * Find caches by download progress
     */
    @Query("SELECT c FROM OfflineMapCache c WHERE c.downloadProgress < :maxProgress AND c.status = 'DOWNLOADING'")
    List<OfflineMapCache> findIncompleteDownloads(@Param("maxProgress") double maxProgress);
    
    /**
     * Find caches by size range
     */
    @Query("SELECT c FROM OfflineMapCache c WHERE c.cacheSizeBytes BETWEEN :minSize AND :maxSize")
    List<OfflineMapCache> findBySizeRange(@Param("minSize") long minSize, @Param("maxSize") long maxSize);
    
    /**
     * Get cache statistics
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_caches,
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_caches,
            COUNT(CASE WHEN status = 'DOWNLOADING' THEN 1 END) as downloading_caches,
            COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed_caches,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_caches,
            COALESCE(SUM(cache_size_bytes), 0) as total_size_bytes,
            COALESCE(AVG(download_progress), 0.0) as avg_download_progress
        FROM offline_map_caches 
        WHERE created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    OfflineMapCacheStatistics getCacheStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get cache statistics by region
     */
    @Query(value = """
        SELECT 
            region_id,
            region_name,
            COUNT(*) as cache_count,
            SUM(cache_size_bytes) as total_size_bytes,
            AVG(download_progress) as avg_download_progress
        FROM offline_map_caches 
        WHERE created_at BETWEEN :startDate AND :endDate
        GROUP BY region_id, region_name
        ORDER BY total_size_bytes DESC
        """, nativeQuery = true)
    List<OfflineMapCacheRegionStatistics> getCacheStatisticsByRegion(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Interface for cache statistics projection
     */
    interface OfflineMapCacheStatistics {
        Long getTotalCaches();
        Long getCompletedCaches();
        Long getDownloadingCaches();
        Long getFailedCaches();
        Long getPendingCaches();
        Long getTotalSizeBytes();
        Double getAvgDownloadProgress();
    }
    
    /**
     * Interface for region statistics projection
     */
    interface OfflineMapCacheRegionStatistics {
        String getRegionId();
        String getRegionName();
        Long getCacheCount();
        Long getTotalSizeBytes();
        Double getAvgDownloadProgress();
    }
}



