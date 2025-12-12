package com.relief.domain.offline;

/**
 * Status of offline map cache
 */
public enum OfflineCacheStatus {
    PENDING,                // Cache request pending
    DOWNLOADING,            // Currently downloading tiles
    COMPLETED,              // Download completed successfully
    FAILED,                 // Download failed
    PAUSED,                 // Download paused
    EXPIRED,                // Cache has expired
    DELETED,                // Cache has been deleted
    CORRUPTED,              // Cache data is corrupted
    UPDATING                // Cache is being updated
}



