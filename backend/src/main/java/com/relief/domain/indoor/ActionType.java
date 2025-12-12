package com.relief.domain.indoor;

/**
 * Types of actions in indoor navigation steps
 */
public enum ActionType {
    START,                  // Start of route
    CONTINUE_STRAIGHT,      // Continue straight
    TURN_LEFT,             // Turn left
    TURN_RIGHT,            // Turn right
    TURN_AROUND,           // Turn around
    GO_UP_STAIRS,          // Go up stairs
    GO_DOWN_STAIRS,        // Go down stairs
    TAKE_ELEVATOR,         // Take elevator
    ENTER_ROOM,            // Enter room
    EXIT_ROOM,             // Exit room
    GO_THROUGH_DOOR,       // Go through door
    CROSS_CORRIDOR,        // Cross corridor
    WAIT,                  // Wait or pause
    ARRIVE,                // Arrive at destination
    CUSTOM                 // Custom action
}



