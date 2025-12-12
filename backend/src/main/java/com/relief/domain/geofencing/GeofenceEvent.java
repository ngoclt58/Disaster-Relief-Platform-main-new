package com.relief.domain.geofencing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * Represents an event that occurred within a geofence
 */
@Entity
@Table(name = "geofence_events", indexes = {
    @Index(name = "idx_geofence_event_geom", columnList = "location"),
    @Index(name = "idx_geofence_event_geofence", columnList = "geofence_id"),
    @Index(name = "idx_geofence_event_type", columnList = "event_type"),
    @Index(name = "idx_geofence_event_occurred_at", columnList = "occurred_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "geofence_id", nullable = false)
    private Geofence geofence;
    
    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private GeofenceEventType eventType;
    
    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType; // Type of entity that triggered the event
    
    @Column(name = "entity_id")
    private Long entityId; // ID of the entity that triggered the event
    
    @Column(name = "entity_name")
    private String entityName; // Name or identifier of the entity
    
    @Column(name = "event_data", columnDefinition = "jsonb")
    private String eventData; // Additional event data as JSON
    
    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private GeofenceEventSeverity severity;
    
    @Column(name = "confidence_score")
    private Double confidenceScore; // Confidence in the event detection (0-1)
    
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
    
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed;
    
    @Column(name = "processing_notes")
    private String processingNotes;
    
    @PrePersist
    protected void onCreate() {
        if (isProcessed == null) {
            isProcessed = false;
        }
        if (detectedAt == null) {
            detectedAt = LocalDateTime.now();
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }

    // Explicit getters and setters for Lombok compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Geofence getGeofence() { return geofence; }
    public void setGeofence(Geofence geofence) { this.geofence = geofence; }

    public GeofenceEventType getEventType() { return eventType; }
    public void setEventType(GeofenceEventType eventType) { this.eventType = eventType; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }

    public GeofenceEventSeverity getSeverity() { return severity; }
    public void setSeverity(GeofenceEventSeverity severity) { this.severity = severity; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public Boolean getIsProcessed() { return isProcessed; }
    public void setIsProcessed(Boolean isProcessed) { this.isProcessed = isProcessed; }

    public String getProcessingNotes() { return processingNotes; }
    public void setProcessingNotes(String processingNotes) { this.processingNotes = processingNotes; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Geofence geofence;
        private GeofenceEventType eventType;
        private Point location;
        private String entityType;
        private Long entityId;
        private String entityName;
        private String eventData;
        private GeofenceEventSeverity severity;
        private Double confidenceScore;
        private LocalDateTime occurredAt;
        private LocalDateTime detectedAt;
        private LocalDateTime processedAt;
        private Boolean isProcessed;
        private String processingNotes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder geofence(Geofence geofence) { this.geofence = geofence; return this; }
        public Builder eventType(GeofenceEventType eventType) { this.eventType = eventType; return this; }
        public Builder location(Point location) { this.location = location; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder entityId(Long entityId) { this.entityId = entityId; return this; }
        public Builder entityName(String entityName) { this.entityName = entityName; return this; }
        public Builder eventData(String eventData) { this.eventData = eventData; return this; }
        public Builder severity(GeofenceEventSeverity severity) { this.severity = severity; return this; }
        public Builder confidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Builder occurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; return this; }
        public Builder detectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; return this; }
        public Builder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }
        public Builder isProcessed(Boolean isProcessed) { this.isProcessed = isProcessed; return this; }
        public Builder processingNotes(String processingNotes) { this.processingNotes = processingNotes; return this; }

        public GeofenceEvent build() {
            GeofenceEvent event = new GeofenceEvent();
            event.setId(id);
            event.setGeofence(geofence);
            event.setEventType(eventType);
            event.setLocation(location);
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityName(entityName);
            event.setEventData(eventData);
            event.setSeverity(severity);
            event.setConfidenceScore(confidenceScore);
            event.setOccurredAt(occurredAt);
            event.setDetectedAt(detectedAt);
            event.setProcessedAt(processedAt);
            event.setIsProcessed(isProcessed);
            event.setProcessingNotes(processingNotes);
            return event;
        }
    }
}



