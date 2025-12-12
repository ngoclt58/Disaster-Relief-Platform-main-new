package com.relief.domain.location;

/**
 * Status of location optimizations
 */
public enum OptimizationStatus {
    PENDING,                    // Optimization pending review
    APPROVED,                   // Optimization approved for implementation
    IN_PROGRESS,                // Optimization being implemented
    COMPLETED,                  // Optimization completed
    REJECTED,                   // Optimization rejected
    CANCELLED,                  // Optimization cancelled
    ON_HOLD,                    // Optimization on hold
    NEEDS_REVISION,             // Optimization needs revision
    IMPLEMENTED,                // Optimization implemented
    FAILED                      // Optimization implementation failed
}



