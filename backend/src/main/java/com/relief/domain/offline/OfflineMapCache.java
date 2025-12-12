package com.relief.domain.offline;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an offline map cache for areas with poor connectivity
 */
@Entity
@Table(name = "offline_map_caches", indexes = {
    @Index(name = "idx_offline_cache_name", columnList = "name"),
    @Index(name = "idx_offline_cache_region", columnList = "region_id"),
    @Index(name = "idx_offline_cache_status", columnList = "status"),
    @Index(name = "idx_offline_cache_priority", columnList = "priority"),
    @Index(name = "idx_offline_cache_geom", columnList = "bounds")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineMapCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "region_id", nullable = false)
    private String regionId; // Unique identifier for the region
    
    @Column(name = "region_name", nullable = false)
    private String regionName;
    
    @Column(name = "bounds", nullable = false, columnDefinition = "geometry(Geometry, 4326)")
    private Geometry bounds; // Geographic bounds of the cached area
    
    @Column(name = "zoom_levels", nullable = false)
    private String zoomLevels; // JSON array of zoom levels to cache
    
    @Column(name = "map_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OfflineMapType mapType;
    
    @Column(name = "tile_source", nullable = false)
    private String tileSource; // URL template for tile source
    
    @Column(name = "tile_format", nullable = false)
    private String tileFormat; // Image format (png, jpg, webp)
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OfflineCacheStatus status;
    
    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private OfflineCachePriority priority;
    
    @Column(name = "total_tiles", nullable = false)
    private Long totalTiles; // Total number of tiles to download
    
    @Column(name = "downloaded_tiles", nullable = false)
    private Long downloadedTiles; // Number of tiles downloaded
    
    @Column(name = "cache_size_bytes", nullable = false)
    private Long cacheSizeBytes; // Size of cached data in bytes
    
    @Column(name = "estimated_size_bytes")
    private Long estimatedSizeBytes; // Estimated total size
    
    @Column(name = "download_progress", nullable = false)
    private Double downloadProgress; // Progress percentage (0.0 to 1.0)
    
    @Column(name = "download_started_at")
    private LocalDateTime downloadStartedAt;
    
    @Column(name = "download_completed_at")
    private LocalDateTime downloadCompletedAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Cache expiration time
    
    @Column(name = "is_compressed", nullable = false)
    private Boolean isCompressed; // Whether the cache is compressed
    
    @Column(name = "compression_ratio")
    private Double compressionRatio; // Compression ratio achieved
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional cache metadata
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "offlineMapCache", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OfflineMapTile> tiles;
    
    @OneToMany(mappedBy = "offlineMapCache", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OfflineMapDownload> downloads;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OfflineCacheStatus.PENDING;
        }
        if (priority == null) {
            priority = OfflineCachePriority.MEDIUM;
        }
        if (totalTiles == null) {
            totalTiles = 0L;
        }
        if (downloadedTiles == null) {
            downloadedTiles = 0L;
        }
        if (cacheSizeBytes == null) {
            cacheSizeBytes = 0L;
        }
        if (downloadProgress == null) {
            downloadProgress = 0.0;
        }
        if (isCompressed == null) {
            isCompressed = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



