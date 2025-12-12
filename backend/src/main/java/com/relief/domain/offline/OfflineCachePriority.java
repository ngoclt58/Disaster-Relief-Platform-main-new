package com.relief.domain.offline;

/**
 * Priority levels for offline map cache downloads
 */
public enum OfflineCachePriority {
    CRITICAL,               // Critical for disaster response
    HIGH,                   // High priority for operations
    MEDIUM,                 // Medium priority
    LOW,                    // Low priority
    BACKGROUND              // Background download
}



