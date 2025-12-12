package com.relief.domain.offline;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a cached map tile
 */
@Entity
@Table(name = "offline_map_tiles", indexes = {
    @Index(name = "idx_offline_tile_cache", columnList = "offline_map_cache_id"),
    @Index(name = "idx_offline_tile_coords", columnList = "z, x, y"),
    @Index(name = "idx_offline_tile_status", columnList = "status"),
    @Index(name = "idx_offline_tile_accessed", columnList = "last_accessed_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineMapTile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offline_map_cache_id", nullable = false)
    private OfflineMapCache offlineMapCache;
    
    @Column(name = "z", nullable = false)
    private Integer z; // Zoom level
    
    @Column(name = "x", nullable = false)
    private Integer x; // X coordinate
    
    @Column(name = "y", nullable = false)
    private Integer y; // Y coordinate
    
    @Column(name = "tile_key", nullable = false)
    private String tileKey; // Unique key for the tile (z/x/y)
    
    @Column(name = "tile_url", nullable = false)
    private String tileUrl; // Original tile URL
    
    @Column(name = "file_path")
    private String filePath; // Local file path
    
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes; // File size in bytes
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OfflineTileStatus status;
    
    @Column(name = "download_attempts", nullable = false)
    private Integer downloadAttempts;
    
    @Column(name = "last_download_attempt")
    private LocalDateTime lastDownloadAttempt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "checksum")
    private String checksum; // File checksum for integrity
    
    @Column(name = "is_compressed", nullable = false)
    private Boolean isCompressed;
    
    @Column(name = "compression_ratio")
    private Double compressionRatio;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional tile metadata
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OfflineTileStatus.PENDING;
        }
        if (downloadAttempts == null) {
            downloadAttempts = 0;
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



