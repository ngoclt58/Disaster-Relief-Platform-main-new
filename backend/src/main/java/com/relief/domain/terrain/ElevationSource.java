package com.relief.domain.terrain;

/**
 * Sources of elevation data
 */
public enum ElevationSource {
    SRTM,           // Shuttle Radar Topography Mission
    ASTER,          // Advanced Spaceborne Thermal Emission and Reflection Radiometer
    LIDAR,          // Light Detection and Ranging
    GPS,            // GPS elevation data
    SURVEY,         // Manual survey data
    OPENSTREETMAP,  // OpenStreetMap elevation data
    GOOGLE_EARTH,   // Google Earth elevation data
    CUSTOM          // Custom elevation data
}



