package com.relief.domain.offline;

/**
 * Types of offline map caches
 */
public enum OfflineMapType {
    SATELLITE,              // Satellite imagery
    STREET_MAP,             // Street map tiles
    TERRAIN,                // Terrain map tiles
    HYBRID,                 // Hybrid satellite/street map
    TOPOGRAPHIC,            // Topographic map tiles
    AERIAL,                 // Aerial photography
    NIGHT,                  // Night mode map tiles
    TRAFFIC,                // Traffic overlay tiles
    WEATHER,                // Weather overlay tiles
    DISASTER_OVERLAY,       // Disaster-specific overlays
    CUSTOM                  // Custom map type
}



