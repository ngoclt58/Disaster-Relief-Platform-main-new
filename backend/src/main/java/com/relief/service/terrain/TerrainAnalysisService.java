package com.relief.service.terrain;

import com.relief.domain.terrain.ElevationPoint;
import com.relief.domain.terrain.TerrainAnalysis;
import com.relief.domain.terrain.TerrainAnalysisType;
import com.relief.repository.terrain.TerrainAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for terrain analysis operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TerrainAnalysisService {
    
    private final TerrainAnalysisRepository terrainAnalysisRepository;
    private final ElevationService elevationService;
    private final GeometryFactory geometryFactory;
    
    /**
     * Perform terrain analysis for a given area
     */
    @Transactional
    public TerrainAnalysis performTerrainAnalysis(Polygon area, TerrainAnalysisType analysisType) {
        log.info("Performing terrain analysis for area: {}, type: {}", area, analysisType);
        
        // Get elevation points within the area
        Envelope envelope = area.getEnvelopeInternal();
        List<ElevationPoint> elevationPoints = elevationService.getElevationPointsInBounds(
            envelope.getMinX(), envelope.getMinY(),
            envelope.getMaxX(), envelope.getMaxY()
        );
        
        if (elevationPoints.isEmpty()) {
            throw new IllegalArgumentException("No elevation data available for the specified area");
        }
        
        // Calculate terrain metrics
        TerrainMetrics metrics = calculateTerrainMetrics(elevationPoints);
        
        // Calculate analysis-specific scores
        double accessibilityScore = calculateAccessibilityScore(metrics, analysisType);
        double floodRiskScore = calculateFloodRiskScore(metrics, analysisType);
        
        // Create terrain analysis record
        TerrainAnalysis analysis = TerrainAnalysis.builder()
            .area(area)
            .analysisType(analysisType)
            .minElevation(metrics.minElevation)
            .maxElevation(metrics.maxElevation)
            .avgElevation(metrics.avgElevation)
            .elevationVariance(metrics.elevationVariance)
            .slopeAverage(metrics.slopeAverage)
            .slopeMaximum(metrics.slopeMaximum)
            .aspectAverage(metrics.aspectAverage)
            .roughnessIndex(metrics.roughnessIndex)
            .accessibilityScore(accessibilityScore)
            .floodRiskScore(floodRiskScore)
            .analysisData(metrics.toJson())
            .build();
            
        return terrainAnalysisRepository.save(analysis);
    }
    
    /**
     * Get terrain analysis for a point
     */
    public Optional<TerrainAnalysis> getTerrainAnalysisForPoint(double longitude, double latitude) {
        return terrainAnalysisRepository.findMostRecentForPoint(longitude, latitude);
    }
    
    /**
     * Get terrain analysis for an area
     */
    public List<TerrainAnalysis> getTerrainAnalysisForArea(Polygon area) {
        return terrainAnalysisRepository.findByPolygonIntersection(area);
    }
    
    /**
     * Find accessible areas based on criteria
     */
    public List<TerrainAnalysis> findAccessibleAreas(double minAccessibilityScore, double maxSlope) {
        return terrainAnalysisRepository.findByAccessibilityScoreRange(minAccessibilityScore, 1.0)
            .stream()
            .filter(analysis -> analysis.getSlopeMaximum() <= maxSlope)
            .toList();
    }
    
    /**
     * Find flood-prone areas
     */
    public List<TerrainAnalysis> findFloodProneAreas(double minFloodRiskScore) {
        return terrainAnalysisRepository.findByFloodRiskScoreRange(minFloodRiskScore, 1.0);
    }
    
    /**
     * Calculate terrain metrics from elevation points
     */
    private TerrainMetrics calculateTerrainMetrics(List<ElevationPoint> elevationPoints) {
        if (elevationPoints.isEmpty()) {
            return new TerrainMetrics();
        }
        
        double minElevation = elevationPoints.stream()
            .mapToDouble(ElevationPoint::getElevation)
            .min().orElse(0.0);
            
        double maxElevation = elevationPoints.stream()
            .mapToDouble(ElevationPoint::getElevation)
            .max().orElse(0.0);
            
        double avgElevation = elevationPoints.stream()
            .mapToDouble(ElevationPoint::getElevation)
            .average().orElse(0.0);
            
        double variance = elevationPoints.stream()
            .mapToDouble(point -> Math.pow(point.getElevation() - avgElevation, 2))
            .average().orElse(0.0);
            
        // Calculate slope and aspect for each point pair
        double totalSlope = 0;
        double maxSlope = 0;
        double totalAspect = 0;
        int slopeCount = 0;
        
        for (int i = 0; i < elevationPoints.size() - 1; i++) {
            ElevationPoint point1 = elevationPoints.get(i);
            ElevationPoint point2 = elevationPoints.get(i + 1);
            
            double slope = elevationService.calculateSlope(
                point1.getLocation().getX(), point1.getLocation().getY(), point1.getElevation(),
                point2.getLocation().getX(), point2.getLocation().getY(), point2.getElevation()
            );
            
            double aspect = elevationService.calculateAspect(
                point1.getLocation().getX(), point1.getLocation().getY(),
                point2.getLocation().getX(), point2.getLocation().getY()
            );
            
            totalSlope += Math.abs(slope);
            maxSlope = Math.max(maxSlope, Math.abs(slope));
            totalAspect += aspect;
            slopeCount++;
        }
        
        double avgSlope = slopeCount > 0 ? totalSlope / slopeCount : 0;
        double avgAspect = slopeCount > 0 ? totalAspect / slopeCount : 0;
        
        // Calculate roughness index (standard deviation of elevation differences)
        double roughness = Math.sqrt(variance);
        
        return new TerrainMetrics(
            minElevation, maxElevation, avgElevation, variance,
            avgSlope, maxSlope, avgAspect, roughness
        );
    }
    
    /**
     * Calculate accessibility score based on terrain metrics
     */
    private double calculateAccessibilityScore(TerrainMetrics metrics, TerrainAnalysisType analysisType) {
        double score = 1.0;
        
        // Penalize high slopes
        if (metrics.slopeMaximum > 30) {
            score -= 0.5;
        } else if (metrics.slopeMaximum > 15) {
            score -= 0.3;
        } else if (metrics.slopeMaximum > 5) {
            score -= 0.1;
        }
        
        // Penalize high roughness
        if (metrics.roughnessIndex > 100) {
            score -= 0.3;
        } else if (metrics.roughnessIndex > 50) {
            score -= 0.2;
        } else if (metrics.roughnessIndex > 20) {
            score -= 0.1;
        }
        
        // Adjust based on analysis type
        switch (analysisType) {
            case EMERGENCY_RESPONSE:
                // Emergency response requires high accessibility
                if (metrics.slopeMaximum > 10) score -= 0.2;
                break;
            case ROUTING:
                // Routing can handle moderate slopes
                if (metrics.slopeMaximum > 20) score -= 0.2;
                break;
            case ACCESSIBILITY:
                // Accessibility analysis is strictest
                if (metrics.slopeMaximum > 5) score -= 0.3;
                break;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Calculate flood risk score based on terrain metrics
     */
    private double calculateFloodRiskScore(TerrainMetrics metrics, TerrainAnalysisType analysisType) {
        double score = 0.0;
        
        // Lower elevation areas are more flood-prone
        if (metrics.avgElevation < 10) {
            score += 0.8;
        } else if (metrics.avgElevation < 50) {
            score += 0.5;
        } else if (metrics.avgElevation < 100) {
            score += 0.2;
        }
        
        // Flat areas are more flood-prone
        if (metrics.slopeMaximum < 2) {
            score += 0.3;
        } else if (metrics.slopeMaximum < 5) {
            score += 0.1;
        }
        
        // Low elevation variance indicates flat terrain
        if (metrics.elevationVariance < 100) {
            score += 0.2;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Data class for terrain metrics
     */
    private record TerrainMetrics(
        double minElevation,
        double maxElevation,
        double avgElevation,
        double elevationVariance,
        double slopeAverage,
        double slopeMaximum,
        double aspectAverage,
        double roughnessIndex
    ) {
        public TerrainMetrics() {
            this(0, 0, 0, 0, 0, 0, 0, 0);
        }
        
        public String toJson() {
            return String.format("""
                {
                    "minElevation": %.2f,
                    "maxElevation": %.2f,
                    "avgElevation": %.2f,
                    "elevationVariance": %.2f,
                    "slopeAverage": %.2f,
                    "slopeMaximum": %.2f,
                    "aspectAverage": %.2f,
                    "roughnessIndex": %.2f
                }
                """, minElevation, maxElevation, avgElevation, elevationVariance,
                slopeAverage, slopeMaximum, aspectAverage, roughnessIndex);
        }
    }
}



