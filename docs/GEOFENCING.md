# Geofencing & Automated Monitoring

## Overview

The Geofencing feature provides automated alerts and actions based on geographic boundaries, enabling intelligent location-based monitoring and response capabilities for disaster relief operations. This system automatically detects events within defined geographic areas and triggers appropriate alerts and actions.

## Features

### 🛡️ **Geographic Boundary Management**
- **Multi-Type Geofences**: Support for 15+ different geofence types including disaster zones, evacuation areas, resource depots, and emergency shelters
- **Flexible Boundaries**: Create complex polygonal boundaries with precise coordinate control
- **Priority-Based Monitoring**: Critical, High, Medium, Low, and Info priority levels for different monitoring requirements
- **Buffer Zones**: Configurable buffer distances around geofence boundaries
- **Active/Inactive States**: Toggle geofence monitoring on and off as needed

### 📍 **Real-Time Event Detection**
- **Entry/Exit Events**: Automatic detection when entities enter or exit geofenced areas
- **Dwell Monitoring**: Track entities that remain within geofences for extended periods
- **Proximity Alerts**: Monitor entities approaching geofence boundaries
- **Violation Detection**: Identify unauthorized access or rule violations
- **Emergency Detection**: Immediate alerts for emergency situations within geofences

### 🚨 **Intelligent Alert System**
- **Multi-Channel Notifications**: Email, SMS, push notifications, and custom channels
- **Severity-Based Escalation**: Critical, High, Medium, Low, and Info severity levels
- **Threshold Management**: Configurable alert thresholds and cooldown periods
- **Automated Actions**: Trigger automated responses based on geofence events
- **Alert Lifecycle Management**: Acknowledge, resolve, and escalate alerts with full audit trail

### ⚙️ **Advanced Configuration**
- **Custom Monitoring Intervals**: Configurable check intervals from seconds to hours
- **Smart Cooldown Periods**: Prevent alert spam with intelligent cooldown management
- **Entity Type Filtering**: Monitor specific types of entities (vehicles, people, equipment)
- **Confidence Scoring**: AI-powered confidence scoring for event detection accuracy
- **Metadata Support**: Store custom configuration and operational data

## Technical Architecture

### Backend Components

#### **Domain Models**
```java
// Geofence boundary definition
@Entity
public class Geofence {
    private String name;                    // Geofence name
    private String description;             // Description
    private Geometry boundary;              // Geographic boundary
    private GeofenceType geofenceType;     // Type of geofence
    private GeofencePriority priority;     // Priority level
    private Boolean isActive;              // Active status
    private Double bufferDistanceMeters;   // Buffer distance
    private Integer checkIntervalSeconds;  // Check interval
    private Integer alertThreshold;        // Alert threshold
    private Integer cooldownPeriodSeconds; // Cooldown period
    private String notificationChannels;   // Notification channels
    private String autoActions;            // Automated actions
    private String metadata;               // Additional data
}

// Geofence event tracking
@Entity
public class GeofenceEvent {
    private Geofence geofence;             // Associated geofence
    private GeofenceEventType eventType;   // Type of event
    private Point location;                // Event location
    private String entityType;             // Entity type
    private Long entityId;                 // Entity ID
    private String entityName;             // Entity name
    private GeofenceEventSeverity severity; // Event severity
    private Double confidenceScore;        // Detection confidence
    private LocalDateTime occurredAt;      // When event occurred
    private Boolean isProcessed;           // Processing status
}

// Alert management
@Entity
public class GeofenceAlert {
    private Geofence geofence;             // Associated geofence
    private GeofenceAlertType alertType;   // Type of alert
    private String title;                  // Alert title
    private String message;                // Alert message
    private GeofenceAlertSeverity severity; // Alert severity
    private GeofenceAlertStatus status;    // Alert status
    private Long triggeredByEventId;       // Triggering event
    private String notificationChannels;   // Notification channels
    private String autoActionsTriggered;   // Actions triggered
    private String assignedTo;             // Assigned user/team
    private LocalDateTime createdAt;       // Creation time
    private LocalDateTime resolvedAt;      // Resolution time
}
```

#### **Services**
- **GeofencingService**: Core geofencing operations and monitoring
- **GeofenceEventService**: Event detection and processing
- **GeofenceAlertService**: Alert generation and management
- **GeofenceMonitoringService**: Real-time monitoring and automation

#### **REST API Endpoints**
```
POST /api/geofencing/geofences                    # Create geofence
PUT  /api/geofencing/geofences/{id}              # Update geofence
DELETE /api/geofencing/geofences/{id}            # Delete geofence
GET  /api/geofencing/geofences                   # Get all geofences
GET  /api/geofencing/geofences/active            # Get active geofences
GET  /api/geofencing/geofences/type/{type}       # Get geofences by type
GET  /api/geofencing/geofences/bounds            # Get geofences within bounds

POST /api/geofencing/check                        # Check point in geofences
POST /api/geofencing/process-events              # Process geofence events

GET  /api/geofencing/geofences/{id}/events       # Get geofence events
GET  /api/geofencing/geofences/{id}/alerts       # Get geofence alerts
GET  /api/geofencing/alerts/active               # Get active alerts
POST /api/geofencing/alerts/{id}/acknowledge     # Acknowledge alert
POST /api/geofencing/alerts/{id}/resolve         # Resolve alert
```

### Frontend Components

#### **GeofenceMap Component**
- Interactive MapLibre GL JS integration
- Real-time geofence visualization
- Event and alert overlay display
- Click-to-analyze functionality
- Multi-layer display controls

#### **GeofenceManagement Component**
- Geofence creation and editing interface
- Boundary coordinate management
- Configuration parameter controls
- Preview and validation
- Bulk operations support

#### **GeofencingDashboard Component**
- Comprehensive geofencing interface
- Real-time monitoring dashboard
- Event and alert management
- Statistics and analytics
- Multi-tab interface for different functions

### Database Schema

#### **Geofences Table**
```sql
CREATE TABLE geofences (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    boundary GEOMETRY(Geometry, 4326) NOT NULL,
    geofence_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    buffer_distance_meters DOUBLE PRECISION,
    check_interval_seconds INTEGER NOT NULL DEFAULT 300,
    alert_threshold INTEGER NOT NULL DEFAULT 1,
    cooldown_period_seconds INTEGER NOT NULL DEFAULT 3600,
    notification_channels JSONB,
    auto_actions JSONB,
    metadata JSONB,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### **Geofence Events Table**
```sql
CREATE TABLE geofence_events (
    id BIGSERIAL PRIMARY KEY,
    geofence_id BIGINT NOT NULL REFERENCES geofences(id),
    event_type VARCHAR(50) NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    entity_name VARCHAR(200),
    event_data JSONB,
    severity VARCHAR(20) NOT NULL,
    confidence_score DOUBLE PRECISION,
    occurred_at TIMESTAMP NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    is_processed BOOLEAN NOT NULL DEFAULT false
);
```

#### **Geofence Alerts Table**
```sql
CREATE TABLE geofence_alerts (
    id BIGSERIAL PRIMARY KEY,
    geofence_id BIGINT NOT NULL REFERENCES geofences(id),
    alert_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    triggered_by_event_id BIGINT REFERENCES geofence_events(id),
    alert_data JSONB,
    notification_channels JSONB,
    auto_actions_triggered JSONB,
    assigned_to VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    resolved_at TIMESTAMP
);
```

## Usage Examples

### **1. Creating Geofences**
```typescript
// Create disaster zone geofence
const disasterZone = await GeofencingService.createGeofence({
  name: 'Disaster Zone Alpha',
  description: 'Primary disaster affected area',
  boundaryCoordinates: [
    { longitude: -74.1, latitude: 40.6 },
    { longitude: -73.9, latitude: 40.6 },
    { longitude: -73.9, latitude: 40.8 },
    { longitude: -74.1, latitude: 40.8 },
    { longitude: -74.1, latitude: 40.6 }
  ],
  geofenceType: 'DISASTER_ZONE',
  priority: 'CRITICAL',
  isActive: true,
  bufferDistanceMeters: 500,
  checkIntervalSeconds: 60,
  alertThreshold: 1,
  cooldownPeriodSeconds: 1800,
  notificationChannels: '["email", "sms", "push"]',
  autoActions: '["notify_emergency_team", "update_status_board"]',
  createdBy: 'admin_user'
});

// Create evacuation zone geofence
const evacuationZone = await GeofencingService.createGeofence({
  name: 'Evacuation Zone Beta',
  description: 'Mandatory evacuation area',
  boundaryCoordinates: [
    { longitude: -74.0, latitude: 40.5 },
    { longitude: -73.8, latitude: 40.5 },
    { longitude: -73.8, latitude: 40.7 },
    { longitude: -74.0, latitude: 40.7 },
    { longitude: -74.0, latitude: 40.5 }
  ],
  geofenceType: 'EVACUATION_ZONE',
  priority: 'HIGH',
  isActive: true,
  checkIntervalSeconds: 120,
  alertThreshold: 2,
  cooldownPeriodSeconds: 3600,
  notificationChannels: '["email", "push"]',
  autoActions: '["notify_evacuation_team", "update_evacuation_status"]',
  createdBy: 'admin_user'
});
```

### **2. Monitoring and Event Detection**
```typescript
// Check if a point is within any geofence
const checkResult = await GeofencingService.checkPointInGeofences({
  longitude: -74.0,
  latitude: 40.7,
  entityType: 'PERSON',
  entityId: 123,
  entityName: 'John Doe'
});

if (checkResult.isInGeofence) {
  console.log(`Entity is in geofence: ${checkResult.geofenceName}`);
  console.log(`Event type: ${checkResult.eventType}`);
}

// Process geofence events (typically called by scheduled job)
await GeofencingService.processGeofenceEvents();
```

### **3. Alert Management**
```typescript
// Get active alerts
const activeAlerts = await GeofencingService.getActiveAlerts();

// Acknowledge an alert
await GeofencingService.acknowledgeAlert(alertId, 'current_user');

// Resolve an alert
await GeofencingService.resolveAlert(alertId, 'current_user', 'Issue resolved');

// Get alerts for specific geofence
const geofenceAlerts = await GeofencingService.getGeofenceAlerts(geofenceId);
```

### **4. Event Analysis**
```typescript
// Get events for a geofence
const events = await GeofencingService.getGeofenceEvents(geofenceId);

// Filter events by type
const entryEvents = events.filter(event => event.eventType === 'ENTRY');
const violationEvents = events.filter(event => event.eventType === 'VIOLATION');

// Analyze event patterns
const criticalEvents = events.filter(event => event.severity === 'CRITICAL');
const unprocessedEvents = events.filter(event => !event.isProcessed);
```

## Configuration

### **Backend Configuration**
```yaml
# application.yml
geofencing:
  monitoring:
    default-check-interval: 300
    max-check-interval: 3600
    min-check-interval: 60
    default-alert-threshold: 1
    default-cooldown-period: 3600
    max-cooldown-period: 86400
    min-cooldown-period: 60
  events:
    max-events-per-geofence: 10000
    event-retention-days: 30
    confidence-threshold: 0.7
    auto-process-events: true
  alerts:
    max-alerts-per-geofence: 1000
    alert-retention-days: 90
    escalation-timeout-hours: 24
    auto-escalate-critical: true
  notifications:
    channels: ["email", "sms", "push", "webhook"]
    rate-limit-per-minute: 100
    batch-notifications: true
    notification-timeout-seconds: 30
```

### **Frontend Configuration**
```typescript
// Geofencing visualization options
const geofencingOptions = {
  showGeofences: true,
  showEvents: true,
  showAlerts: true,
  geofenceOpacity: 0.3,
  eventRadius: 8,
  alertIconSize: 1.5,
  autoRefresh: true,
  refreshInterval: 30000, // 30 seconds
  maxEventsDisplay: 1000,
  maxAlertsDisplay: 500
};
```

## Geofence Types

### **Disaster Management**
- **DISASTER_ZONE**: Areas affected by natural disasters
- **EVACUATION_ZONE**: Mandatory evacuation areas
- **RESTRICTED_ZONE**: Restricted access areas
- **QUARANTINE_ZONE**: Quarantine and isolation areas
- **RECOVERY_ZONE**: Areas in recovery phase

### **Resource Management**
- **RESOURCE_DEPOT**: Resource storage and distribution centers
- **SUPPLY_ROUTE**: Critical supply routes
- **INFRASTRUCTURE**: Critical infrastructure sites
- **COMMUNICATION_HUB**: Communication and coordination centers

### **Emergency Services**
- **EMERGENCY_SHELTER**: Emergency shelter locations
- **MEDICAL_FACILITY**: Medical facilities and hospitals
- **RESPONSE_BASE**: Emergency response base camps
- **CHECKPOINT**: Security and access checkpoints

### **Population Management**
- **POPULATION_DENSITY**: High population density areas
- **VULNERABLE_AREA**: Areas with vulnerable populations

## Event Types

### **Movement Events**
- **ENTRY**: Entity entered the geofence
- **EXIT**: Entity exited the geofence
- **DWELL**: Entity dwelling within the geofence
- **PROXIMITY**: Entity in proximity to the geofence

### **Security Events**
- **VIOLATION**: Violation of geofence rules
- **UNAUTHORIZED_ACCESS**: Unauthorized access attempt

### **Operational Events**
- **EMERGENCY**: Emergency situation within geofence
- **RESOURCE_DEPLETION**: Resource depletion in geofence
- **CAPACITY_EXCEEDED**: Capacity exceeded in geofence
- **MAINTENANCE_REQUIRED**: Maintenance required in geofence
- **STATUS_CHANGE**: Status change within geofence

## Alert Types

### **Boundary Alerts**
- **BOUNDARY_VIOLATION**: Violation of geofence boundary rules
- **UNAUTHORIZED_ACCESS**: Unauthorized access attempt

### **Capacity Alerts**
- **CAPACITY_EXCEEDED**: Capacity limits exceeded
- **RESOURCE_DEPLETION**: Resources depleted below threshold

### **Emergency Alerts**
- **EMERGENCY_DETECTED**: Emergency situation detected
- **MAINTENANCE_REQUIRED**: Maintenance required

### **Threshold Alerts**
- **THRESHOLD_EXCEEDED**: Monitoring threshold exceeded
- **TIME_BASED_ALERT**: Time-based alert triggered
- **STATUS_CHANGE**: Status change detected

## Performance Considerations

### **Spatial Query Optimization**
- **GIST Indexing**: Spatial indexes on geometry columns for fast queries
- **Boundary Caching**: Cache geofence boundaries for frequent lookups
- **Event Batching**: Batch event processing for efficiency
- **Alert Throttling**: Prevent alert spam with intelligent throttling

### **Real-Time Processing**
- **Asynchronous Processing**: Non-blocking event processing
- **Queue Management**: Message queues for event processing
- **Load Balancing**: Distribute processing across multiple instances
- **Caching Strategies**: Redis caching for frequently accessed data

### **Database Performance**
- **Partitioning**: Partition large tables by date or geofence
- **Index Optimization**: Optimize indexes for common query patterns
- **Connection Pooling**: Efficient database connection management
- **Query Optimization**: Optimize spatial queries for performance

## Security Considerations

### **Access Control**
- **Role-Based Access**: Different access levels for different user roles
- **Geofence Ownership**: Control access based on geofence ownership
- **API Authentication**: Secure API access with JWT tokens
- **Data Encryption**: Encrypt sensitive geofence metadata

### **Alert Security**
- **Alert Validation**: Validate alert data before processing
- **Rate Limiting**: Prevent alert spam and abuse
- **Audit Logging**: Track all geofence and alert operations
- **Data Sanitization**: Sanitize user input and metadata

## Monitoring & Analytics

### **Key Metrics**
- **Geofence Coverage**: Percentage of area covered by geofences
- **Event Detection Rate**: Events detected per hour/day
- **Alert Response Time**: Time from event to alert generation
- **False Positive Rate**: Percentage of false positive events
- **Processing Performance**: Event processing times and throughput

### **Alerting**
- **System Health**: Alert on geofencing system issues
- **Performance Degradation**: Alert on slow processing times
- **High Event Volume**: Alert on unusual event volumes
- **Alert Backlog**: Alert on unprocessed alerts

## Integration Examples

### **External Data Sources**
```typescript
// Integration with GPS tracking
const gpsData = await GPSService.getCurrentPositions();
for (const position of gpsData) {
  const checkResult = await GeofencingService.checkPointInGeofences({
    longitude: position.longitude,
    latitude: position.latitude,
    entityType: 'VEHICLE',
    entityId: position.vehicleId,
    entityName: position.vehicleName
  });
  
  if (checkResult.isInGeofence) {
    // Handle geofence entry/exit
    await VehicleService.updateLocationStatus(position.vehicleId, checkResult);
  }
}

// Integration with emergency systems
const emergencyEvents = await GeofencingService.getGeofenceEvents(disasterZoneId);
const criticalEvents = emergencyEvents.filter(e => e.severity === 'CRITICAL');
for (const event of criticalEvents) {
  await EmergencyService.triggerEmergencyResponse(event);
}
```

### **Real-Time Updates**
```typescript
// WebSocket integration for real-time updates
const socket = new WebSocket('ws://localhost:8080/ws/geofencing');

socket.onmessage = (event) => {
  const update = JSON.parse(event.data);
  if (update.type === 'GEOFENCE_EVENT') {
    // Update event display
    updateEventDisplay(update.data);
  } else if (update.type === 'GEOFENCE_ALERT') {
    // Update alert display
    updateAlertDisplay(update.data);
  }
};
```

## Future Enhancements

### **Planned Features**
- **Machine Learning Integration**: AI-powered event detection and prediction
- **Mobile App Integration**: Real-time geofencing on mobile devices
- **IoT Sensor Integration**: Integration with IoT sensors for enhanced monitoring
- **Advanced Analytics**: Statistical analysis and trend detection
- **Multi-Tenant Support**: Support for multiple organizations

### **Integration Opportunities**
- **Weather Data**: Weather pattern correlation with geofence events
- **Traffic Data**: Traffic flow analysis within geofences
- **Social Media**: Social media sentiment analysis for geofence areas
- **Crowdsourcing**: Citizen-reported data integration
- **Satellite Imagery**: Integration with satellite data for boundary validation

## Troubleshooting

### **Common Issues**

#### **Event Detection Problems**
- Check geofence boundary coordinates and format
- Verify entity location data accuracy
- Review confidence score thresholds
- Check geofence active status

#### **Alert Generation Issues**
- Verify alert threshold settings
- Check cooldown period configuration
- Review notification channel settings
- Validate automated action configurations

#### **Performance Issues**
- Monitor database query performance
- Check spatial index usage
- Review event processing queue
- Optimize geofence check intervals

### **Debug Tools**
- **Geofence Inspector**: Browser tool for geofence validation
- **Event Logger**: Detailed event processing logs
- **Alert Tracer**: Alert generation and delivery tracking
- **Performance Monitor**: Real-time performance metrics

## Support

For technical support or feature requests related to Geofencing:

1. **Documentation**: Check this guide and API documentation
2. **Logs**: Review application and processing logs
3. **Database**: Check geofence, event, and alert tables
4. **Community**: Post questions in the development team chat
5. **Issues**: Create GitHub issues for bugs or feature requests

---

*This feature significantly enhances the disaster relief platform's monitoring capabilities, providing intelligent geofencing and automated alerting for comprehensive geographic boundary management and real-time event detection.*



