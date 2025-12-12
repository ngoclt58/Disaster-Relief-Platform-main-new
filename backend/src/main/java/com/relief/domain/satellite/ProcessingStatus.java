package com.relief.domain.satellite;

/**
 * Satellite image processing status
 */
public enum ProcessingStatus {
    PENDING,           // Image received, waiting for processing
    PROCESSING,        // Currently being processed
    COMPLETED,         // Processing completed successfully
    FAILED,            // Processing failed
    CANCELLED,         // Processing was cancelled
    ARCHIVED           // Image archived after processing
}



