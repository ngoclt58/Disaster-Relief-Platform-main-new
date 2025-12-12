package com.relief.domain.offline;

/**
 * Status of individual map tiles
 */
public enum OfflineTileStatus {
    PENDING,                // Tile download pending
    DOWNLOADING,            // Currently downloading
    COMPLETED,              // Successfully downloaded
    FAILED,                 // Download failed
    CORRUPTED,              // File is corrupted
    EXPIRED,                // Tile has expired
    DELETED                 // Tile has been deleted
}



