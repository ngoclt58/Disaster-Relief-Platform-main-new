package com.relief.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @Column(nullable = false)
    private String type; // image, video, document, audio

    @Column(nullable = false)
    private String url;

    private String sha256;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "geom_point", columnDefinition = "geometry(Point, 4326)")
    private Point geomPoint;

    @Builder.Default
    private Boolean redacted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Explicit getters and setters for Lombok compatibility
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getOwnerUser() { return ownerUser; }
    public void setOwnerUser(User ownerUser) { this.ownerUser = ownerUser; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }

    public Point getGeomPoint() { return geomPoint; }
    public void setGeomPoint(Point geomPoint) { this.geomPoint = geomPoint; }

    public Boolean getRedacted() { return redacted; }
    public void setRedacted(Boolean redacted) { this.redacted = redacted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder pattern for Lombok compatibility
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User ownerUser;
        private String type;
        private String url;
        private String sha256;
        private LocalDateTime takenAt;
        private Point geomPoint;
        private Boolean redacted = false;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder ownerUser(User ownerUser) {
            this.ownerUser = ownerUser;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder sha256(String sha256) {
            this.sha256 = sha256;
            return this;
        }

        public Builder takenAt(LocalDateTime takenAt) {
            this.takenAt = takenAt;
            return this;
        }

        public Builder geomPoint(Point geomPoint) {
            this.geomPoint = geomPoint;
            return this;
        }

        public Builder redacted(Boolean redacted) {
            this.redacted = redacted;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Media build() {
            Media media = new Media();
            media.setId(id);
            media.setOwnerUser(ownerUser);
            media.setType(type);
            media.setUrl(url);
            media.setSha256(sha256);
            media.setTakenAt(takenAt);
            media.setGeomPoint(geomPoint);
            media.setRedacted(redacted);
            media.setCreatedAt(createdAt);
            return media;
        }
    }
}



