package com.relief.domain.location;

/**
 * Types of location optimizations
 */
public enum OptimizationType {
    // Route optimizations
    ROUTE_OPTIMIZATION,         // Optimize travel routes
    SHORTEST_PATH,              // Find shortest path
    FASTEST_PATH,               // Find fastest path
    FUEL_EFFICIENT_ROUTE,       // Most fuel-efficient route
    TIME_OPTIMIZED_ROUTE,       // Time-optimized route
    DISTANCE_OPTIMIZED_ROUTE,   // Distance-optimized route
    
    // Resource optimizations
    RESOURCE_ALLOCATION,        // Optimize resource allocation
    PERSONNEL_DEPLOYMENT,       // Optimize personnel deployment
    EQUIPMENT_PLACEMENT,        // Optimize equipment placement
    SUPPLY_CHAIN_OPTIMIZATION,  // Optimize supply chain
    INVENTORY_OPTIMIZATION,     // Optimize inventory management
    
    // Coverage optimizations
    AREA_COVERAGE,              // Optimize area coverage
    SEARCH_PATTERN_OPTIMIZATION, // Optimize search patterns
    PATROL_ROUTE_OPTIMIZATION,  // Optimize patrol routes
    MONITORING_OPTIMIZATION,    // Optimize monitoring coverage
    
    // Response optimizations
    EMERGENCY_RESPONSE_TIME,    // Optimize emergency response time
    RESCUE_OPERATION_EFFICIENCY, // Optimize rescue operations
    MEDICAL_RESPONSE_OPTIMIZATION, // Optimize medical response
    EVACUATION_OPTIMIZATION,    // Optimize evacuation procedures
    
    // Communication optimizations
    COMMUNICATION_NETWORK,      // Optimize communication network
    RELAY_STATION_PLACEMENT,    // Optimize relay station placement
    COORDINATION_POINT_OPTIMIZATION, // Optimize coordination points
    
    // Environmental optimizations
    WEATHER_AVOIDANCE,          // Optimize for weather conditions
    TERRAIN_OPTIMIZATION,       // Optimize for terrain
    OBSTACLE_AVOIDANCE,         // Optimize obstacle avoidance
    ACCESSIBILITY_OPTIMIZATION, // Optimize for accessibility
    
    // Efficiency optimizations
    WORKFLOW_OPTIMIZATION,      // Optimize workflows
    PROCESS_OPTIMIZATION,       // Optimize processes
    TASK_SEQUENCING,            // Optimize task sequencing
    SCHEDULE_OPTIMIZATION,      // Optimize schedules
    
    // Safety optimizations
    SAFETY_OPTIMIZATION,        // Optimize for safety
    RISK_REDUCTION,             // Reduce risks
    HAZARD_AVOIDANCE,           // Avoid hazards
    EMERGENCY_PREPAREDNESS,     // Optimize emergency preparedness
    
    // Cost optimizations
    COST_REDUCTION,             // Reduce costs
    BUDGET_OPTIMIZATION,        // Optimize budget allocation
    RESOURCE_EFFICIENCY,        // Improve resource efficiency
    WASTE_REDUCTION,            // Reduce waste
    
    // Performance optimizations
    PERFORMANCE_IMPROVEMENT,    // Improve performance
    THROUGHPUT_OPTIMIZATION,    // Optimize throughput
    LATENCY_REDUCTION,          // Reduce latency
    CAPACITY_OPTIMIZATION,      // Optimize capacity
    
    // Custom optimizations
    CUSTOM_OPTIMIZATION,        // Custom optimization
    USER_DEFINED,               // User defined optimization
    MACHINE_LEARNED,            // Machine learning optimization
    AI_GENERATED                // AI generated optimization
}



