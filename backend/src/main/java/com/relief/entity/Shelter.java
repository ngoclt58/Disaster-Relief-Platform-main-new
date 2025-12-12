package com.relief.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shelters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private Integer capacity = 0;

    @Column(name = "current_occupancy", nullable = false)
    @Builder.Default
    private Integer currentOccupancy = 0;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "facilities", columnDefinition = "text[]")
    private List<String> facilities;

    @Column(name = "geom_point", columnDefinition = "geometry(Point, 4326)")
    @JsonIgnore
    private Point geomPoint;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "is_emergency_shelter", nullable = false)
    @Builder.Default
    private Boolean isEmergencyShelter = true;

    @Column(name = "accessibility_features", columnDefinition = "text[]")
    private List<String> accessibilityFeatures;

    @Column(name = "operating_hours")
    @Builder.Default
    private String operatingHours = "24/7";

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "manager_phone", length = 50)
    private String managerPhone;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    private User updatedBy;

    // Helper methods
    public boolean isFull() {
        return currentOccupancy >= capacity;
    }

    public boolean isAvailable() {
        return "ACTIVE".equals(status) && !isFull();
    }

    public int getAvailableCapacity() {
        return Math.max(0, capacity - currentOccupancy);
    }

    public double getOccupancyRate() {
        if (capacity == 0) return 0.0;
        return (double) currentOccupancy / capacity * 100.0;
    }

    // Enum for status values
    public enum ShelterStatus {
        ACTIVE, FULL, CLOSED, MAINTENANCE
    }
}