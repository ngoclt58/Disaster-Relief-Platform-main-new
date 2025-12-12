package com.relief.service.ai;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

/**
 * Location similarity service for geographic proximity calculations
 */
@Service
public class LocationSimilarityService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate similarity based on distance (closer = more similar)
     */
    public double calculateSimilarity(Point point1, Point point2) {
        if (point1 == null || point2 == null) {
            return 0.0;
        }

        double distance = calculateDistance(point1, point2);
        
        // Convert distance to similarity score (0-1)
        // Within 100m = 1.0, within 1km = 0.8, within 5km = 0.6, within 10km = 0.4
        if (distance <= 0.1) return 1.0;
        if (distance <= 1.0) return 0.8;
        if (distance <= 5.0) return 0.6;
        if (distance <= 10.0) return 0.4;
        if (distance <= 50.0) return 0.2;
        return 0.0;
    }

    /**
     * Calculate distance between two points in kilometers
     */
    public double calculateDistance(Point point1, Point point2) {
        double lat1 = Math.toRadians(point1.getY());
        double lon1 = Math.toRadians(point1.getX());
        double lat2 = Math.toRadians(point2.getY());
        double lon2 = Math.toRadians(point2.getX());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Check if two points are within specified distance
     */
    public boolean isWithinDistance(Point point1, Point point2, double maxDistanceKm) {
        return calculateDistance(point1, point2) <= maxDistanceKm;
    }
}


