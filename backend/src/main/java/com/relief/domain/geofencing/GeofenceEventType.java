package com.relief.domain.geofencing;

/**
 * Types of events that can occur within geofences
 */
public enum GeofenceEventType {
    ENTRY,                  // Entity entered the geofence
    EXIT,                   // Entity exited the geofence
    DWELL,                  // Entity dwelling within the geofence
    PROXIMITY,              // Entity in proximity to the geofence
    VIOLATION,              // Violation of geofence rules
    EMERGENCY,              // Emergency situation within geofence
    RESOURCE_DEPLETION,     // Resource depletion in geofence
    CAPACITY_EXCEEDED,      // Capacity exceeded in geofence
    MAINTENANCE_REQUIRED,   // Maintenance required in geofence
    STATUS_CHANGE,          // Status change within geofence
    CUSTOM                  // Custom event type
}



