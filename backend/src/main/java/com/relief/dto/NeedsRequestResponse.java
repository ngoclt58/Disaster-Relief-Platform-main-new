package com.relief.dto;

import com.relief.entity.NeedsRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NeedsRequestResponse {
    private UUID id;
    private String type;
    private String category; // Alias for type for frontend compatibility
    private Integer severity;
    private String notes;
    private String description; // Alias for notes
    private String status;
    private String address;
    private Location location; // Converted from geomPoint
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String currentAssignee;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private double[] coordinates; // [lng, lat] format
        private String type = "Point";
    }

    public static NeedsRequestResponse fromEntity(NeedsRequest entity) {
        NeedsRequestResponseBuilder builder = NeedsRequestResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .category(entity.getType()) // Frontend expects 'category'
                .severity(entity.getSeverity())
                .notes(entity.getNotes())
                .description(entity.getNotes()) // Frontend expects 'description'
                .status(entity.getStatus())
                .address(entity.getAddress())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // Convert Point to coordinates array [lng, lat]
        if (entity.getGeomPoint() != null) {
            Point point = entity.getGeomPoint();
            Location location = Location.builder()
                    .coordinates(new double[]{point.getX(), point.getY()})
                    .type("Point")
                    .build();
            builder.location(location);
        }

        // Add user info if available
        if (entity.getCreatedBy() != null) {
            builder.createdBy(entity.getCreatedBy().getEmail() != null 
                ? entity.getCreatedBy().getEmail() 
                : entity.getCreatedBy().getFullName());
        }

        if (entity.getCurrentAssignee() != null) {
            builder.currentAssignee(entity.getCurrentAssignee().getEmail() != null 
                ? entity.getCurrentAssignee().getEmail() 
                : entity.getCurrentAssignee().getFullName());
        }

        return builder.build();
    }
}

