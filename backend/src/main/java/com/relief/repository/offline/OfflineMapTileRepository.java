package com.relief.repository.offline;

import com.relief.domain.offline.OfflineMapTile;
import com.relief.domain.offline.OfflineTileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for offline map tiles
 */
@Repository
public interface OfflineMapTileRepository extends JpaRepository<OfflineMapTile, Long> {
    
    /**
     * Find tiles by cache ID
     */
    List<OfflineMapTile> findByOfflineMapCacheId(Long cacheId);
    
    /**
     * Find tiles by status
     */
    List<OfflineMapTile> findByStatus(OfflineTileStatus status);
    
    /**
     * Find tiles by cache and status
     */
    List<OfflineMapTile> findByOfflineMapCacheIdAndStatus(Long cacheId, OfflineTileStatus status);
    
    /**
     * Find tiles by zoom level
     */
    List<OfflineMapTile> findByZ(Integer z);
    
    /**
     * Find tiles by cache and zoom level
     */
    List<OfflineMapTile> findByOfflineMapCacheIdAndZ(Long cacheId, Integer z);
    
    /**
     * Find tiles by coordinates
     */
    Optional<OfflineMapTile> findByZAndXAndY(Integer z, Integer x, Integer y);
    
    /**
     * Find tiles by cache and coordinates
     */
    Optional<OfflineMapTile> findByOfflineMapCacheIdAndZAndXAndY(Long cacheId, Integer z, Integer x, Integer y);
    
    /**
     * Find tiles by tile key
     */
    Optional<OfflineMapTile> findByTileKey(String tileKey);
    
    /**
     * Find tiles by cache and tile key
     */
    Optional<OfflineMapTile> findByOfflineMapCacheIdAndTileKey(Long cacheId, String tileKey);
    
    /**
     * Find tiles by download attempts
     */
    @Query("SELECT t FROM OfflineMapTile t WHERE t.downloadAttempts >= :maxAttempts")
    List<OfflineMapTile> findByMaxDownloadAttempts(@Param("maxAttempts") Integer maxAttempts);
    
    /**
     * Find tiles by last accessed date
     */
    @Query("SELECT t FROM OfflineMapTile t WHERE t.lastAccessedAt < :lastAccessedDate")
    List<OfflineMapTile> findStaleTiles(@Param("lastAccessedDate") LocalDateTime lastAccessedDate);
    
    /**
     * Find tiles by file size range
     */
    @Query("SELECT t FROM OfflineMapTile t WHERE t.fileSizeBytes BETWEEN :minSize AND :maxSize")
    List<OfflineMapTile> findByFileSizeRange(@Param("minSize") long minSize, @Param("maxSize") long maxSize);
    
    /**
     * Find tiles by compression status
     */
    List<OfflineMapTile> findByIsCompressed(Boolean isCompressed);
    
    /**
     * Find tiles by cache and compression status
     */
    List<OfflineMapTile> findByOfflineMapCacheIdAndIsCompressed(Long cacheId, Boolean isCompressed);
    
    /**
     * Count tiles by cache and status
     */
    Long countByOfflineMapCacheIdAndStatus(Long cacheId, OfflineTileStatus status);
    
    /**
     * Count tiles by cache
     */
    Long countByOfflineMapCacheId(Long cacheId);
    
    /**
     * Sum file size by cache
     */
    @Query("SELECT SUM(t.fileSizeBytes) FROM OfflineMapTile t WHERE t.offlineMapCache.id = :cacheId AND t.status = 'COMPLETED'")
    Long sumFileSizeByCacheId(@Param("cacheId") Long cacheId);
    
    /**
     * Get tile statistics for a cache
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_tiles,
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_tiles,
            COUNT(CASE WHEN status = 'DOWNLOADING' THEN 1 END) as downloading_tiles,
            COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed_tiles,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_tiles,
            SUM(file_size_bytes) as total_size_bytes,
            AVG(file_size_bytes) as avg_file_size,
            COUNT(CASE WHEN is_compressed = true THEN 1 END) as compressed_tiles
        FROM offline_map_tiles 
        WHERE offline_map_cache_id = :cacheId
        """, nativeQuery = true)
    OfflineMapTileStatistics getTileStatistics(@Param("cacheId") Long cacheId);
    
    /**
     * Get tile statistics by zoom level
     */
    @Query(value = """
        SELECT 
            z as zoom_level,
            COUNT(*) as tile_count,
            SUM(file_size_bytes) as total_size_bytes,
            AVG(file_size_bytes) as avg_file_size
        FROM offline_map_tiles 
        WHERE offline_map_cache_id = :cacheId
        GROUP BY z
        ORDER BY z
        """, nativeQuery = true)
    List<OfflineMapTileZoomStatistics> getTileStatisticsByZoom(@Param("cacheId") Long cacheId);
    
    /**
     * Find tiles for cleanup (old, unused, or corrupted)
     */
    @Query("SELECT t FROM OfflineMapTile t WHERE " +
           "(t.lastAccessedAt < :cleanupDate AND t.status = 'COMPLETED') OR " +
           "t.status = 'CORRUPTED' OR " +
           "t.status = 'EXPIRED'")
    List<OfflineMapTile> findTilesForCleanup(@Param("cleanupDate") LocalDateTime cleanupDate);
    
    /**
     * Delete tiles by cache and status
     */
    void deleteByOfflineMapCacheIdAndStatus(Long cacheId, OfflineTileStatus status);
    
    /**
     * Delete tiles by cache
     */
    void deleteByOfflineMapCacheId(Long cacheId);
    
    /**
     * Interface for tile statistics projection
     */
    interface OfflineMapTileStatistics {
        Long getTotalTiles();
        Long getCompletedTiles();
        Long getDownloadingTiles();
        Long getFailedTiles();
        Long getPendingTiles();
        Long getTotalSizeBytes();
        Double getAvgFileSize();
        Long getCompressedTiles();
    }
    
    /**
     * Interface for zoom level statistics projection
     */
    interface OfflineMapTileZoomStatistics {
        Integer getZoomLevel();
        Long getTileCount();
        Long getTotalSizeBytes();
        Double getAvgFileSize();
    }
}



