package com.relief.domain.location;

/**
 * Types of movement patterns detected from location history
 */
public enum PatternType {
    // Movement patterns
    LINEAR_MOVEMENT,            // Straight-line movement
    CIRCULAR_MOVEMENT,          // Circular or orbital movement
    RANDOM_MOVEMENT,            // Random or unpredictable movement
    GRID_PATTERN,               // Grid-based search pattern
    SPIRAL_PATTERN,             // Spiral search pattern
    ZIGZAG_PATTERN,             // Zigzag movement pattern
    BACK_AND_FORTH,             // Back and forth movement
    
    // Stationary patterns
    STATIONARY_CLUSTER,         // Cluster of stationary points
    WAITING_PATTERN,            // Waiting or idle pattern
    WORK_STATION,               // Work station pattern
    REST_AREA,                  // Rest or break area pattern
    
    // Route patterns
    COMMUTE_ROUTE,              // Regular commute route
    SUPPLY_ROUTE,               // Supply delivery route
    PATROL_ROUTE,               // Security or patrol route
    EMERGENCY_ROUTE,            // Emergency response route
    EVACUATION_ROUTE,           // Evacuation route
    
    // Search patterns
    SEARCH_GRID,                // Grid search pattern
    SEARCH_SPIRAL,              // Spiral search pattern
    SEARCH_RANDOM,              // Random search pattern
    SEARCH_SYSTEMATIC,          // Systematic search pattern
    COVERAGE_PATTERN,           // Area coverage pattern
    
    // Response patterns
    EMERGENCY_RESPONSE,         // Emergency response pattern
    RESCUE_OPERATION,           // Rescue operation pattern
    MEDICAL_RESPONSE,           // Medical response pattern
    FIRE_RESPONSE,              // Fire response pattern
    FLOOD_RESPONSE,             // Flood response pattern
    
    // Resource patterns
    RESOURCE_GATHERING,         // Resource gathering pattern
    SUPPLY_DISTRIBUTION,        // Supply distribution pattern
    EQUIPMENT_DEPLOYMENT,       // Equipment deployment pattern
    PERSONNEL_DEPLOYMENT,       // Personnel deployment pattern
    
    // Communication patterns
    COMMUNICATION_HUB,          // Communication hub pattern
    RELAY_STATION,              // Relay station pattern
    COORDINATION_POINT,         // Coordination point pattern
    
    // Environmental patterns
    WEATHER_AVOIDANCE,          // Weather avoidance pattern
    TERRAIN_FOLLOWING,          // Terrain following pattern
    OBSTACLE_AVOIDANCE,         // Obstacle avoidance pattern
    EFFICIENT_PATH,             // Most efficient path pattern
    
    // Anomaly patterns
    ANOMALY_DETECTED,           // Anomaly in movement pattern
    DEVIATION_FROM_NORM,        // Deviation from normal pattern
    UNUSUAL_ACTIVITY,           // Unusual activity pattern
    SUSPICIOUS_MOVEMENT,        // Suspicious movement pattern
    
    // Optimization patterns
    OPTIMIZED_ROUTE,            // Optimized route pattern
    EFFICIENT_MOVEMENT,         // Efficient movement pattern
    TIME_OPTIMIZED,             // Time-optimized pattern
    DISTANCE_OPTIMIZED,         // Distance-optimized pattern
    RESOURCE_OPTIMIZED,         // Resource-optimized pattern
    
    // Custom patterns
    CUSTOM_PATTERN,             // Custom defined pattern
    USER_DEFINED,               // User defined pattern
    MACHINE_LEARNED,            // Machine learning detected pattern
    AI_GENERATED                // AI generated pattern
}



