package com.relief.controller.geofencing;

import com.relief.service.geofencing.GeofencingService;
import com.relief.domain.geofencing.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for geofencing operations
 */
@RestController
@RequestMapping("/geofencing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Geofencing", description = "Geofencing and automated monitoring services")
public class GeofencingController {
    
    private static final Logger log = LoggerFactory.getLogger(GeofencingController.class);
    
    private final GeofencingService geofencingService;
    
    // Geofence Management Endpoints
    
    /**
     * Create a new geofence
     */
    @PostMapping("/geofences")
    @Operation(summary = "Create geofence", description = "Create a new geofence for monitoring")
    public ResponseEntity<GeofenceResponse> createGeofence(
            @Valid @RequestBody GeofenceRequest request) {
        
        var geofenceRequest = new GeofencingService.GeofenceRequest(
            request.name(),
            request.description(),
            request.boundaryCoordinates().stream()
                .map(coord -> new Coordinate(coord.longitude(), coord.latitude()))
                .toList(),
            request.geofenceType(),
            request.priority(),
            request.isActive(),
            request.bufferDistanceMeters(),
            request.checkIntervalSeconds(),
            request.alertThreshold(),
            request.cooldownPeriodSeconds(),
            request.notificationChannels(),
            request.autoActions(),
            request.metadata(),
            request.createdBy()
        );
        
        var geofence = geofencingService.createGeofence(geofenceRequest);
        
        var response = new GeofenceResponse(
            geofence.getId(),
            geofence.getName(),
            geofence.getDescription(),
            geofence.getGeofenceType().name(),
            geofence.getPriority().name(),
            geofence.getIsActive(),
            geofence.getBufferDistanceMeters(),
            geofence.getCheckIntervalSeconds(),
            geofence.getAlertThreshold(),
            geofence.getCooldownPeriodSeconds(),
            geofence.getNotificationChannels(),
            geofence.getAutoActions(),
            geofence.getMetadata(),
            geofence.getCreatedBy(),
            geofence.getCreatedAt(),
            geofence.getUpdatedAt(),
            geofence.getLastCheckedAt(),
            geofence.getLastAlertAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update an existing geofence
     */
    @PutMapping("/geofences/{id}")
    @Operation(summary = "Update geofence", description = "Update an existing geofence")
    public ResponseEntity<GeofenceResponse> updateGeofence(
            @PathVariable Long id,
            @Valid @RequestBody GeofenceRequest request) {
        
        var geofenceRequest = new GeofencingService.GeofenceRequest(
            request.name(),
            request.description(),
            request.boundaryCoordinates().stream()
                .map(coord -> new Coordinate(coord.longitude(), coord.latitude()))
                .toList(),
            request.geofenceType(),
            request.priority(),
            request.isActive(),
            request.bufferDistanceMeters(),
            request.checkIntervalSeconds(),
            request.alertThreshold(),
            request.cooldownPeriodSeconds(),
            request.notificationChannels(),
            request.autoActions(),
            request.metadata(),
            request.createdBy()
        );
        
        var geofence = geofencingService.updateGeofence(id, geofenceRequest);
        
        var response = new GeofenceResponse(
            geofence.getId(),
            geofence.getName(),
            geofence.getDescription(),
            geofence.getGeofenceType().name(),
            geofence.getPriority().name(),
            geofence.getIsActive(),
            geofence.getBufferDistanceMeters(),
            geofence.getCheckIntervalSeconds(),
            geofence.getAlertThreshold(),
            geofence.getCooldownPeriodSeconds(),
            geofence.getNotificationChannels(),
            geofence.getAutoActions(),
            geofence.getMetadata(),
            geofence.getCreatedBy(),
            geofence.getCreatedAt(),
            geofence.getUpdatedAt(),
            geofence.getLastCheckedAt(),
            geofence.getLastAlertAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a geofence
     */
    @DeleteMapping("/geofences/{id}")
    @Operation(summary = "Delete geofence", description = "Delete a geofence")
    public ResponseEntity<Void> deleteGeofence(@PathVariable Long id) {
        geofencingService.deleteGeofence(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get geofence by ID
     */
    @GetMapping("/geofences/{id}")
    @Operation(summary = "Get geofence", description = "Get a specific geofence")
    public ResponseEntity<GeofenceResponse> getGeofence(@PathVariable Long id) {
        var geofence = geofencingService.getGeofence(id);
        
        if (geofence.isPresent()) {
            var geofenceData = geofence.get();
            var response = new GeofenceResponse(
                geofenceData.getId(),
                geofenceData.getName(),
                geofenceData.getDescription(),
                geofenceData.getGeofenceType().name(),
                geofenceData.getPriority().name(),
                geofenceData.getIsActive(),
                geofenceData.getBufferDistanceMeters(),
                geofenceData.getCheckIntervalSeconds(),
                geofenceData.getAlertThreshold(),
                geofenceData.getCooldownPeriodSeconds(),
                geofenceData.getNotificationChannels(),
                geofenceData.getAutoActions(),
                geofenceData.getMetadata(),
                geofenceData.getCreatedBy(),
                geofenceData.getCreatedAt(),
                geofenceData.getUpdatedAt(),
                geofenceData.getLastCheckedAt(),
                geofenceData.getLastAlertAt()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all geofences
     */
    @GetMapping("/geofences")
    @Operation(summary = "Get all geofences", description = "Get all geofences")
    public ResponseEntity<List<GeofenceResponse>> getAllGeofences() {
        var geofences = geofencingService.getAllGeofences();
        
        List<GeofenceResponse> responses = geofences.stream()
            .map(geofence -> new GeofenceResponse(
                geofence.getId(),
                geofence.getName(),
                geofence.getDescription(),
                geofence.getGeofenceType().name(),
                geofence.getPriority().name(),
                geofence.getIsActive(),
                geofence.getBufferDistanceMeters(),
                geofence.getCheckIntervalSeconds(),
                geofence.getAlertThreshold(),
                geofence.getCooldownPeriodSeconds(),
                geofence.getNotificationChannels(),
                geofence.getAutoActions(),
                geofence.getMetadata(),
                geofence.getCreatedBy(),
                geofence.getCreatedAt(),
                geofence.getUpdatedAt(),
                geofence.getLastCheckedAt(),
                geofence.getLastAlertAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get active geofences
     */
    @GetMapping("/geofences/active")
    @Operation(summary = "Get active geofences", description = "Get all active geofences")
    public ResponseEntity<List<GeofenceResponse>> getActiveGeofences() {
        var geofences = geofencingService.getActiveGeofences();
        
        List<GeofenceResponse> responses = geofences.stream()
            .map(geofence -> new GeofenceResponse(
                geofence.getId(),
                geofence.getName(),
                geofence.getDescription(),
                geofence.getGeofenceType().name(),
                geofence.getPriority().name(),
                geofence.getIsActive(),
                geofence.getBufferDistanceMeters(),
                geofence.getCheckIntervalSeconds(),
                geofence.getAlertThreshold(),
                geofence.getCooldownPeriodSeconds(),
                geofence.getNotificationChannels(),
                geofence.getAutoActions(),
                geofence.getMetadata(),
                geofence.getCreatedBy(),
                geofence.getCreatedAt(),
                geofence.getUpdatedAt(),
                geofence.getLastCheckedAt(),
                geofence.getLastAlertAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get geofences by type
     */
    @GetMapping("/geofences/type/{type}")
    @Operation(summary = "Get geofences by type", description = "Get geofences by type")
    public ResponseEntity<List<GeofenceResponse>> getGeofencesByType(
            @PathVariable GeofenceType type) {
        
        var geofences = geofencingService.getGeofencesByType(type);
        
        List<GeofenceResponse> responses = geofences.stream()
            .map(geofence -> new GeofenceResponse(
                geofence.getId(),
                geofence.getName(),
                geofence.getDescription(),
                geofence.getGeofenceType().name(),
                geofence.getPriority().name(),
                geofence.getIsActive(),
                geofence.getBufferDistanceMeters(),
                geofence.getCheckIntervalSeconds(),
                geofence.getAlertThreshold(),
                geofence.getCooldownPeriodSeconds(),
                geofence.getNotificationChannels(),
                geofence.getAutoActions(),
                geofence.getMetadata(),
                geofence.getCreatedBy(),
                geofence.getCreatedAt(),
                geofence.getUpdatedAt(),
                geofence.getLastCheckedAt(),
                geofence.getLastAlertAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get geofences within bounds
     */
    @GetMapping("/geofences/bounds")
    @Operation(summary = "Get geofences in bounds", description = "Get geofences within bounds")
    public ResponseEntity<List<GeofenceResponse>> getGeofencesWithinBounds(
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var geofences = geofencingService.getGeofencesWithinBounds(minLon, minLat, maxLon, maxLat);
        
        List<GeofenceResponse> responses = geofences.stream()
            .map(geofence -> new GeofenceResponse(
                geofence.getId(),
                geofence.getName(),
                geofence.getDescription(),
                geofence.getGeofenceType().name(),
                geofence.getPriority().name(),
                geofence.getIsActive(),
                geofence.getBufferDistanceMeters(),
                geofence.getCheckIntervalSeconds(),
                geofence.getAlertThreshold(),
                geofence.getCooldownPeriodSeconds(),
                geofence.getNotificationChannels(),
                geofence.getAutoActions(),
                geofence.getMetadata(),
                geofence.getCreatedBy(),
                geofence.getCreatedAt(),
                geofence.getUpdatedAt(),
                geofence.getLastCheckedAt(),
                geofence.getLastAlertAt()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    // Geofence Monitoring Endpoints
    
    /**
     * Check if a point is within any geofence
     */
    @PostMapping("/check")
    @Operation(summary = "Check point in geofences", description = "Check if a point is within any geofence")
    public ResponseEntity<GeofenceCheckResponse> checkPointInGeofences(
            @Valid @RequestBody GeofenceCheckRequest request) {
        
        var result = geofencingService.checkPointInGeofences(
            request.longitude(),
            request.latitude(),
            request.entityType(),
            request.entityId(),
            request.entityName()
        );
        
        var response = new GeofenceCheckResponse(
            result.isInGeofence(),
            result.geofence() != null ? result.geofence().getId() : null,
            result.geofence() != null ? result.geofence().getName() : null,
            result.event() != null ? result.event().getId() : null,
            result.event() != null ? result.event().getEventType().name() : null
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Process geofence events
     */
    @PostMapping("/process-events")
    @Operation(summary = "Process geofence events", description = "Process geofence events and generate alerts")
    public ResponseEntity<Void> processGeofenceEvents() {
        geofencingService.processGeofenceEvents();
        return ResponseEntity.ok().build();
    }
    
    // Geofence Events Endpoints
    
    /**
     * Get geofence events
     */
    @GetMapping("/geofences/{geofenceId}/events")
    @Operation(summary = "Get geofence events", description = "Get events for a specific geofence")
    public ResponseEntity<List<GeofenceEventResponse>> getGeofenceEvents(
            @PathVariable Long geofenceId) {
        
        var events = geofencingService.getGeofenceEvents(geofenceId);
        
        List<GeofenceEventResponse> responses = events.stream()
            .map(event -> new GeofenceEventResponse(
                event.getId(),
                event.getGeofence().getId(),
                event.getEventType().name(),
                event.getLocation().getX(),
                event.getLocation().getY(),
                event.getEntityType(),
                event.getEntityId(),
                event.getEntityName(),
                event.getEventData(),
                event.getSeverity().name(),
                event.getConfidenceScore(),
                event.getOccurredAt(),
                event.getDetectedAt(),
                event.getProcessedAt(),
                event.getIsProcessed(),
                event.getProcessingNotes()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    // Geofence Alerts Endpoints
    
    /**
     * Get geofence alerts
     */
    @GetMapping("/geofences/{geofenceId}/alerts")
    @Operation(summary = "Get geofence alerts", description = "Get alerts for a specific geofence")
    public ResponseEntity<List<GeofenceAlertResponse>> getGeofenceAlerts(
            @PathVariable Long geofenceId) {
        
        var alerts = geofencingService.getGeofenceAlerts(geofenceId);
        
        List<GeofenceAlertResponse> responses = alerts.stream()
            .map(alert -> new GeofenceAlertResponse(
                alert.getId(),
                alert.getGeofence().getId(),
                alert.getAlertType().name(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getSeverity().name(),
                alert.getStatus().name(),
                alert.getTriggeredByEventId(),
                alert.getAlertData(),
                alert.getNotificationChannels(),
                alert.getAutoActionsTriggered(),
                alert.getAssignedTo(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgedBy(),
                alert.getResolvedAt(),
                alert.getResolvedBy(),
                alert.getResolutionNotes(),
                alert.getEscalatedAt(),
                alert.getEscalatedTo(),
                alert.getEscalationReason()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get active alerts
     */
    @GetMapping("/alerts/active")
    @Operation(summary = "Get active alerts", description = "Get all active alerts")
    public ResponseEntity<List<GeofenceAlertResponse>> getActiveAlerts() {
        var alerts = geofencingService.getActiveAlerts();
        
        List<GeofenceAlertResponse> responses = alerts.stream()
            .map(alert -> new GeofenceAlertResponse(
                alert.getId(),
                alert.getGeofence().getId(),
                alert.getAlertType().name(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getSeverity().name(),
                alert.getStatus().name(),
                alert.getTriggeredByEventId(),
                alert.getAlertData(),
                alert.getNotificationChannels(),
                alert.getAutoActionsTriggered(),
                alert.getAssignedTo(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgedBy(),
                alert.getResolvedAt(),
                alert.getResolvedBy(),
                alert.getResolutionNotes(),
                alert.getEscalatedAt(),
                alert.getEscalatedTo(),
                alert.getEscalationReason()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Acknowledge an alert
     */
    @PostMapping("/alerts/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge alert", description = "Acknowledge a geofence alert")
    public ResponseEntity<Void> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestParam String acknowledgedBy) {
        
        geofencingService.acknowledgeAlert(alertId, acknowledgedBy);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Resolve an alert
     */
    @PostMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "Resolve alert", description = "Resolve a geofence alert")
    public ResponseEntity<Void> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam String resolvedBy,
            @RequestParam(required = false) String resolutionNotes) {
        
        geofencingService.resolveAlert(alertId, resolvedBy, resolutionNotes);
        return ResponseEntity.ok().build();
    }
    
    // Request/Response DTOs
    
    public record GeofenceRequest(
        String name,
        String description,
        List<CoordinateRequest> boundaryCoordinates,
        GeofenceType geofenceType,
        GeofencePriority priority,
        Boolean isActive,
        Double bufferDistanceMeters,
        Integer checkIntervalSeconds,
        Integer alertThreshold,
        Integer cooldownPeriodSeconds,
        String notificationChannels,
        String autoActions,
        String metadata,
        String createdBy
    ) {}
    
    public record CoordinateRequest(double longitude, double latitude) {}
    
    public record GeofenceResponse(
        Long id,
        String name,
        String description,
        String geofenceType,
        String priority,
        Boolean isActive,
        Double bufferDistanceMeters,
        Integer checkIntervalSeconds,
        Integer alertThreshold,
        Integer cooldownPeriodSeconds,
        String notificationChannels,
        String autoActions,
        String metadata,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastCheckedAt,
        LocalDateTime lastAlertAt
    ) {}
    
    public record GeofenceCheckRequest(
        double longitude,
        double latitude,
        String entityType,
        Long entityId,
        String entityName
    ) {}
    
    public record GeofenceCheckResponse(
        boolean isInGeofence,
        Long geofenceId,
        String geofenceName,
        Long eventId,
        String eventType
    ) {}
    
    public record GeofenceEventResponse(
        Long id,
        Long geofenceId,
        String eventType,
        double longitude,
        double latitude,
        String entityType,
        Long entityId,
        String entityName,
        String eventData,
        String severity,
        Double confidenceScore,
        LocalDateTime occurredAt,
        LocalDateTime detectedAt,
        LocalDateTime processedAt,
        Boolean isProcessed,
        String processingNotes
    ) {}
    
    public record GeofenceAlertResponse(
        Long id,
        Long geofenceId,
        String alertType,
        String title,
        String message,
        String severity,
        String status,
        Long triggeredByEventId,
        String alertData,
        String notificationChannels,
        String autoActionsTriggered,
        String assignedTo,
        LocalDateTime createdAt,
        LocalDateTime acknowledgedAt,
        String acknowledgedBy,
        LocalDateTime resolvedAt,
        String resolvedBy,
        String resolutionNotes,
        LocalDateTime escalatedAt,
        String escalatedTo,
        String escalationReason
    ) {}
}



