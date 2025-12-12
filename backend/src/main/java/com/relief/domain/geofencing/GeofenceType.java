package com.relief.domain.geofencing;

/**
 * Types of geofences for different monitoring purposes
 */
public enum GeofenceType {
    DISASTER_ZONE,          // Areas affected by disaster
    EVACUATION_ZONE,        // Mandatory evacuation areas
    RESTRICTED_ZONE,        // Restricted access areas
    RESOURCE_DEPOT,         // Resource storage and distribution centers
    EMERGENCY_SHELTER,      // Emergency shelter locations
    MEDICAL_FACILITY,       // Medical facilities and hospitals
    COMMUNICATION_HUB,      // Communication and coordination centers
    SUPPLY_ROUTE,           // Critical supply routes
    INFRASTRUCTURE,         // Critical infrastructure sites
    POPULATION_DENSITY,     // High population density areas
    VULNERABLE_AREA,        // Areas with vulnerable populations
    RESPONSE_BASE,          // Emergency response base camps
    CHECKPOINT,             // Security and access checkpoints
    QUARANTINE_ZONE,        // Quarantine and isolation areas
    RECOVERY_ZONE,          // Areas in recovery phase
    CUSTOM                  // Custom geofence type
}



