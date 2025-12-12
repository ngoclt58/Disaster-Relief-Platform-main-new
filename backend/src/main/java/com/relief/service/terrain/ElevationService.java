package com.relief.service.terrain;

import com.relief.domain.terrain.ElevationPoint;
import com.relief.domain.terrain.ElevationSource;
import com.relief.repository.terrain.ElevationPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for elevation data management and terrain analysis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElevationService {
    
    private final ElevationPointRepository elevationPointRepository;
    private final GeometryFactory geometryFactory;
    
    /**
     * Get elevation at a specific point
     */
    public Optional<Double> getElevationAtPoint(double longitude, double latitude) {
        return elevationPointRepository.findNearestToPoint(longitude, latitude)
            .map(ElevationPoint::getElevation);
    }
    
    /**
     * Get elevation points within a bounding box
     */
    public List<ElevationPoint> getElevationPointsInBounds(
            double minLon, double minLat, double maxLon, double maxLat) {
        return elevationPointRepository.findWithinBounds(
            minLon, minLat, maxLon, maxLat, 
            (minLon + maxLon) / 2, (minLat + maxLat) / 2
        );
    }
    
    /**
     * Get elevation points within a radius
     */
    public List<ElevationPoint> getElevationPointsInRadius(
            double longitude, double latitude, double radiusMeters) {
        return elevationPointRepository.findWithinRadius(longitude, latitude, radiusMeters);
    }
    
    /**
     * Add elevation point
     */
    @Transactional
    public ElevationPoint addElevationPoint(double longitude, double latitude, double elevation, 
                                          ElevationSource source, Double accuracy, Double resolution) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        
        ElevationPoint elevationPoint = ElevationPoint.builder()
            .location(point)
            .elevation(elevation)
            .source(source)
            .accuracy(accuracy)
            .resolution(resolution)
            .build();
            
        return elevationPointRepository.save(elevationPoint);
    }
    
    /**
     * Bulk import elevation points
     */
    @Transactional
    public List<ElevationPoint> bulkImportElevationPoints(List<ElevationPointData> points) {
        List<ElevationPoint> elevationPoints = points.stream()
            .map(data -> {
                Point point = geometryFactory.createPoint(new Coordinate(data.longitude(), data.latitude()));
                return ElevationPoint.builder()
                    .location(point)
                    .elevation(data.elevation())
                    .source(data.source())
                    .accuracy(data.accuracy())
                    .resolution(data.resolution())
                    .build();
            })
            .toList();
            
        return elevationPointRepository.saveAll(elevationPoints);
    }
    
    /**
     * Get elevation statistics for an area
     */
    public ElevationStatistics getElevationStatistics(double minLon, double minLat, 
                                                    double maxLon, double maxLat) {
        var stats = elevationPointRepository.getStatisticsForBounds(minLon, minLat, maxLon, maxLat);
        
        return new ElevationStatistics(
            stats.getMinElevation(),
            stats.getMaxElevation(),
            stats.getAvgElevation(),
            stats.getElevationStddev(),
            stats.getPointCount()
        );
    }
    
    /**
     * Calculate slope between two points
     */
    public double calculateSlope(double lon1, double lat1, double elev1,
                               double lon2, double lat2, double elev2) {
        // Calculate horizontal distance using Haversine formula
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        
        if (distance == 0) return 0;
        
        // Calculate slope in degrees
        double elevationDiff = elev2 - elev1;
        return Math.toDegrees(Math.atan(elevationDiff / distance));
    }
    
    /**
     * Calculate aspect (direction of slope) between two points
     */
    public double calculateAspect(double lon1, double lat1, double lon2, double lat2) {
        double deltaLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        
        double y = Math.sin(deltaLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - 
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLon);
        
        double aspect = Math.toDegrees(Math.atan2(y, x));
        return (aspect + 360) % 360; // Normalize to 0-360
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Data class for elevation point import
     */
    public record ElevationPointData(
        double longitude,
        double latitude,
        double elevation,
        ElevationSource source,
        Double accuracy,
        Double resolution
    ) {}
    
    /**
     * Data class for elevation statistics
     */
    public record ElevationStatistics(
        Double minElevation,
        Double maxElevation,
        Double avgElevation,
        Double elevationStddev,
        Long pointCount
    ) {}
}



