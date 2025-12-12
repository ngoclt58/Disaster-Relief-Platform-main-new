package com.relief.service.offline;

import com.relief.domain.offline.*;
import com.relief.repository.offline.OfflineMapCacheRepository;
import com.relief.repository.offline.OfflineMapTileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service for offline map caching and tile management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfflineMapCacheService {
    
    private final OfflineMapCacheRepository cacheRepository;
    private final OfflineMapTileRepository tileRepository;
    private final GeometryFactory geometryFactory;
    private final RestTemplate restTemplate;
    
    @Value("${app.offline-map.storage-path:/tmp/offline-maps}")
    private String storagePath;
    
    @Value("${app.offline-map.max-concurrent-downloads:5}")
    private int maxConcurrentDownloads;
    
    @Value("${app.offline-map.tile-timeout-seconds:30}")
    private int tileTimeoutSeconds;
    
    private ExecutorService downloadExecutor;
    
    @PostConstruct
    private void initExecutor() {
        int threads = maxConcurrentDownloads > 0 ? maxConcurrentDownloads : 5;
        this.downloadExecutor = Executors.newFixedThreadPool(threads);
    }
    
    @PreDestroy
    private void shutdownExecutor() {
        if (downloadExecutor != null && !downloadExecutor.isShutdown()) {
            downloadExecutor.shutdown();
            try {
                if (!downloadExecutor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    downloadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                downloadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Create an offline map cache
     */
    @Transactional
    public OfflineMapCache createOfflineMapCache(OfflineMapCacheRequest request) {
        log.info("Creating offline map cache: {}", request.name());
        
        // Create bounds geometry
        Geometry bounds = createBoundsGeometry(request.boundsCoordinates());
        
        // Calculate total tiles
        long totalTiles = calculateTotalTiles(request.boundsCoordinates(), request.zoomLevels());
        
        OfflineMapCache cache = OfflineMapCache.builder()
            .name(request.name())
            .description(request.description())
            .regionId(request.regionId())
            .regionName(request.regionName())
            .bounds(bounds)
            .zoomLevels(request.zoomLevels().toString())
            .mapType(request.mapType())
            .tileSource(request.tileSource())
            .tileFormat(request.tileFormat())
            .status(OfflineCacheStatus.PENDING)
            .priority(request.priority())
            .totalTiles(totalTiles)
            .downloadedTiles(0L)
            .cacheSizeBytes(0L)
            .estimatedSizeBytes(request.estimatedSizeBytes())
            .downloadProgress(0.0)
            .isCompressed(request.isCompressed())
            .metadata(request.metadata())
            .createdBy(request.createdBy())
            .build();
            
        OfflineMapCache savedCache = cacheRepository.save(cache);
        
        // Start download process asynchronously
        if (request.autoStart()) {
            startDownloadProcess(savedCache.getId());
        }
        
        return savedCache;
    }
    
    /**
     * Start download process for a cache
     */
    @Transactional
    public void startDownloadProcess(Long cacheId) {
        OfflineMapCache cache = cacheRepository.findById(cacheId)
            .orElseThrow(() -> new IllegalArgumentException("Cache not found"));
        
        if (cache.getStatus() != OfflineCacheStatus.PENDING) {
            throw new IllegalStateException("Cache is not in pending status");
        }
        
        log.info("Starting download process for cache: {}", cacheId);
        
        // Update cache status
        cache.setStatus(OfflineCacheStatus.DOWNLOADING);
        cache.setDownloadStartedAt(LocalDateTime.now());
        cacheRepository.save(cache);
        
        // Start async download
        CompletableFuture.runAsync(() -> downloadTiles(cache), downloadExecutor)
            .exceptionally(throwable -> {
                log.error("Download process failed for cache: {}", cacheId, throwable);
                updateCacheStatus(cacheId, OfflineCacheStatus.FAILED);
                return null;
            });
    }
    
    /**
     * Download tiles for a cache
     */
    private void downloadTiles(OfflineMapCache cache) {
        try {
            List<Integer> zoomLevels = parseZoomLevels(cache.getZoomLevels());
            List<Coordinate> bounds = extractBoundsCoordinates(cache.getBounds());
            
            long totalTiles = 0;
            long downloadedTiles = 0;
            
            for (Integer zoom : zoomLevels) {
                List<TileCoordinate> tiles = calculateTilesForZoom(bounds, zoom);
                totalTiles += tiles.size();
                
                for (TileCoordinate tile : tiles) {
                    try {
                        downloadTile(cache, tile);
                        downloadedTiles++;
                        
                        // Update progress
                        double progress = (double) downloadedTiles / totalTiles;
                        updateCacheProgress(cache.getId(), downloadedTiles, progress);
                        
                    } catch (Exception e) {
                        log.error("Failed to download tile: {}/{}/{}", tile.z, tile.x, tile.y, e);
                    }
                }
            }
            
            // Mark as completed
            updateCacheStatus(cache.getId(), OfflineCacheStatus.COMPLETED);
            updateCacheCompletion(cache.getId(), downloadedTiles);
            
        } catch (Exception e) {
            log.error("Download process failed for cache: {}", cache.getId(), e);
            updateCacheStatus(cache.getId(), OfflineCacheStatus.FAILED);
        }
    }
    
    /**
     * Download a single tile
     */
    private void downloadTile(OfflineMapCache cache, TileCoordinate tile) throws IOException {
        String tileUrl = buildTileUrl(cache.getTileSource(), tile);
        String tileKey = String.format("%d/%d/%d", tile.z, tile.x, tile.y);
        
        // Check if tile already exists
        Optional<OfflineMapTile> existingTile = tileRepository.findByOfflineMapCacheIdAndTileKey(
            cache.getId(), tileKey);
        
        if (existingTile.isPresent() && existingTile.get().getStatus() == OfflineTileStatus.COMPLETED) {
            return;
        }
        
        // Create or update tile record
        OfflineMapTile tileEntity = existingTile.orElse(OfflineMapTile.builder()
            .offlineMapCache(cache)
            .z(tile.z)
            .x(tile.x)
            .y(tile.y)
            .tileKey(tileKey)
            .tileUrl(tileUrl)
            .status(OfflineTileStatus.DOWNLOADING)
            .downloadAttempts(0)
            .isCompressed(cache.getIsCompressed())
            .build());
        
        tileEntity.setStatus(OfflineTileStatus.DOWNLOADING);
        tileEntity.setDownloadAttempts(tileEntity.getDownloadAttempts() + 1);
        tileEntity.setLastDownloadAttempt(LocalDateTime.now());
        
        try {
            // Download tile data
            byte[] tileData = downloadTileData(tileUrl);
            
            // Save tile file
            String filePath = saveTileFile(cache, tile, tileData);
            
            // Update tile entity
            tileEntity.setFilePath(filePath);
            tileEntity.setFileSizeBytes((long) tileData.length);
            tileEntity.setStatus(OfflineTileStatus.COMPLETED);
            tileEntity.setLastAccessedAt(LocalDateTime.now());
            
            // Calculate checksum
            tileEntity.setChecksum(calculateChecksum(tileData));
            
            tileRepository.save(tileEntity);
            
        } catch (Exception e) {
            tileEntity.setStatus(OfflineTileStatus.FAILED);
            tileRepository.save(tileEntity);
            throw e;
        }
    }
    
    /**
     * Download tile data from URL
     */
    private byte[] downloadTileData(String tileUrl) throws IOException {
        try (InputStream inputStream = new URL(tileUrl).openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Save tile file to storage
     */
    private String saveTileFile(OfflineMapCache cache, TileCoordinate tile, byte[] tileData) {
        String fileName = String.format("%d_%d_%d.%s", tile.z, tile.x, tile.y, cache.getTileFormat());
        String filePath = String.format("%s/%s/%s", storagePath, cache.getRegionId(), fileName);
        
        // Create directory if it doesn't exist
        java.io.File directory = new java.io.File(filePath).getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Write file
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
            fos.write(tileData);
        } catch (IOException e) {
            log.error("IO error when saving tile: {}", filePath, e);
            throw new RuntimeException("Failed to save tile file: " + filePath, e);
        }
        
        return filePath;
    }
    
    /**
     * Calculate total tiles for bounds and zoom levels
     */
    private long calculateTotalTiles(List<Coordinate> bounds, List<Integer> zoomLevels) {
        long totalTiles = 0;
        
        for (Integer zoom : zoomLevels) {
            List<TileCoordinate> tiles = calculateTilesForZoom(bounds, zoom);
            totalTiles += tiles.size();
        }
        
        return totalTiles;
    }
    
    /**
     * Calculate tiles for a specific zoom level
     */
    private List<TileCoordinate> calculateTilesForZoom(List<Coordinate> bounds, int zoom) {
        List<TileCoordinate> tiles = new ArrayList<>();
        
        // Convert bounds to tile coordinates
        int minX = (int) Math.floor((bounds.get(0).x + 180) / 360 * Math.pow(2, zoom));
        int maxX = (int) Math.floor((bounds.get(2).x + 180) / 360 * Math.pow(2, zoom));
        int minY = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(bounds.get(2).y)) + 
            1 / Math.cos(Math.toRadians(bounds.get(2).y))) / Math.PI) / 2 * Math.pow(2, zoom));
        int maxY = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(bounds.get(0).y)) + 
            1 / Math.cos(Math.toRadians(bounds.get(0).y))) / Math.PI) / 2 * Math.pow(2, zoom));
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                tiles.add(new TileCoordinate(zoom, x, y));
            }
        }
        
        return tiles;
    }
    
    /**
     * Build tile URL from template
     */
    private String buildTileUrl(String tileSource, TileCoordinate tile) {
        return tileSource
            .replace("{z}", String.valueOf(tile.z))
            .replace("{x}", String.valueOf(tile.x))
            .replace("{y}", String.valueOf(tile.y));
    }
    
    /**
     * Parse zoom levels from JSON string
     */
    private List<Integer> parseZoomLevels(String zoomLevelsJson) {
        // Simple JSON array parsing - in production, use proper JSON library
        return Arrays.stream(zoomLevelsJson.replaceAll("[\\[\\]\\s]", "").split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }
    
    /**
     * Extract bounds coordinates from geometry
     */
    private List<Coordinate> extractBoundsCoordinates(Geometry bounds) {
        Coordinate[] coords = bounds.getCoordinates();
        return Arrays.asList(coords);
    }
    
    /**
     * Create bounds geometry from coordinates
     */
    private Geometry createBoundsGeometry(List<Coordinate> coordinates) {
        if (coordinates.size() < 3) {
            throw new IllegalArgumentException("At least 3 coordinates required for bounds");
        }
        
        // Close the polygon if not already closed
        if (!coordinates.get(0).equals(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(coordinates.get(0));
        }
        
        Coordinate[] coordArray = coordinates.toArray(new Coordinate[0]);
        LinearRing ring = geometryFactory.createLinearRing(coordArray);
        return geometryFactory.createPolygon(ring);
    }
    
    /**
     * Calculate checksum for tile data
     */
    private String calculateChecksum(byte[] data) {
        return Integer.toHexString(Arrays.hashCode(data));
    }
    
    /**
     * Update cache status
     */
    @Transactional
    public void updateCacheStatus(Long cacheId, OfflineCacheStatus status) {
        OfflineMapCache cache = cacheRepository.findById(cacheId)
            .orElseThrow(() -> new IllegalArgumentException("Cache not found"));
        
        cache.setStatus(status);
        if (status == OfflineCacheStatus.COMPLETED) {
            cache.setDownloadCompletedAt(LocalDateTime.now());
        }
        
        cacheRepository.save(cache);
    }
    
    /**
     * Update cache progress
     */
    @Transactional
    public void updateCacheProgress(Long cacheId, long downloadedTiles, double progress) {
        OfflineMapCache cache = cacheRepository.findById(cacheId)
            .orElseThrow(() -> new IllegalArgumentException("Cache not found"));
        
        cache.setDownloadedTiles(downloadedTiles);
        cache.setDownloadProgress(progress);
        
        cacheRepository.save(cache);
    }
    
    /**
     * Update cache completion
     */
    @Transactional
    public void updateCacheCompletion(Long cacheId, long downloadedTiles) {
        OfflineMapCache cache = cacheRepository.findById(cacheId)
            .orElseThrow(() -> new IllegalArgumentException("Cache not found"));
        
        cache.setDownloadedTiles(downloadedTiles);
        cache.setDownloadProgress(1.0);
        cache.setStatus(OfflineCacheStatus.COMPLETED);
        cache.setDownloadCompletedAt(LocalDateTime.now());
        
        // Calculate actual cache size
        Long actualSize = tileRepository.sumFileSizeByCacheId(cacheId);
        cache.setCacheSizeBytes(actualSize != null ? actualSize : 0L);
        
        cacheRepository.save(cache);
    }
    
    /**
     * Get offline map cache by ID
     */
    public Optional<OfflineMapCache> getOfflineMapCache(Long cacheId) {
        return cacheRepository.findById(cacheId);
    }
    
    /**
     * Get all offline map caches
     */
    public List<OfflineMapCache> getAllOfflineMapCaches() {
        return cacheRepository.findAll();
    }
    
    /**
     * Get caches by region
     */
    public List<OfflineMapCache> getCachesByRegion(String regionId) {
        return cacheRepository.findByRegionId(regionId);
    }
    
    /**
     * Get caches by status
     */
    public List<OfflineMapCache> getCachesByStatus(OfflineCacheStatus status) {
        return cacheRepository.findByStatus(status);
    }
    
    /**
     * Get caches within bounds
     */
    public List<OfflineMapCache> getCachesWithinBounds(double minLon, double minLat, double maxLon, double maxLat) {
        return cacheRepository.findWithinBounds(minLon, minLat, maxLon, maxLat);
    }
    
    /**
     * Get caches containing point
     */
    public List<OfflineMapCache> getCachesContainingPoint(double longitude, double latitude) {
        return cacheRepository.findContainingPoint(longitude, latitude);
    }
    
    /**
     * Delete offline map cache
     */
    @Transactional
    public void deleteOfflineMapCache(Long cacheId) {
        OfflineMapCache cache = cacheRepository.findById(cacheId)
            .orElseThrow(() -> new IllegalArgumentException("Cache not found"));
        
        // Delete all tiles
        tileRepository.deleteByOfflineMapCacheId(cacheId);
        
        // Delete cache
        cacheRepository.delete(cache);
    }
    
    /**
     * Clean up expired caches
     */
    @Transactional
    public void cleanupExpiredCaches() {
        LocalDateTime expirationDate = LocalDateTime.now();
        List<OfflineMapCache> expiredCaches = cacheRepository.findExpiredCaches(expirationDate);
        
        for (OfflineMapCache cache : expiredCaches) {
            deleteOfflineMapCache(cache.getId());
            log.info("Cleaned up expired cache: {}", cache.getName());
        }
    }
    
    /**
     * Get cache statistics
     */
    public OfflineMapCacheRepository.OfflineMapCacheStatistics getCacheStatistics(
            LocalDateTime startDate, LocalDateTime endDate) {
        return cacheRepository.getCacheStatistics(startDate, endDate);
    }
    
    /**
     * Get cache statistics by region
     */
    public List<OfflineMapCacheRepository.OfflineMapCacheRegionStatistics> getCacheStatisticsByRegion(
            LocalDateTime startDate, LocalDateTime endDate) {
        return cacheRepository.getCacheStatisticsByRegion(startDate, endDate);
    }
    
    // Data classes
    public record OfflineMapCacheRequest(
        String name,
        String description,
        String regionId,
        String regionName,
        List<Coordinate> boundsCoordinates,
        List<Integer> zoomLevels,
        OfflineMapType mapType,
        String tileSource,
        String tileFormat,
        OfflineCachePriority priority,
        Long estimatedSizeBytes,
        Boolean isCompressed,
        String metadata,
        String createdBy,
        Boolean autoStart
    ) {}
    
    private record TileCoordinate(int z, int x, int y) {}
}



