package com.relief.service.workflow;

/**
 * Types of workflow steps
 */
public enum WorkflowStepType {
    CREATE_TASK,           // Create a new task
    SEND_NOTIFICATION,     // Send notification to users
    ASSIGN_USER,           // Assign user to task
    WAIT_FOR_CONDITION,    // Wait for specified time or condition
    PARALLEL_EXECUTION,    // Execute multiple steps in parallel
    CONDITIONAL_BRANCH,    // Conditional execution based on context
    UPDATE_STATUS,         // Update request or task status
    SEND_EMAIL,           // Send email notification
    CREATE_DELIVERY,      // Create delivery record
    ESCALATE              // Escalate to higher authority
}


