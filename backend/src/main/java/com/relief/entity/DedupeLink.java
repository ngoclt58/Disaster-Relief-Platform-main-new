package com.relief.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dedupe_links")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DedupeLink {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private DedupeGroup group;

    @Column(name = "entity_id", nullable = false, columnDefinition = "uuid")
    private UUID entityId; // The duplicate entity id (e.g., NeedsRequest id)

    @Column(name = "score")
    private Double score; // similarity score

    @Column(name = "reason", length = 1000)
    private String reason; // e.g., same phone + bbox proximity

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Explicit getters and setters for Lombok compatibility
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DedupeGroup getGroup() { return group; }
    public void setGroup(DedupeGroup group) { this.group = group; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private DedupeGroup group;
        private UUID entityId;
        private Double score;
        private String reason;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder group(DedupeGroup group) { this.group = group; return this; }
        public Builder entityId(UUID entityId) { this.entityId = entityId; return this; }
        public Builder score(Double score) { this.score = score; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DedupeLink build() {
            DedupeLink link = new DedupeLink();
            link.setId(id);
            link.setGroup(group);
            link.setEntityId(entityId);
            link.setScore(score);
            link.setReason(reason);
            link.setCreatedAt(createdAt);
            return link;
        }
    }
}





