package com.relief.domain.geofencing;

/**
 * Status of geofence alerts
 */
public enum GeofenceAlertStatus {
    ACTIVE,         // Alert is active and requires attention
    ACKNOWLEDGED,   // Alert has been acknowledged
    IN_PROGRESS,    // Alert is being worked on
    RESOLVED,       // Alert has been resolved
    ESCALATED,      // Alert has been escalated
    CANCELLED,      // Alert has been cancelled
    EXPIRED         // Alert has expired
}



