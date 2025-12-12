package com.relief.controller.offline;

import com.relief.domain.offline.*;
import com.relief.service.offline.OfflineMapCacheService;
import com.relief.service.offline.OfflineMapCacheService.OfflineMapCacheRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for offline map caching
 */
@RestController
@RequestMapping("/offline-maps")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Offline Map Cache", description = "Offline map caching and tile management APIs")
public class OfflineMapCacheController {
    
    private final OfflineMapCacheService offlineMapCacheService;
    
    @PostMapping("/caches")
    @Operation(summary = "Create offline map cache", description = "Create a new offline map cache for areas with poor connectivity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<OfflineMapCache> createOfflineMapCache(@Valid @RequestBody OfflineMapCacheRequest request) {
        log.info("Creating offline map cache: {}", request.name());
        OfflineMapCache cache = offlineMapCacheService.createOfflineMapCache(request);
        return ResponseEntity.ok(cache);
    }
    
    @GetMapping("/caches/{cacheId}")
    @Operation(summary = "Get offline map cache", description = "Get offline map cache by ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<OfflineMapCache> getOfflineMapCache(@PathVariable Long cacheId) {
        log.info("Getting offline map cache: {}", cacheId);
        Optional<OfflineMapCache> cache = offlineMapCacheService.getOfflineMapCache(cacheId);
        return cache.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/caches")
    @Operation(summary = "Get offline map caches", description = "Get all offline map caches with optional filtering")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<OfflineMapCache>> getAllOfflineMapCaches(
            @Parameter(description = "Filter by region ID") @RequestParam(required = false) String regionId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) OfflineCacheStatus status,
            @Parameter(description = "Filter by map type") @RequestParam(required = false) OfflineMapType mapType,
            @Parameter(description = "Filter by priority") @RequestParam(required = false) OfflineCachePriority priority) {
        log.info("Getting offline map caches - regionId: {}, status: {}, mapType: {}", regionId, status, mapType);
        
        List<OfflineMapCache> caches;
        if (regionId != null) {
            caches = offlineMapCacheService.getCachesByRegion(regionId);
        } else if (status != null) {
            caches = offlineMapCacheService.getCachesByStatus(status);
        } else {
            caches = offlineMapCacheService.getAllOfflineMapCaches();
        }
        
        return ResponseEntity.ok(caches);
    }
    
    @GetMapping("/caches/within-bounds")
    @Operation(summary = "Get caches within bounds", description = "Get offline map caches within geographic bounds")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<OfflineMapCache>> getCachesWithinBounds(
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        log.info("Getting caches within bounds: ({}, {}) to ({}, {})", minLon, minLat, maxLon, maxLat);
        
        List<OfflineMapCache> caches = offlineMapCacheService.getCachesWithinBounds(minLon, minLat, maxLon, maxLat);
        return ResponseEntity.ok(caches);
    }
    
    @GetMapping("/caches/containing-point")
    @Operation(summary = "Get caches containing point", description = "Get offline map caches containing a specific point")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<OfflineMapCache>> getCachesContainingPoint(
            @Parameter(description = "Longitude") @RequestParam double longitude,
            @Parameter(description = "Latitude") @RequestParam double latitude) {
        log.info("Getting caches containing point: ({}, {})", longitude, latitude);
        
        List<OfflineMapCache> caches = offlineMapCacheService.getCachesContainingPoint(longitude, latitude);
        return ResponseEntity.ok(caches);
    }
    
    @PostMapping("/caches/{cacheId}/start-download")
    @Operation(summary = "Start cache download", description = "Start downloading tiles for an offline map cache")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Void> startCacheDownload(@PathVariable Long cacheId) {
        log.info("Starting download for cache: {}", cacheId);
        offlineMapCacheService.startDownloadProcess(cacheId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/caches/{cacheId}/pause-download")
    @Operation(summary = "Pause cache download", description = "Pause downloading tiles for an offline map cache")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Void> pauseCacheDownload(@PathVariable Long cacheId) {
        log.info("Pausing download for cache: {}", cacheId);
        offlineMapCacheService.updateCacheStatus(cacheId, OfflineCacheStatus.PAUSED);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/caches/{cacheId}/resume-download")
    @Operation(summary = "Resume cache download", description = "Resume downloading tiles for an offline map cache")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Void> resumeCacheDownload(@PathVariable Long cacheId) {
        log.info("Resuming download for cache: {}", cacheId);
        offlineMapCacheService.startDownloadProcess(cacheId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/caches/{cacheId}")
    @Operation(summary = "Delete offline map cache", description = "Delete an offline map cache and all its tiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOfflineMapCache(@PathVariable Long cacheId) {
        log.info("Deleting offline map cache: {}", cacheId);
        offlineMapCacheService.deleteOfflineMapCache(cacheId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/caches/cleanup")
    @Operation(summary = "Cleanup expired caches", description = "Clean up expired offline map caches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cleanupExpiredCaches() {
        log.info("Cleaning up expired caches");
        offlineMapCacheService.cleanupExpiredCaches();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/caches/{cacheId}/statistics")
    @Operation(summary = "Get cache statistics", description = "Get statistics for an offline map cache")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getCacheStatistics(@PathVariable Long cacheId) {
        log.info("Getting statistics for cache: {}", cacheId);
        
        // This would need to be implemented in the service
        Map<String, Object> statistics = Map.of(
            "totalTiles", 0,
            "downloadedTiles", 0,
            "failedTiles", 0,
            "downloadProgress", 0.0,
            "cacheSizeBytes", 0L,
            "estimatedSizeBytes", 0L
        );
        
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get global statistics", description = "Get global statistics for all offline map caches")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getGlobalStatistics(
            @Parameter(description = "Start date (ISO format: yyyy-MM-ddTHH:mm:ss)") @RequestParam(required = false) 
            String startDate,
            @Parameter(description = "End date (ISO format: yyyy-MM-ddTHH:mm:ss)") @RequestParam(required = false) 
            String endDate) {
        log.info("Getting global statistics - startDate: {}, endDate: {}", startDate, endDate);
        
        LocalDateTime start;
        LocalDateTime end;
        
        try {
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                start = LocalDateTime.now().minusDays(30);
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                end = LocalDateTime.now();
            }
        } catch (DateTimeParseException e) {
            log.error("Invalid date format provided: startDate={}, endDate={}", startDate, endDate, e);
            return ResponseEntity.badRequest().build();
        }
        
        var statistics = offlineMapCacheService.getCacheStatistics(start, end);
        
        // Handle null case when no caches exist
        if (statistics == null) {
            Map<String, Object> result = Map.of(
                "totalCaches", 0L,
                "completedCaches", 0L,
                "downloadingCaches", 0L,
                "failedCaches", 0L,
                "pendingCaches", 0L,
                "totalSizeBytes", 0L,
                "avgDownloadProgress", 0.0
            );
            return ResponseEntity.ok(result);
        }
        
        Map<String, Object> result = Map.of(
            "totalCaches", statistics.getTotalCaches() != null ? statistics.getTotalCaches() : 0L,
            "completedCaches", statistics.getCompletedCaches() != null ? statistics.getCompletedCaches() : 0L,
            "downloadingCaches", statistics.getDownloadingCaches() != null ? statistics.getDownloadingCaches() : 0L,
            "failedCaches", statistics.getFailedCaches() != null ? statistics.getFailedCaches() : 0L,
            "pendingCaches", statistics.getPendingCaches() != null ? statistics.getPendingCaches() : 0L,
            "totalSizeBytes", statistics.getTotalSizeBytes() != null ? statistics.getTotalSizeBytes() : 0L,
            "avgDownloadProgress", statistics.getAvgDownloadProgress() != null ? statistics.getAvgDownloadProgress() : 0.0
        );
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/statistics/regions")
    @Operation(summary = "Get regional statistics", description = "Get statistics by region for offline map caches")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<List<Map<String, Object>>> getRegionalStatistics(
            @Parameter(description = "Start date (ISO format: yyyy-MM-ddTHH:mm:ss)") @RequestParam(required = false) 
            String startDate,
            @Parameter(description = "End date (ISO format: yyyy-MM-ddTHH:mm:ss)") @RequestParam(required = false) 
            String endDate) {
        log.info("Getting regional statistics - startDate: {}, endDate: {}", startDate, endDate);
        
        LocalDateTime start;
        LocalDateTime end;
        
        try {
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                start = LocalDateTime.now().minusDays(30);
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                end = LocalDateTime.now();
            }
        } catch (DateTimeParseException e) {
            log.error("Invalid date format provided: startDate={}, endDate={}", startDate, endDate, e);
            return ResponseEntity.badRequest().build();
        }
        
        var regionStatistics = offlineMapCacheService.getCacheStatisticsByRegion(start, end);
        
        List<Map<String, Object>> result = regionStatistics.stream()
            .map(stat -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("regionId", stat.getRegionId());
                map.put("regionName", stat.getRegionName());
                map.put("cacheCount", stat.getCacheCount());
                map.put("totalSizeBytes", stat.getTotalSizeBytes());
                map.put("avgDownloadProgress", stat.getAvgDownloadProgress());
                return map;
            })
            .toList();
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/tiles/{cacheId}")
    @Operation(summary = "Get cache tiles", description = "Get tiles for an offline map cache")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<Map<String, Object>>> getCacheTiles(
            @PathVariable Long cacheId,
            @Parameter(description = "Filter by zoom level") @RequestParam(required = false) Integer zoomLevel,
            @Parameter(description = "Filter by status") @RequestParam(required = false) OfflineTileStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "100") int size) {
        log.info("Getting tiles for cache: {}", cacheId);
        
        // This would need to be implemented in the service
        List<Map<String, Object>> tiles = List.of();
        
        return ResponseEntity.ok(tiles);
    }
    
    @GetMapping("/tiles/{cacheId}/download")
    @Operation(summary = "Download tile", description = "Download a specific tile from cache")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<byte[]> downloadTile(
            @PathVariable Long cacheId,
            @Parameter(description = "Zoom level") @RequestParam int z,
            @Parameter(description = "X coordinate") @RequestParam int x,
            @Parameter(description = "Y coordinate") @RequestParam int y) {
        log.info("Downloading tile: {}/{}/{}/{}", cacheId, z, x, y);
        
        // This would need to be implemented in the service
        byte[] tileData = new byte[0];
        
        return ResponseEntity.ok()
            .header("Content-Type", "image/png")
            .body(tileData);
    }
}



