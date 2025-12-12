package com.relief.domain.indoor;

/**
 * Types of indoor navigation routes
 */
public enum IndoorRouteType {
    SHORTEST_PATH,         // Shortest distance route
    FASTEST_PATH,          // Fastest time route
    ACCESSIBLE_PATH,       // Wheelchair accessible route
    EMERGENCY_EVACUATION,  // Emergency evacuation route
    EMERGENCY_RESPONSE,    // Emergency response route
    SERVICE_ROUTE,         // Service or maintenance route
    PUBLIC_ROUTE,          // Public access route
    PRIVATE_ROUTE,         // Private or restricted route
    SCENIC_ROUTE,          // Scenic or preferred route
    AVOID_CROWDS,          // Route avoiding crowded areas
    CUSTOM                 // Custom route type
}



