package com.relief.domain.indoor;

/**
 * Types of indoor navigation edges
 */
public enum IndoorEdgeType {
    CORRIDOR,              // Regular corridor or hallway
    DOORWAY,               // Doorway or passage
    STAIRS,                // Staircase
    ELEVATOR,              // Elevator
    RAMP,                  // Ramp for accessibility
    ESCALATOR,             // Escalator
    BRIDGE,                // Bridge or walkway
    TUNNEL,                // Tunnel or underground passage
    EMERGENCY_ROUTE,       // Emergency evacuation route
    SERVICE_ROUTE,         // Service or maintenance route
    PUBLIC_ROUTE,          // Public access route
    PRIVATE_ROUTE,         // Private or restricted route
    CUSTOM                 // Custom edge type
}



