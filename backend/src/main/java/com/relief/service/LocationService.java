package com.relief.service;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

/**
 * Location service for geographic operations
 */
@Service
public class LocationService {

    /**
     * Get human-readable location description
     */
    public String getLocationDescription(Point point) {
        if (point == null) {
            return "Unknown location";
        }
        
        // In real implementation, use reverse geocoding service
        double lat = point.getY();
        double lon = point.getX();
        
        return String.format("Lat: %.4f, Lon: %.4f", lat, lon);
    }
}


