package com.relief.service.satellite;

import com.relief.domain.satellite.*;
import com.relief.repository.satellite.DamageAssessmentRepository;
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
 * Service for damage assessment using satellite imagery
 */
@Service
@RequiredArgsConstructor
public class DamageAssessmentService {
    private static final Logger log = LoggerFactory.getLogger(DamageAssessmentService.class);
    
    private final DamageAssessmentRepository damageAssessmentRepository;
    private final SatelliteImageRepository satelliteImageRepository;
    private final GeometryFactory geometryFactory;
    
    /**
     * Perform damage assessment on satellite imagery
     */
    @Transactional
    public DamageAssessment performDamageAssessment(DamageAssessmentRequest request) {
        log.info("Performing damage assessment for image: {}", request.satelliteImageId());
        
        SatelliteImage image = satelliteImageRepository.findById(request.satelliteImageId())
            .orElseThrow(() -> new IllegalArgumentException("Satellite image not found"));
        
        // Create damage area polygon
        Polygon damageArea = createDamageArea(request.damageCoordinates());
        
        // Perform damage analysis
        DamageAnalysisResult analysis = analyzeDamage(image, damageArea, request);
        
        // Create damage assessment
        DamageAssessment assessment = DamageAssessment.builder()
            .satelliteImage(image)
            .damageArea(damageArea)
            .damageType(analysis.damageType())
            .severity(analysis.severity())
            .confidenceScore(analysis.confidenceScore())
            .damagePercentage(analysis.damagePercentage())
            .affectedAreaSqm(analysis.affectedAreaSqm())
            .preDisasterImageId(request.preDisasterImageId())
            .changeDetectionScore(analysis.changeDetectionScore())
            .analysisAlgorithm(request.analysisAlgorithm())
            .analysisParameters(request.analysisParameters())
            .assessedBy(request.assessedBy())
            .notes(request.notes())
            .build();
            
        return damageAssessmentRepository.save(assessment);
    }
    
    /**
     * Get damage assessments within a bounding box
     */
    public List<DamageAssessment> getDamageAssessmentsInBounds(double minLon, double minLat,
                                                              double maxLon, double maxLat) {
        return damageAssessmentRepository.findWithinBounds(minLon, minLat, maxLon, maxLat);
    }
    
    /**
     * Get damage assessments by type
     */
    public List<DamageAssessment> getDamageAssessmentsByType(DamageType damageType) {
        return damageAssessmentRepository.findByDamageType(damageType);
    }
    
    /**
     * Get damage assessments by severity
     */
    public List<DamageAssessment> getDamageAssessmentsBySeverity(DamageSeverity severity) {
        return damageAssessmentRepository.findBySeverity(severity);
    }
    
    /**
     * Get high confidence damage assessments
     */
    public List<DamageAssessment> getHighConfidenceAssessments() {
        return damageAssessmentRepository.findHighConfidenceAssessments();
    }
    
    /**
     * Get damage assessments by date range
     */
    public List<DamageAssessment> getDamageAssessmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return damageAssessmentRepository.findByAssessedAtBetween(startDate, endDate);
    }
    
    /**
     * Get damage assessment statistics
     */
    public DamageAssessmentStatistics getDamageStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        var stats = damageAssessmentRepository.getDamageStatistics(startDate, endDate);
        
        return new DamageAssessmentStatistics(
            stats.stream().mapToLong(row -> (Long) row[2]).sum(),
            stats.stream().mapToDouble(row -> (Double) row[3]).average().orElse(0.0),
            stats.stream().mapToDouble(row -> (Double) row[4]).average().orElse(0.0),
            stats.stream().mapToDouble(row -> (Double) row[5]).sum()
        );
    }
    
    /**
     * Get damage assessment summary for an area
     */
    public DamageAssessmentSummary getDamageSummary(double minLon, double minLat,
                                                   double maxLon, double maxLat,
                                                   LocalDateTime startDate, LocalDateTime endDate) {
        var summary = damageAssessmentRepository.getDamageSummary(
            minLon, minLat, maxLon, maxLat, startDate, endDate
        );
        
        return new DamageAssessmentSummary(
            summary.getTotalAssessments(),
            summary.getAvgConfidence(),
            summary.getAvgDamagePercentage(),
            summary.getTotalAffectedArea(),
            summary.getLatestAssessment()
        );
    }
    
    /**
     * Perform automated damage detection
     */
    @Transactional
    public List<DamageAssessment> performAutomatedDamageDetection(Long satelliteImageId, 
                                                                String algorithm) {
        log.info("Performing automated damage detection for image: {} using algorithm: {}", 
                satelliteImageId, algorithm);
        
        SatelliteImage image = satelliteImageRepository.findById(satelliteImageId)
            .orElseThrow(() -> new IllegalArgumentException("Satellite image not found"));
        
        // Simulate automated damage detection
        // In a real implementation, this would call external AI/ML services
        List<DamageDetectionResult> detections = detectDamageAutomatically(image, algorithm);
        
        List<DamageAssessment> assessments = detections.stream()
            .map(detection -> {
                Polygon damageArea = createDamageArea(detection.coordinates());
                
                return DamageAssessment.builder()
                    .satelliteImage(image)
                    .damageArea(damageArea)
                    .damageType(detection.damageType())
                    .severity(detection.severity())
                    .confidenceScore(detection.confidenceScore())
                    .damagePercentage(detection.damagePercentage())
                    .affectedAreaSqm(detection.affectedAreaSqm())
                    .analysisAlgorithm(algorithm)
                    .assessedBy("AUTOMATED_SYSTEM")
                    .notes("Automated damage detection result")
                    .build();
            })
            .toList();
            
        return damageAssessmentRepository.saveAll(assessments);
    }
    
    /**
     * Create damage area polygon from coordinates
     */
    private Polygon createDamageArea(List<Coordinate> coordinates) {
        if (coordinates.size() < 3) {
            throw new IllegalArgumentException("At least 3 coordinates required for damage area");
        }
        
        // Close the polygon if not already closed
        if (!coordinates.get(0).equals(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(coordinates.get(0));
        }
        
        Coordinate[] coordArray = coordinates.toArray(new Coordinate[0]);
        LinearRing ring = geometryFactory.createLinearRing(coordArray);
        return geometryFactory.createPolygon(ring);
    }
    
    /**
     * Analyze damage based on satellite imagery
     */
    private DamageAnalysisResult analyzeDamage(SatelliteImage image, Polygon damageArea, 
                                             DamageAssessmentRequest request) {
        // Calculate affected area in square meters
        double affectedAreaSqm = calculateAreaInSquareMeters(damageArea);
        
        // Determine damage type based on analysis parameters
        DamageType damageType = determineDamageType(request.analysisParameters());
        
        // Calculate damage percentage (simplified)
        double damagePercentage = calculateDamagePercentage(image, damageArea, damageType);
        
        // Determine severity based on damage percentage
        DamageSeverity severity = determineSeverity(damagePercentage);
        
        // Calculate confidence score
        double confidenceScore = calculateConfidenceScore(image, damageArea, damageType);
        
        // Calculate change detection score
        double changeDetectionScore = calculateChangeDetectionScore(image, request.preDisasterImageId());
        
        return new DamageAnalysisResult(
            damageType, severity, confidenceScore, damagePercentage,
            affectedAreaSqm, changeDetectionScore
        );
    }
    
    /**
     * Detect damage automatically using AI/ML algorithms
     */
    private List<DamageDetectionResult> detectDamageAutomatically(SatelliteImage image, String algorithm) {
        // This is a simplified simulation of automated damage detection
        // In a real implementation, this would integrate with AI/ML services
        
        log.info("Simulating automated damage detection for image: {}", image.getId());
        
        // Simulate detection results
        return List.of(
            new DamageDetectionResult(
                List.of(
                    new Coordinate(-74.0, 40.7),
                    new Coordinate(-73.9, 40.7),
                    new Coordinate(-73.9, 40.8),
                    new Coordinate(-74.0, 40.8)
                ),
                DamageType.BUILDING_COLLAPSE,
                DamageSeverity.MODERATE,
                0.85,
                45.0,
                10000.0
            )
        );
    }
    
    /**
     * Calculate area in square meters
     */
    private double calculateAreaInSquareMeters(Polygon polygon) {
        // Convert from degrees to meters (approximate)
        double areaDegrees = polygon.getArea();
        double areaSquareMeters = areaDegrees * 111000 * 111000; // Rough conversion
        return areaSquareMeters;
    }
    
    /**
     * Determine damage type from analysis parameters
     */
    private DamageType determineDamageType(String analysisParameters) {
        // Simplified logic - in real implementation, this would be more sophisticated
        if (analysisParameters != null && analysisParameters.contains("flood")) {
            return DamageType.FLOODING;
        } else if (analysisParameters != null && analysisParameters.contains("fire")) {
            return DamageType.FIRE;
        } else if (analysisParameters != null && analysisParameters.contains("building")) {
            return DamageType.BUILDING_COLLAPSE;
        }
        return DamageType.GENERAL;
    }
    
    /**
     * Calculate damage percentage
     */
    private double calculateDamagePercentage(SatelliteImage image, Polygon damageArea, DamageType damageType) {
        // Simplified calculation - in real implementation, this would analyze pixel data
        double basePercentage = 30.0; // Base damage percentage
        
        // Adjust based on image quality
        if (image.getQualityScore() != null && image.getQualityScore() < 0.5) {
            basePercentage += 20.0; // Lower quality images might show more damage
        }
        
        // Adjust based on damage type
        switch (damageType) {
            case BUILDING_COLLAPSE -> basePercentage += 20.0;
            case FLOODING -> basePercentage += 15.0;
            case FIRE -> basePercentage += 25.0;
            case LANDSLIDE -> basePercentage += 30.0;
            default -> basePercentage += 10.0;
        }
        
        return Math.min(100.0, basePercentage);
    }
    
    /**
     * Determine severity based on damage percentage
     */
    private DamageSeverity determineSeverity(double damagePercentage) {
        if (damagePercentage >= 81) return DamageSeverity.CATASTROPHIC;
        if (damagePercentage >= 61) return DamageSeverity.SEVERE;
        if (damagePercentage >= 41) return DamageSeverity.MODERATE;
        if (damagePercentage >= 21) return DamageSeverity.LIGHT;
        return DamageSeverity.MINIMAL;
    }
    
    /**
     * Calculate confidence score
     */
    private double calculateConfidenceScore(SatelliteImage image, Polygon damageArea, DamageType damageType) {
        double confidence = 0.5; // Base confidence
        
        // Adjust based on image quality
        if (image.getQualityScore() != null) {
            confidence += image.getQualityScore() * 0.3;
        }
        
        // Adjust based on cloud cover
        if (image.getCloudCoverPercentage() != null) {
            confidence -= image.getCloudCoverPercentage() / 200.0; // Penalize cloud cover
        }
        
        // Adjust based on resolution
        if (image.getResolutionMeters() < 5) {
            confidence += 0.2; // High resolution images are more reliable
        } else if (image.getResolutionMeters() > 20) {
            confidence -= 0.2; // Low resolution images are less reliable
        }
        
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    /**
     * Calculate change detection score
     */
    private double calculateChangeDetectionScore(SatelliteImage image, Long preDisasterImageId) {
        if (preDisasterImageId == null) {
            return 0.0; // No pre-disaster image for comparison
        }
        
        // Simplified calculation - in real implementation, this would compare images
        return 0.75; // Simulated change detection confidence
    }
    
    // Data classes
    public record DamageAssessmentRequest(
        Long satelliteImageId,
        List<Coordinate> damageCoordinates,
        Long preDisasterImageId,
        String analysisAlgorithm,
        String analysisParameters,
        String assessedBy,
        String notes
    ) {}
    
    public record DamageAnalysisResult(
        DamageType damageType,
        DamageSeverity severity,
        double confidenceScore,
        double damagePercentage,
        double affectedAreaSqm,
        double changeDetectionScore
    ) {}
    
    public record DamageDetectionResult(
        List<Coordinate> coordinates,
        DamageType damageType,
        DamageSeverity severity,
        double confidenceScore,
        double damagePercentage,
        double affectedAreaSqm
    ) {}
    
    public record DamageAssessmentStatistics(
        Long totalAssessments,
        double avgConfidence,
        double avgDamagePercentage,
        double totalAffectedArea
    ) {}
    
    public record DamageAssessmentSummary(
        Long totalAssessments,
        Double avgConfidence,
        Double avgDamagePercentage,
        Double totalAffectedArea,
        LocalDateTime latestAssessment
    ) {}
}
