package com.relief.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private NeedsRequest request;

    @jakarta.persistence.Transient
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "hub_id")
    private UUID hubId;

    @Column(name = "planned_kit_code")
    private String plannedKitCode;

    @Column(name = "title")
    private String title;

    @Column(name = "description", length = 2048)
    private String description;

    @Column(name = "priority")
    private String priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * Comma-separated list of skills required to complete this task.
     * Used by {@link com.relief.service.task.SkillBasedMatchingService}.
     */
    @Column(name = "required_skills")
    private String requiredSkills;

    /**
     * High-level task type/category (e.g. MEDICAL_RESPONSE, FOOD_DELIVERY).
     * Used by automation and dependency services.
     */
    @Column(name = "type")
    private String type;

    /**
     * Optional explicit task location. When present, this is used directly
     * for proximity calculations; otherwise the associated {@link NeedsRequest}
     * location may be used.
     */
    @Column(name = "location", columnDefinition = "geometry(Point, 4326)")
    private Point location;

    /**
     * Comma-separated list of task IDs (UUIDs) this task depends on.
     * This powers the {@link com.relief.service.task.TaskDependencyService}.
     */
    @Column(name = "dependencies")
    private String dependencies;

    @Builder.Default
    private String status = "new"; // new, assigned, picked_up, delivered, could_not_deliver

    private LocalDateTime eta;

    @Column(name = "route_id")
    private UUID routeId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Explicit getters and setters for Lombok compatibility
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public NeedsRequest getRequest() { return request; }
    public void setRequest(NeedsRequest request) { this.request = request; }
    
    public UUID getRequestId() { 
        return request != null ? request.getId() : requestId; 
    }
    public void setRequestId(UUID requestId) { 
        this.requestId = requestId;
    }

    public User getAssignee() { return assignee; }
    public void setAssignee(User assignee) { this.assignee = assignee; }

    public UUID getHubId() { return hubId; }
    public void setHubId(UUID hubId) { this.hubId = hubId; }

    public String getPlannedKitCode() { return plannedKitCode; }
    public void setPlannedKitCode(String plannedKitCode) { this.plannedKitCode = plannedKitCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }

    public String getDependencies() { return dependencies; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEta() { return eta; }
    public void setEta(LocalDateTime eta) { this.eta = eta; }

    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}




