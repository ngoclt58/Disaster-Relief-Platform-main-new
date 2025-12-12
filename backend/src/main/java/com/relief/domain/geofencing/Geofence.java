package com.relief.domain.geofencing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a geofence boundary for automated monitoring and alerts
 */
@Entity
@Table(name = "geofences", indexes = {
    @Index(name = "idx_geofence_geom", columnList = "boundary"),
    @Index(name = "idx_geofence_name", columnList = "name"),
    @Index(name = "idx_geofence_active", columnList = "is_active"),
    @Index(name = "idx_geofence_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Geofence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "boundary", nullable = false, columnDefinition = "geometry(Geometry, 4326)")
    private Geometry boundary;
    
    @Column(name = "geofence_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private GeofenceType geofenceType;
    
    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private GeofencePriority priority;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "buffer_distance_meters")
    private Double bufferDistanceMeters;
    
    @Column(name = "check_interval_seconds")
    private Integer checkIntervalSeconds;
    
    @Column(name = "alert_threshold")
    private Integer alertThreshold; // Number of events before alert
    
    @Column(name = "cooldown_period_seconds")
    private Integer cooldownPeriodSeconds;
    
    @Column(name = "notification_channels", columnDefinition = "jsonb")
    private String notificationChannels; // JSON array of notification channels
    
    @Column(name = "auto_actions", columnDefinition = "jsonb")
    private String autoActions; // JSON array of automated actions
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional configuration data
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;
    
    @Column(name = "last_alert_at")
    private LocalDateTime lastAlertAt;
    
    @OneToMany(mappedBy = "geofence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GeofenceEvent> events;
    
    @OneToMany(mappedBy = "geofence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GeofenceAlert> alerts;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (checkIntervalSeconds == null) {
            checkIntervalSeconds = 300; // Default 5 minutes
        }
        if (alertThreshold == null) {
            alertThreshold = 1;
        }
        if (cooldownPeriodSeconds == null) {
            cooldownPeriodSeconds = 3600; // Default 1 hour
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Explicit getters and setters for Lombok compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Geometry getBoundary() { return boundary; }
    public void setBoundary(Geometry boundary) { this.boundary = boundary; }

    public GeofenceType getGeofenceType() { return geofenceType; }
    public void setGeofenceType(GeofenceType geofenceType) { this.geofenceType = geofenceType; }

    public GeofencePriority getPriority() { return priority; }
    public void setPriority(GeofencePriority priority) { this.priority = priority; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Double getBufferDistanceMeters() { return bufferDistanceMeters; }
    public void setBufferDistanceMeters(Double bufferDistanceMeters) { this.bufferDistanceMeters = bufferDistanceMeters; }

    public Integer getCheckIntervalSeconds() { return checkIntervalSeconds; }
    public void setCheckIntervalSeconds(Integer checkIntervalSeconds) { this.checkIntervalSeconds = checkIntervalSeconds; }

    public Integer getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(Integer alertThreshold) { this.alertThreshold = alertThreshold; }

    public Integer getCooldownPeriodSeconds() { return cooldownPeriodSeconds; }
    public void setCooldownPeriodSeconds(Integer cooldownPeriodSeconds) { this.cooldownPeriodSeconds = cooldownPeriodSeconds; }

    public String getNotificationChannels() { return notificationChannels; }
    public void setNotificationChannels(String notificationChannels) { this.notificationChannels = notificationChannels; }

    public String getAutoActions() { return autoActions; }
    public void setAutoActions(String autoActions) { this.autoActions = autoActions; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(LocalDateTime lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }

    public LocalDateTime getLastAlertAt() { return lastAlertAt; }
    public void setLastAlertAt(LocalDateTime lastAlertAt) { this.lastAlertAt = lastAlertAt; }

    public List<GeofenceEvent> getEvents() { return events; }
    public void setEvents(List<GeofenceEvent> events) { this.events = events; }

    public List<GeofenceAlert> getAlerts() { return alerts; }
    public void setAlerts(List<GeofenceAlert> alerts) { this.alerts = alerts; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private Geometry boundary;
        private GeofenceType geofenceType;
        private GeofencePriority priority;
        private Boolean isActive;
        private Double bufferDistanceMeters;
        private Integer checkIntervalSeconds;
        private Integer alertThreshold;
        private Integer cooldownPeriodSeconds;
        private String notificationChannels;
        private String autoActions;
        private String metadata;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastCheckedAt;
        private LocalDateTime lastAlertAt;
        private List<GeofenceEvent> events;
        private List<GeofenceAlert> alerts;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder boundary(Geometry boundary) { this.boundary = boundary; return this; }
        public Builder geofenceType(GeofenceType geofenceType) { this.geofenceType = geofenceType; return this; }
        public Builder priority(GeofencePriority priority) { this.priority = priority; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder bufferDistanceMeters(Double bufferDistanceMeters) { this.bufferDistanceMeters = bufferDistanceMeters; return this; }
        public Builder checkIntervalSeconds(Integer checkIntervalSeconds) { this.checkIntervalSeconds = checkIntervalSeconds; return this; }
        public Builder alertThreshold(Integer alertThreshold) { this.alertThreshold = alertThreshold; return this; }
        public Builder cooldownPeriodSeconds(Integer cooldownPeriodSeconds) { this.cooldownPeriodSeconds = cooldownPeriodSeconds; return this; }
        public Builder notificationChannels(String notificationChannels) { this.notificationChannels = notificationChannels; return this; }
        public Builder autoActions(String autoActions) { this.autoActions = autoActions; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder lastCheckedAt(LocalDateTime lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; return this; }
        public Builder lastAlertAt(LocalDateTime lastAlertAt) { this.lastAlertAt = lastAlertAt; return this; }
        public Builder events(List<GeofenceEvent> events) { this.events = events; return this; }
        public Builder alerts(List<GeofenceAlert> alerts) { this.alerts = alerts; return this; }

        public Geofence build() {
            Geofence geofence = new Geofence();
            geofence.setId(id);
            geofence.setName(name);
            geofence.setDescription(description);
            geofence.setBoundary(boundary);
            geofence.setGeofenceType(geofenceType);
            geofence.setPriority(priority);
            geofence.setIsActive(isActive);
            geofence.setBufferDistanceMeters(bufferDistanceMeters);
            geofence.setCheckIntervalSeconds(checkIntervalSeconds);
            geofence.setAlertThreshold(alertThreshold);
            geofence.setCooldownPeriodSeconds(cooldownPeriodSeconds);
            geofence.setNotificationChannels(notificationChannels);
            geofence.setAutoActions(autoActions);
            geofence.setMetadata(metadata);
            geofence.setCreatedBy(createdBy);
            geofence.setCreatedAt(createdAt);
            geofence.setUpdatedAt(updatedAt);
            geofence.setLastCheckedAt(lastCheckedAt);
            geofence.setLastAlertAt(lastAlertAt);
            geofence.setEvents(events);
            geofence.setAlerts(alerts);
            return geofence;
        }
    }
}



