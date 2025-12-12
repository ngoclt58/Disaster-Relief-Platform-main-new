package com.relief.controller.realtime;

import com.relief.service.realtime.AnomalyDetectionService;
import com.relief.service.realtime.AnomalyDetectionService.AnomalyDetector;
import com.relief.service.realtime.AnomalyDetectionService.DataPoint;
import com.relief.service.realtime.AnomalyDetectionService.DetectionModel;
import com.relief.service.realtime.AnomalyDetectionService.Anomaly;
import com.relief.service.realtime.AnomalyDetectionService.AnomalySummary;
import com.relief.service.realtime.AnomalyDetectionService.AnomalyPattern;
import com.relief.service.realtime.AnomalyDetectionService.DetectionAnalytics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Anomaly detection controller for automatic detection of unusual patterns or behaviors
 */
@RestController
@RequestMapping("/api/realtime/anomaly-detection")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Anomaly Detection", description = "Automatic detection of unusual patterns or behaviors")
public class AnomalyDetectionController {

    private final AnomalyDetectionService anomalyDetectionService;

    @PostMapping("/detectors")
    @Operation(summary = "Create anomaly detector")
    public ResponseEntity<AnomalyDetector> createDetector(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String dataSource,
            @RequestParam String detectionType,
            @RequestBody Map<String, Object> configuration) {
        
        AnomalyDetector detector = anomalyDetectionService.createDetector(name, description, dataSource, detectionType, configuration);
        return ResponseEntity.ok(detector);
    }

    @PostMapping("/models")
    @Operation(summary = "Train detection model")
    public ResponseEntity<DetectionModel> trainModel(
            @RequestParam String detectorId,
            @RequestBody List<DataPoint> trainingData,
            @RequestBody Map<String, Object> parameters) {
        
        DetectionModel model = anomalyDetectionService.trainModel(detectorId, trainingData, parameters);
        return ResponseEntity.ok(model);
    }

    @PostMapping("/detect")
    @Operation(summary = "Detect anomaly")
    public ResponseEntity<Anomaly> detectAnomaly(
            @RequestParam String detectorId,
            @RequestBody DataPoint dataPoint) {
        
        Anomaly anomaly = anomalyDetectionService.detectAnomaly(detectorId, dataPoint);
        return ResponseEntity.ok(anomaly);
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Get anomalies")
    public ResponseEntity<List<Anomaly>> getAnomalies(
            @RequestParam String detectorId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<Anomaly> anomalies = anomalyDetectionService.getAnomalies(detectorId, start, end);
        return ResponseEntity.ok(anomalies);
    }

    @GetMapping("/anomalies/summary")
    @Operation(summary = "Get anomaly summary")
    public ResponseEntity<AnomalySummary> getAnomalySummary(
            @RequestParam String detectorId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        AnomalySummary summary = anomalyDetectionService.getAnomalySummary(detectorId, start, end);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/anomalies/{anomalyId}/resolve")
    @Operation(summary = "Resolve anomaly")
    public ResponseEntity<Void> resolveAnomaly(
            @PathVariable String anomalyId,
            @RequestParam String resolution,
            @RequestParam String resolvedBy) {
        
        anomalyDetectionService.resolveAnomaly(anomalyId, resolution, resolvedBy);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/patterns/detect")
    @Operation(summary = "Detect anomaly pattern")
    public ResponseEntity<AnomalyPattern> detectPattern(
            @RequestParam String detectorId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        AnomalyPattern pattern = anomalyDetectionService.detectPattern(detectorId, start, end);
        return ResponseEntity.ok(pattern);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get detection analytics")
    public ResponseEntity<DetectionAnalytics> getAnalytics(@RequestParam String detectorId) {
        DetectionAnalytics analytics = anomalyDetectionService.getAnalytics(detectorId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/detectors/{detectorId}")
    @Operation(summary = "Get anomaly detector")
    public ResponseEntity<AnomalyDetector> getDetector(@PathVariable String detectorId) {
        AnomalyDetector detector = anomalyDetectionService.getDetector(detectorId);
        return ResponseEntity.ok(detector);
    }

    @GetMapping("/detectors")
    @Operation(summary = "Get all anomaly detectors")
    public ResponseEntity<List<AnomalyDetector>> getDetectors() {
        List<AnomalyDetector> detectors = anomalyDetectionService.getDetectors();
        return ResponseEntity.ok(detectors);
    }

    @PutMapping("/detectors/{detectorId}")
    @Operation(summary = "Update anomaly detector")
    public ResponseEntity<Void> updateDetector(
            @PathVariable String detectorId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestBody Map<String, Object> configuration,
            @RequestParam double sensitivity) {
        
        anomalyDetectionService.updateDetector(detectorId, name, description, configuration, sensitivity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/detectors/{detectorId}")
    @Operation(summary = "Delete anomaly detector")
    public ResponseEntity<Void> deleteDetector(@PathVariable String detectorId) {
        anomalyDetectionService.deleteDetector(detectorId);
        return ResponseEntity.ok().build();
    }
}


