package com.relief.domain.offline;

/**
 * Status of offline map download sessions
 */
public enum OfflineDownloadStatus {
    PENDING,                // Download pending
    RUNNING,                // Download in progress
    COMPLETED,              // Download completed successfully
    FAILED,                 // Download failed
    PAUSED,                 // Download paused
    CANCELLED,              // Download cancelled
    RETRYING                // Download retrying after failure
}



