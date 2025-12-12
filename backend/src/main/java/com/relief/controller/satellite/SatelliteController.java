package com.relief.controller.satellite;

import com.relief.service.satellite.DamageAssessmentService;
import com.relief.service.satellite.SatelliteImageryService;
import com.relief.domain.satellite.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for satellite imagery and damage assessment
 */
@RestController
@RequestMapping("/satellite")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Satellite Imagery", description = "Satellite imagery and damage assessment services")
public class SatelliteController {
    
    private final SatelliteImageryService satelliteImageryService;
    private final DamageAssessmentService damageAssessmentService;
    
    // Satellite Image Endpoints
    
    /**
     * Add satellite image
     */
    @PostMapping("/images")
    @Operation(summary = "Add satellite image", description = "Add a new satellite image to the system")
    public ResponseEntity<SatelliteImageResponse> addSatelliteImage(
            @Valid @RequestBody SatelliteImageRequest request) {
        
        var imageData = new SatelliteImageryService.SatelliteImageData(
            request.imageUrl(),
            request.thumbnailUrl(),
            request.minLon(), request.minLat(), request.maxLon(), request.maxLat(),
            request.provider(),
            request.satelliteName(),
            request.capturedAt(),
            request.resolutionMeters(),
            request.cloudCoverPercentage(),
            request.sunElevationAngle(),
            request.sunAzimuthAngle(),
            request.imageBands(),
            request.metadata()
        );
        
        var image = satelliteImageryService.addSatelliteImage(imageData);
        
        var response = new SatelliteImageResponse(
            image.getId(),
            image.getImageUrl(),
            image.getThumbnailUrl(),
            image.getProvider().name(),
            image.getSatelliteName(),
            image.getCapturedAt(),
            image.getResolutionMeters(),
            image.getCloudCoverPercentage(),
            image.getSunElevationAngle(),
            image.getSunAzimuthAngle(),
            image.getImageBands(),
            image.getMetadata(),
            image.getProcessingStatus().name(),
            image.getQualityScore()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get satellite images within bounds
     */
    @GetMapping("/images/bounds")
    @Operation(summary = "Get satellite images in bounds", description = "Get satellite images within a bounding box")
    public ResponseEntity<List<SatelliteImageResponse>> getImagesInBounds(
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var images = satelliteImageryService.getImagesInBounds(minLon, minLat, maxLon, maxLat);
        
        List<SatelliteImageResponse> responses = images.stream()
            .map(image -> new SatelliteImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getThumbnailUrl(),
                image.getProvider().name(),
                image.getSatelliteName(),
                image.getCapturedAt(),
                image.getResolutionMeters(),
                image.getCloudCoverPercentage(),
                image.getSunElevationAngle(),
                image.getSunAzimuthAngle(),
                image.getImageBands(),
                image.getMetadata(),
                image.getProcessingStatus().name(),
                image.getQualityScore()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get satellite images by provider
     */
    @GetMapping("/images/provider/{provider}")
    @Operation(summary = "Get images by provider", description = "Get satellite images by provider")
    public ResponseEntity<List<SatelliteImageResponse>> getImagesByProvider(
            @PathVariable SatelliteProvider provider) {
        
        var images = satelliteImageryService.getImagesByProvider(provider);
        
        List<SatelliteImageResponse> responses = images.stream()
            .map(image -> new SatelliteImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getThumbnailUrl(),
                image.getProvider().name(),
                image.getSatelliteName(),
                image.getCapturedAt(),
                image.getResolutionMeters(),
                image.getCloudCoverPercentage(),
                image.getSunElevationAngle(),
                image.getSunAzimuthAngle(),
                image.getImageBands(),
                image.getMetadata(),
                image.getProcessingStatus().name(),
                image.getQualityScore()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get most recent satellite image for a point
     */
    @GetMapping("/images/recent")
    @Operation(summary = "Get most recent image", description = "Get the most recent satellite image for a point")
    public ResponseEntity<SatelliteImageResponse> getMostRecentImage(
            @Parameter(description = "Longitude") @RequestParam double longitude,
            @Parameter(description = "Latitude") @RequestParam double latitude) {
        
        var image = satelliteImageryService.getMostRecentImage(longitude, latitude);
        
        if (image.isPresent()) {
            var response = new SatelliteImageResponse(
                image.get().getId(),
                image.get().getImageUrl(),
                image.get().getThumbnailUrl(),
                image.get().getProvider().name(),
                image.get().getSatelliteName(),
                image.get().getCapturedAt(),
                image.get().getResolutionMeters(),
                image.get().getCloudCoverPercentage(),
                image.get().getSunElevationAngle(),
                image.get().getSunAzimuthAngle(),
                image.get().getImageBands(),
                image.get().getMetadata(),
                image.get().getProcessingStatus().name(),
                image.get().getQualityScore()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get satellite image statistics
     */
    @GetMapping("/images/statistics")
    @Operation(summary = "Get image statistics", description = "Get satellite image statistics for a date range")
    public ResponseEntity<SatelliteImageStatisticsResponse> getImageStatistics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        var stats = satelliteImageryService.getStatistics(startDate, endDate);
        
        var response = new SatelliteImageStatisticsResponse(
            stats.totalImages(),
            stats.avgResolution(),
            stats.avgCloudCover(),
            stats.avgQuality(),
            stats.earliestCapture(),
            stats.latestCapture()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update processing status
     */
    @PutMapping("/images/{id}/status")
    @Operation(summary = "Update processing status", description = "Update the processing status of a satellite image")
    public ResponseEntity<Void> updateProcessingStatus(
            @PathVariable Long id,
            @RequestParam ProcessingStatus status) {
        
        satelliteImageryService.updateProcessingStatus(id, status);
        return ResponseEntity.ok().build();
    }
    
    // Damage Assessment Endpoints
    
    /**
     * Perform damage assessment
     */
    @PostMapping("/damage-assessment")
    @Operation(summary = "Perform damage assessment", description = "Perform damage assessment on satellite imagery")
    public ResponseEntity<DamageAssessmentResponse> performDamageAssessment(
            @Valid @RequestBody DamageAssessmentRequest request) {
        
        var assessmentRequest = new DamageAssessmentService.DamageAssessmentRequest(
            request.satelliteImageId(),
            request.damageCoordinates().stream()
                .map(coord -> new Coordinate(coord.longitude(), coord.latitude()))
                .toList(),
            request.preDisasterImageId(),
            request.analysisAlgorithm(),
            request.analysisParameters(),
            request.assessedBy(),
            request.notes()
        );
        
        var assessment = damageAssessmentService.performDamageAssessment(assessmentRequest);
        
        var response = new DamageAssessmentResponse(
            assessment.getId(),
            assessment.getSatelliteImage().getId(),
            assessment.getDamageType().name(),
            assessment.getSeverity().name(),
            assessment.getConfidenceScore(),
            assessment.getDamagePercentage(),
            assessment.getAffectedAreaSqm(),
            assessment.getPreDisasterImageId(),
            assessment.getChangeDetectionScore(),
            assessment.getAnalysisAlgorithm(),
            assessment.getAnalysisParameters(),
            assessment.getAssessedAt(),
            assessment.getAssessedBy(),
            assessment.getNotes()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get damage assessments within bounds
     */
    @GetMapping("/damage-assessment/bounds")
    @Operation(summary = "Get damage assessments in bounds", description = "Get damage assessments within a bounding box")
    public ResponseEntity<List<DamageAssessmentResponse>> getDamageAssessmentsInBounds(
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var assessments = damageAssessmentService.getDamageAssessmentsInBounds(minLon, minLat, maxLon, maxLat);
        
        List<DamageAssessmentResponse> responses = assessments.stream()
            .map(assessment -> new DamageAssessmentResponse(
                assessment.getId(),
                assessment.getSatelliteImage().getId(),
                assessment.getDamageType().name(),
                assessment.getSeverity().name(),
                assessment.getConfidenceScore(),
                assessment.getDamagePercentage(),
                assessment.getAffectedAreaSqm(),
                assessment.getPreDisasterImageId(),
                assessment.getChangeDetectionScore(),
                assessment.getAnalysisAlgorithm(),
                assessment.getAnalysisParameters(),
                assessment.getAssessedAt(),
                assessment.getAssessedBy(),
                assessment.getNotes()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get damage assessments by type
     */
    @GetMapping("/damage-assessment/type/{type}")
    @Operation(summary = "Get damage assessments by type", description = "Get damage assessments by damage type")
    public ResponseEntity<List<DamageAssessmentResponse>> getDamageAssessmentsByType(
            @PathVariable DamageType type) {
        
        var assessments = damageAssessmentService.getDamageAssessmentsByType(type);
        
        List<DamageAssessmentResponse> responses = assessments.stream()
            .map(assessment -> new DamageAssessmentResponse(
                assessment.getId(),
                assessment.getSatelliteImage().getId(),
                assessment.getDamageType().name(),
                assessment.getSeverity().name(),
                assessment.getConfidenceScore(),
                assessment.getDamagePercentage(),
                assessment.getAffectedAreaSqm(),
                assessment.getPreDisasterImageId(),
                assessment.getChangeDetectionScore(),
                assessment.getAnalysisAlgorithm(),
                assessment.getAnalysisParameters(),
                assessment.getAssessedAt(),
                assessment.getAssessedBy(),
                assessment.getNotes()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get damage assessment statistics
     */
    @GetMapping("/damage-assessment/statistics")
    @Operation(summary = "Get damage assessment statistics", description = "Get damage assessment statistics for a date range")
    public ResponseEntity<DamageAssessmentStatisticsResponse> getDamageStatistics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        var stats = damageAssessmentService.getDamageStatistics(startDate, endDate);
        
        var response = new DamageAssessmentStatisticsResponse(
            stats.totalAssessments(),
            stats.avgConfidence(),
            stats.avgDamagePercentage(),
            stats.totalAffectedArea()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Perform automated damage detection
     */
    @PostMapping("/damage-assessment/automated")
    @Operation(summary = "Automated damage detection", description = "Perform automated damage detection using AI/ML")
    public ResponseEntity<List<DamageAssessmentResponse>> performAutomatedDamageDetection(
            @RequestParam Long satelliteImageId,
            @RequestParam String algorithm) {
        
        var assessments = damageAssessmentService.performAutomatedDamageDetection(satelliteImageId, algorithm);
        
        List<DamageAssessmentResponse> responses = assessments.stream()
            .map(assessment -> new DamageAssessmentResponse(
                assessment.getId(),
                assessment.getSatelliteImage().getId(),
                assessment.getDamageType().name(),
                assessment.getSeverity().name(),
                assessment.getConfidenceScore(),
                assessment.getDamagePercentage(),
                assessment.getAffectedAreaSqm(),
                assessment.getPreDisasterImageId(),
                assessment.getChangeDetectionScore(),
                assessment.getAnalysisAlgorithm(),
                assessment.getAnalysisParameters(),
                assessment.getAssessedAt(),
                assessment.getAssessedBy(),
                assessment.getNotes()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    // Request/Response DTOs
    
    public record SatelliteImageRequest(
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
    
    public record SatelliteImageResponse(
        Long id,
        String imageUrl,
        String thumbnailUrl,
        String provider,
        String satelliteName,
        LocalDateTime capturedAt,
        double resolutionMeters,
        Double cloudCoverPercentage,
        Double sunElevationAngle,
        Double sunAzimuthAngle,
        String imageBands,
        String metadata,
        String processingStatus,
        Double qualityScore
    ) {}
    
    public record SatelliteImageStatisticsResponse(
        Long totalImages,
        Double avgResolution,
        Double avgCloudCover,
        Double avgQuality,
        LocalDateTime earliestCapture,
        LocalDateTime latestCapture
    ) {}
    
    public record DamageAssessmentRequest(
        Long satelliteImageId,
        List<CoordinateRequest> damageCoordinates,
        Long preDisasterImageId,
        String analysisAlgorithm,
        String analysisParameters,
        String assessedBy,
        String notes
    ) {}
    
    public record CoordinateRequest(double longitude, double latitude) {}
    
    public record DamageAssessmentResponse(
        Long id,
        Long satelliteImageId,
        String damageType,
        String severity,
        double confidenceScore,
        Double damagePercentage,
        Double affectedAreaSqm,
        Long preDisasterImageId,
        Double changeDetectionScore,
        String analysisAlgorithm,
        String analysisParameters,
        LocalDateTime assessedAt,
        String assessedBy,
        String notes
    ) {}
    
    public record DamageAssessmentStatisticsResponse(
        Long totalAssessments,
        double avgConfidence,
        double avgDamagePercentage,
        double totalAffectedArea
    ) {}
}



