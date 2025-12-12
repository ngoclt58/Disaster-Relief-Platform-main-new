package com.relief.service.satellite;

import com.relief.domain.satellite.*;
import com.relief.repository.satellite.SatelliteImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for satellite imagery management and processing
 */
@Service
@RequiredArgsConstructor
public class SatelliteImageryService {
    private static final Logger log = LoggerFactory.getLogger(SatelliteImageryService.class);
    
    private final SatelliteImageRepository satelliteImageRepository;
    private final GeometryFactory geometryFactory;
    
    /**
     * Add satellite image to the system
     */
    @Transactional
    public SatelliteImage addSatelliteImage(SatelliteImageData imageData) {
        log.info("Adding satellite image from provider: {}", imageData.provider());
        
        // Create coverage area polygon
        Polygon coverageArea = createCoverageArea(
            imageData.minLon(), imageData.minLat(),
            imageData.maxLon(), imageData.maxLat()
        );
        
        SatelliteImage image = SatelliteImage.builder()
            .imageUrl(imageData.imageUrl())
            .thumbnailUrl(imageData.thumbnailUrl())
            .coverageArea(coverageArea)
            .provider(imageData.provider())
            .satelliteName(imageData.satelliteName())
            .capturedAt(imageData.capturedAt())
            .resolutionMeters(imageData.resolutionMeters())
            .cloudCoverPercentage(imageData.cloudCoverPercentage())
            .sunElevationAngle(imageData.sunElevationAngle())
            .sunAzimuthAngle(imageData.sunAzimuthAngle())
            .imageBands(imageData.imageBands())
            .metadata(imageData.metadata())
            .processingStatus(ProcessingStatus.PENDING)
            .qualityScore(calculateQualityScore(imageData))
            .build();
            
        return satelliteImageRepository.save(image);
    }
    
    /**
     * Get satellite images within a bounding box
     */
    public List<SatelliteImage> getImagesInBounds(double minLon, double minLat, 
                                                 double maxLon, double maxLat) {
        return satelliteImageRepository.findWithinBounds(minLon, minLat, maxLon, maxLat);
    }
    
    /**
     * Get satellite images by provider
     */
    public List<SatelliteImage> getImagesByProvider(SatelliteProvider provider) {
        return satelliteImageRepository.findByProvider(provider);
    }
    
    /**
     * Get satellite images by processing status
     */
    public List<SatelliteImage> getImagesByStatus(ProcessingStatus status) {
        return satelliteImageRepository.findByProcessingStatus(status);
    }
    
    /**
     * Get satellite images captured within date range
     */
    public List<SatelliteImage> getImagesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return satelliteImageRepository.findByCapturedAtBetween(startDate, endDate);
    }
    
    /**
     * Get the most recent satellite image for a point
     */
    public Optional<SatelliteImage> getMostRecentImage(double longitude, double latitude) {
        return satelliteImageRepository.findMostRecentForPoint(longitude, latitude);
    }
    
    /**
     * Get satellite images for change detection
     */
    public List<SatelliteImage> getImagesForChangeDetection(double minLon, double minLat,
                                                           double maxLon, double maxLat,
                                                           LocalDateTime startDate, LocalDateTime endDate) {
        return satelliteImageRepository.findForChangeDetection(
            minLon, minLat, maxLon, maxLat, startDate, endDate
        );
    }
    
    /**
     * Update processing status
     */
    @Transactional
    public void updateProcessingStatus(Long imageId, ProcessingStatus status) {
        satelliteImageRepository.findById(imageId).ifPresent(image -> {
            image.setProcessingStatus(status);
            satelliteImageRepository.save(image);
            log.info("Updated processing status for image {} to {}", imageId, status);
        });
    }
    
    /**
     * Update quality score
     */
    @Transactional
    public void updateQualityScore(Long imageId, double qualityScore) {
        satelliteImageRepository.findById(imageId).ifPresent(image -> {
            image.setQualityScore(qualityScore);
            satelliteImageRepository.save(image);
            log.info("Updated quality score for image {} to {}", imageId, qualityScore);
        });
    }
    
    /**
     * Get satellite image statistics
     */
    public SatelliteImageStatistics getStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        var stats = satelliteImageRepository.getStatistics(startDate, endDate);
        
        return new SatelliteImageStatistics(
            stats.getTotalImages(),
            stats.getAvgResolution(),
            stats.getAvgCloudCover(),
            stats.getAvgQuality(),
            stats.getEarliestCapture(),
            stats.getLatestCapture()
        );
    }
    
    /**
     * Create coverage area polygon from bounds
     */
    private Polygon createCoverageArea(double minLon, double minLat, double maxLon, double maxLat) {
        Coordinate[] coordinates = {
            new Coordinate(minLon, minLat),
            new Coordinate(maxLon, minLat),
            new Coordinate(maxLon, maxLat),
            new Coordinate(minLon, maxLat),
            new Coordinate(minLon, minLat) // Close the polygon
        };
        
        LinearRing ring = geometryFactory.createLinearRing(coordinates);
        return geometryFactory.createPolygon(ring);
    }
    
    /**
     * Calculate quality score based on various factors
     */
    private double calculateQualityScore(SatelliteImageData imageData) {
        double score = 1.0;
        
        // Penalize high cloud cover
        if (imageData.cloudCoverPercentage() != null) {
            if (imageData.cloudCoverPercentage() > 50) {
                score -= 0.5;
            } else if (imageData.cloudCoverPercentage() > 25) {
                score -= 0.3;
            } else if (imageData.cloudCoverPercentage() > 10) {
                score -= 0.1;
            }
        }
        
        // Penalize low resolution
        if (imageData.resolutionMeters() > 30) {
            score -= 0.3;
        } else if (imageData.resolutionMeters() > 10) {
            score -= 0.1;
        }
        
        // Penalize low sun elevation
        if (imageData.sunElevationAngle() != null && imageData.sunElevationAngle() < 20) {
            score -= 0.2;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Data class for satellite image input
     */
    public record SatelliteImageData(
        String imageUrl,
        String thumbnailUrl,
        double minLon, double minLat, double maxLon, double maxLat,
        SatelliteProvider provider,
        String satelliteName,
        LocalDateTime capturedAt,
        double resolutionMeters,
        Double cloudCoverPercentage,
        Double sunElevationAngle,
        Double sunAzimuthAngle,
        String imageBands,
        String metadata
    ) {}
    
    /**
     * Data class for satellite image statistics
     */
    public record SatelliteImageStatistics(
        Long totalImages,
        Double avgResolution,
        Double avgCloudCover,
        Double avgQuality,
        LocalDateTime earliestCapture,
        LocalDateTime latestCapture
    ) {}
}
