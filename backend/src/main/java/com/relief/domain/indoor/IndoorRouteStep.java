package com.relief.domain.indoor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * Represents a step in an indoor navigation route
 */
@Entity
@Table(name = "indoor_route_steps", indexes = {
    @Index(name = "idx_indoor_route_step_route", columnList = "route_id"),
    @Index(name = "idx_indoor_route_step_sequence", columnList = "sequence_number"),
    @Index(name = "idx_indoor_route_step_geom", columnList = "position")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndoorRouteStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private IndoorRoute route;
    
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;
    
    @Column(name = "instruction", nullable = false, columnDefinition = "TEXT")
    private String instruction; // Turn-by-turn instruction
    
    @Column(name = "position", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point position;
    
    @Column(name = "local_x", nullable = false)
    private Double localX; // Local coordinate X
    
    @Column(name = "local_y", nullable = false)
    private Double localY; // Local coordinate Y
    
    @Column(name = "distance_from_start", nullable = false)
    private Double distanceFromStart; // Distance from route start in meters
    
    @Column(name = "estimated_time_from_start", nullable = false)
    private Integer estimatedTimeFromStart; // Time from route start in seconds
    
    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    
    @Column(name = "action_direction")
    private String actionDirection; // Direction for the action (e.g., "left", "right", "straight")
    
    @Column(name = "landmark")
    private String landmark; // Nearby landmark or reference point
    
    @Column(name = "floor_level", nullable = false)
    private Integer floorLevel; // Floor level for this step
    
    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical; // Whether this step is critical for navigation
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Additional step data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isCritical == null) {
            isCritical = false;
        }
    }
}



