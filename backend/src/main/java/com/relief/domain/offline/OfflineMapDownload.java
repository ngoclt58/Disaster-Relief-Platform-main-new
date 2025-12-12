package com.relief.domain.offline;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a download session for offline map cache
 */
@Entity
@Table(name = "offline_map_downloads", indexes = {
    @Index(name = "idx_offline_download_cache", columnList = "offline_map_cache_id"),
    @Index(name = "idx_offline_download_status", columnList = "status"),
    @Index(name = "idx_offline_download_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineMapDownload {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offline_map_cache_id", nullable = false)
    private OfflineMapCache offlineMapCache;
    
    @Column(name = "download_id", nullable = false)
    private String downloadId; // Unique download session ID
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OfflineDownloadStatus status;
    
    @Column(name = "total_tiles", nullable = false)
    private Long totalTiles;
    
    @Column(name = "downloaded_tiles", nullable = false)
    private Long downloadedTiles;
    
    @Column(name = "failed_tiles", nullable = false)
    private Long failedTiles;
    
    @Column(name = "progress_percentage", nullable = false)
    private Double progressPercentage;
    
    @Column(name = "download_speed_bytes_per_sec")
    private Long downloadSpeedBytesPerSec;
    
    @Column(name = "estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;
    
    @Column(name = "download_config", columnDefinition = "jsonb")
    private String downloadConfig; // Download configuration JSON
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional download metadata
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OfflineDownloadStatus.PENDING;
        }
        if (totalTiles == null) {
            totalTiles = 0L;
        }
        if (downloadedTiles == null) {
            downloadedTiles = 0L;
        }
        if (failedTiles == null) {
            failedTiles = 0L;
        }
        if (progressPercentage == null) {
            progressPercentage = 0.0;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (maxRetries == null) {
            maxRetries = 3;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



