package com.relief.service.geofencing;

import com.relief.domain.geofencing.*;
import com.relief.repository.geofencing.GeofenceRepository;
import com.relief.repository.geofencing.GeofenceEventRepository;
import com.relief.repository.geofencing.GeofenceAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for geofencing operations and monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeofencingService {
    
    private static final Logger log = LoggerFactory.getLogger(GeofencingService.class);
    
    private final GeofenceRepository geofenceRepository;
    private final GeofenceEventRepository eventRepository;
    private final GeofenceAlertRepository alertRepository;
    private final GeometryFactory geometryFactory;
    
    /**
     * Create a new geofence
     */
    @Transactional
    public Geofence createGeofence(GeofenceRequest request) {
        log.info("Creating geofence: {}", request.name());
        
        // Create boundary geometry
        Geometry boundary = createBoundaryGeometry(request.boundaryCoordinates());
        
        Geofence geofence = Geofence.builder()
            .name(request.name())
            .description(request.description())
            .boundary(boundary)
            .geofenceType(request.geofenceType())
            .priority(request.priority())
            .isActive(request.isActive())
            .bufferDistanceMeters(request.bufferDistanceMeters())
            .checkIntervalSeconds(request.checkIntervalSeconds())
            .alertThreshold(request.alertThreshold())
            .cooldownPeriodSeconds(request.cooldownPeriodSeconds())
            .notificationChannels(request.notificationChannels())
            .autoActions(request.autoActions())
            .metadata(request.metadata())
            .createdBy(request.createdBy())
            .build();
            
        return geofenceRepository.save(geofence);
    }
    
    /**
     * Update an existing geofence
     */
    @Transactional
    public Geofence updateGeofence(Long geofenceId, GeofenceRequest request) {
        log.info("Updating geofence: {}", geofenceId);
        
        Geofence geofence = geofenceRepository.findById(geofenceId)
            .orElseThrow(() -> new IllegalArgumentException("Geofence not found"));
        
        // Update fields
        geofence.setName(request.name());
        geofence.setDescription(request.description());
        geofence.setGeofenceType(request.geofenceType());
        geofence.setPriority(request.priority());
        geofence.setIsActive(request.isActive());
        geofence.setBufferDistanceMeters(request.bufferDistanceMeters());
        geofence.setCheckIntervalSeconds(request.checkIntervalSeconds());
        geofence.setAlertThreshold(request.alertThreshold());
        geofence.setCooldownPeriodSeconds(request.cooldownPeriodSeconds());
        geofence.setNotificationChannels(request.notificationChannels());
        geofence.setAutoActions(request.autoActions());
        geofence.setMetadata(request.metadata());
        
        // Update boundary if provided
        if (request.boundaryCoordinates() != null) {
            Geometry boundary = createBoundaryGeometry(request.boundaryCoordinates());
            geofence.setBoundary(boundary);
        }
        
        return geofenceRepository.save(geofence);
    }
    
    /**
     * Delete a geofence
     */
    @Transactional
    public void deleteGeofence(Long geofenceId) {
        log.info("Deleting geofence: {}", geofenceId);
        
        Geofence geofence = geofenceRepository.findById(geofenceId)
            .orElseThrow(() -> new IllegalArgumentException("Geofence not found"));
        
        geofenceRepository.delete(geofence);
    }
    
    /**
     * Get geofence by ID
     */
    public Optional<Geofence> getGeofence(Long geofenceId) {
        return geofenceRepository.findById(geofenceId);
    }
    
    /**
     * Get all geofences
     */
    public List<Geofence> getAllGeofences() {
        return geofenceRepository.findAll();
    }
    
    /**
     * Get active geofences
     */
    public List<Geofence> getActiveGeofences() {
        return geofenceRepository.findByIsActiveTrue();
    }
    
    /**
     * Get geofences by type
     */
    public List<Geofence> getGeofencesByType(GeofenceType geofenceType) {
        return geofenceRepository.findByGeofenceType(geofenceType);
    }
    
    /**
     * Get geofences by priority
     */
    public List<Geofence> getGeofencesByPriority(GeofencePriority priority) {
        return geofenceRepository.findByPriority(priority);
    }
    
    /**
     * Get geofences containing a point
     */
    public List<Geofence> getGeofencesContainingPoint(double longitude, double latitude) {
        return geofenceRepository.findContainingPoint(longitude, latitude);
    }
    
    /**
     * Get geofences within bounds
     */
    public List<Geofence> getGeofencesWithinBounds(double minLon, double minLat, 
                                                  double maxLon, double maxLat) {
        return geofenceRepository.findWithinBounds(minLon, minLat, maxLon, maxLat);
    }
    
    /**
     * Check if a point is within any geofence
     */
    public GeofenceCheckResult checkPointInGeofences(double longitude, double latitude, 
                                                    String entityType, Long entityId, 
                                                    String entityName) {
        List<Geofence> containingGeofences = geofenceRepository.findContainingPoint(longitude, latitude);
        
        if (containingGeofences.isEmpty()) {
            return new GeofenceCheckResult(false, null, null);
        }
        
        // Find the highest priority geofence
        Geofence highestPriorityGeofence = containingGeofences.stream()
            .min((g1, g2) -> g1.getPriority().compareTo(g2.getPriority()))
            .orElse(containingGeofences.get(0));
        
        // Create entry event
        GeofenceEvent event = createGeofenceEvent(
            highestPriorityGeofence, 
            GeofenceEventType.ENTRY,
            longitude, latitude,
            entityType, entityId, entityName
        );
        
        return new GeofenceCheckResult(true, highestPriorityGeofence, event);
    }
    
    /**
     * Process geofence events and generate alerts
     */
    @Transactional
    public void processGeofenceEvents() {
        log.info("Processing geofence events");
        
        // Get geofences that need checking
        LocalDateTime checkTime = LocalDateTime.now().minusMinutes(5); // Check every 5 minutes
        List<Geofence> geofencesToCheck = geofenceRepository.findNeedingCheck(checkTime);
        
        for (Geofence geofence : geofencesToCheck) {
            try {
                processGeofence(geofence);
                geofence.setLastCheckedAt(LocalDateTime.now());
                geofenceRepository.save(geofence);
            } catch (Exception e) {
                log.error("Error processing geofence {}: {}", geofence.getId(), e.getMessage());
            }
        }
    }
    
    /**
     * Process a specific geofence
     */
    private void processGeofence(Geofence geofence) {
        // Get recent events for this geofence
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<GeofenceEvent> recentEvents = eventRepository.findRecentByGeofenceId(geofence.getId(), since);
        
        // Check if alert threshold is reached
        if (recentEvents.size() >= geofence.getAlertThreshold()) {
            // Check cooldown period
            if (geofence.getLastAlertAt() == null || 
                geofence.getLastAlertAt().isBefore(LocalDateTime.now().minusSeconds(geofence.getCooldownPeriodSeconds()))) {
                
                createGeofenceAlert(geofence, recentEvents);
                geofence.setLastAlertAt(LocalDateTime.now());
                geofenceRepository.save(geofence);
            }
        }
    }
    
    /**
     * Create a geofence event
     */
    @Transactional
    public GeofenceEvent createGeofenceEvent(Geofence geofence, GeofenceEventType eventType,
                                           double longitude, double latitude,
                                           String entityType, Long entityId, String entityName) {
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        
        GeofenceEvent event = GeofenceEvent.builder()
            .geofence(geofence)
            .eventType(eventType)
            .location(location)
            .entityType(entityType)
            .entityId(entityId)
            .entityName(entityName)
            .severity(determineEventSeverity(geofence, eventType))
            .confidenceScore(calculateConfidenceScore(geofence, eventType))
            .occurredAt(LocalDateTime.now())
            .detectedAt(LocalDateTime.now())
            .isProcessed(false)
            .build();
            
        return eventRepository.save(event);
    }
    
    /**
     * Create a geofence alert
     */
    @Transactional
    public GeofenceAlert createGeofenceAlert(Geofence geofence, List<GeofenceEvent> events) {
        GeofenceAlertType alertType = determineAlertType(geofence, events);
        GeofenceAlertSeverity severity = determineAlertSeverity(geofence, events);
        
        GeofenceAlert alert = GeofenceAlert.builder()
            .geofence(geofence)
            .alertType(alertType)
            .title(generateAlertTitle(geofence, alertType))
            .message(generateAlertMessage(geofence, events, alertType))
            .severity(severity)
            .status(GeofenceAlertStatus.ACTIVE)
            .triggeredByEventId(events.get(0).getId())
            .alertData(generateAlertData(geofence, events))
            .notificationChannels(geofence.getNotificationChannels())
            .autoActionsTriggered(geofence.getAutoActions())
            .build();
            
        return alertRepository.save(alert);
    }
    
    /**
     * Get geofence events
     */
    public List<GeofenceEvent> getGeofenceEvents(Long geofenceId) {
        return eventRepository.findByGeofenceId(geofenceId);
    }
    
    /**
     * Get geofence alerts
     */
    public List<GeofenceAlert> getGeofenceAlerts(Long geofenceId) {
        return alertRepository.findByGeofenceId(geofenceId);
    }
    
    /**
     * Get active alerts
     */
    public List<GeofenceAlert> getActiveAlerts() {
        return alertRepository.findUnresolvedAlerts();
    }
    
    /**
     * Acknowledge an alert
     */
    @Transactional
    public void acknowledgeAlert(Long alertId, String acknowledgedBy) {
        GeofenceAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        
        alert.setStatus(GeofenceAlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(acknowledgedBy);
        
        alertRepository.save(alert);
    }
    
    /**
     * Resolve an alert
     */
    @Transactional
    public void resolveAlert(Long alertId, String resolvedBy, String resolutionNotes) {
        GeofenceAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        
        alert.setStatus(GeofenceAlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alert.setResolutionNotes(resolutionNotes);
        
        alertRepository.save(alert);
    }
    
    /**
     * Create boundary geometry from coordinates
     */
    private Geometry createBoundaryGeometry(List<Coordinate> coordinates) {
        if (coordinates.size() < 3) {
            throw new IllegalArgumentException("At least 3 coordinates required for boundary");
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
     * Determine event severity based on geofence and event type
     */
    private GeofenceEventSeverity determineEventSeverity(Geofence geofence, GeofenceEventType eventType) {
        // Base severity on geofence priority and event type
        if (geofence.getPriority() == GeofencePriority.CRITICAL) {
            return GeofenceEventSeverity.CRITICAL;
        } else if (geofence.getPriority() == GeofencePriority.HIGH) {
            return GeofenceEventSeverity.HIGH;
        } else if (eventType == GeofenceEventType.VIOLATION || eventType == GeofenceEventType.EMERGENCY) {
            return GeofenceEventSeverity.HIGH;
        } else if (eventType == GeofenceEventType.ENTRY || eventType == GeofenceEventType.EXIT) {
            return GeofenceEventSeverity.MEDIUM;
        } else {
            return GeofenceEventSeverity.LOW;
        }
    }
    
    /**
     * Calculate confidence score for event detection
     */
    private Double calculateConfidenceScore(Geofence geofence, GeofenceEventType eventType) {
        double baseConfidence = 0.8;
        
        // Adjust based on geofence type
        switch (geofence.getGeofenceType()) {
            case DISASTER_ZONE, EVACUATION_ZONE -> baseConfidence += 0.1;
            case RESTRICTED_ZONE, QUARANTINE_ZONE -> baseConfidence += 0.15;
            default -> baseConfidence += 0.05;
        }
        
        // Adjust based on event type
        switch (eventType) {
            case ENTRY, EXIT -> baseConfidence += 0.1;
            case VIOLATION, EMERGENCY -> baseConfidence += 0.05;
            default -> baseConfidence += 0.0;
        }
        
        return Math.min(1.0, baseConfidence);
    }
    
    /**
     * Determine alert type based on geofence and events
     */
    private GeofenceAlertType determineAlertType(Geofence geofence, List<GeofenceEvent> events) {
        // Check for specific event types
        boolean hasViolations = events.stream().anyMatch(e -> e.getEventType() == GeofenceEventType.VIOLATION);
        boolean hasEmergencies = events.stream().anyMatch(e -> e.getEventType() == GeofenceEventType.EMERGENCY);
        
        if (hasEmergencies) {
            return GeofenceAlertType.EMERGENCY_DETECTED;
        } else if (hasViolations) {
            return GeofenceAlertType.BOUNDARY_VIOLATION;
        } else if (events.size() >= geofence.getAlertThreshold()) {
            return GeofenceAlertType.THRESHOLD_EXCEEDED;
        } else {
            return GeofenceAlertType.CUSTOM;
        }
    }
    
    /**
     * Determine alert severity based on geofence and events
     */
    private GeofenceAlertSeverity determineAlertSeverity(Geofence geofence, List<GeofenceEvent> events) {
        // Check for critical events
        boolean hasCriticalEvents = events.stream()
            .anyMatch(e -> e.getSeverity() == GeofenceEventSeverity.CRITICAL);
        
        if (hasCriticalEvents || geofence.getPriority() == GeofencePriority.CRITICAL) {
            return GeofenceAlertSeverity.CRITICAL;
        } else if (geofence.getPriority() == GeofencePriority.HIGH) {
            return GeofenceAlertSeverity.HIGH;
        } else {
            return GeofenceAlertSeverity.MEDIUM;
        }
    }
    
    /**
     * Generate alert title
     */
    private String generateAlertTitle(Geofence geofence, GeofenceAlertType alertType) {
        return String.format("%s Alert - %s", alertType.name().replace("_", " "), geofence.getName());
    }
    
    /**
     * Generate alert message
     */
    private String generateAlertMessage(Geofence geofence, List<GeofenceEvent> events, GeofenceAlertType alertType) {
        return String.format("Geofence '%s' has triggered %d events of type %s. " +
                           "Please review and take appropriate action.",
                           geofence.getName(), events.size(), alertType.name());
    }
    
    /**
     * Generate alert data
     */
    private String generateAlertData(Geofence geofence, List<GeofenceEvent> events) {
        return String.format("{\"geofence_id\": %d, \"event_count\": %d, \"events\": [%s]}",
                           geofence.getId(), events.size(), 
                           events.stream().map(e -> String.valueOf(e.getId())).reduce((a, b) -> a + "," + b).orElse(""));
    }
    
    // Data classes
    public record GeofenceRequest(
        String name,
        String description,
        List<Coordinate> boundaryCoordinates,
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
    
    public record GeofenceCheckResult(
        boolean isInGeofence,
        Geofence geofence,
        GeofenceEvent event
    ) {}
}



