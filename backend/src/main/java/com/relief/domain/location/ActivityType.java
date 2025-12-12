package com.relief.domain.location;

/**
 * Types of activities tracked in location history
 */
public enum ActivityType {
    // Movement activities
    WALKING,                    // Walking on foot
    RUNNING,                    // Running
    DRIVING,                    // Driving a vehicle
    FLYING,                     // Flying (helicopter, drone)
    BOATING,                    // Boating or water transport
    CYCLING,                    // Cycling
    STATIONARY,                 // Stationary/not moving
    
    // Work activities
    SEARCH_AND_RESCUE,          // Search and rescue operations
    MEDICAL_TREATMENT,          // Providing medical treatment
    SUPPLY_DELIVERY,            // Delivering supplies
    EVACUATION,                 // Evacuation operations
    DAMAGE_ASSESSMENT,          // Assessing damage
    INFRASTRUCTURE_REPAIR,      // Repairing infrastructure
    COMMUNICATION,              // Communication activities
    COORDINATION,               // Coordination activities
    
    // Relief activities
    FOOD_DISTRIBUTION,          // Distributing food
    WATER_DISTRIBUTION,         // Distributing water
    SHELTER_MANAGEMENT,         // Managing shelters
    RESOURCE_COLLECTION,        // Collecting resources
    VOLUNTEER_COORDINATION,     // Coordinating volunteers
    PUBLIC_INFORMATION,         // Providing public information
    
    // Emergency activities
    EMERGENCY_RESPONSE,         // Emergency response
    FIRE_FIGHTING,              // Fire fighting
    FLOOD_RESPONSE,             // Flood response
    EARTHQUAKE_RESPONSE,        // Earthquake response
    HURRICANE_RESPONSE,         // Hurricane response
    TORNADO_RESPONSE,           // Tornado response
    
    // Administrative activities
    MEETING,                    // Attending meetings
    PLANNING,                   // Planning activities
    REPORTING,                  // Reporting activities
    TRAINING,                   // Training activities
    MAINTENANCE,                // Maintenance activities
    
    // Unknown or other
    UNKNOWN,                    // Unknown activity
    OTHER                       // Other activity
}



