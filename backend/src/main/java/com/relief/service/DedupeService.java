package com.relief.service;

import com.relief.entity.DedupeGroup;
import com.relief.entity.DedupeLink;
import com.relief.entity.NeedsRequest;
import com.relief.entity.User;
import com.relief.repository.DedupeGroupRepository;
import com.relief.repository.DedupeLinkRepository;
import com.relief.repository.NeedsRequestRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DedupeService {

    private final DedupeGroupRepository groupRepository;
    private final DedupeLinkRepository linkRepository;
    private final NeedsRequestRepository needsRequestRepository;
    private final AuditService auditService;
    private final MeterRegistry meterRegistry;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public DedupeGroup createGroup(String entityType, List<DedupeCandidate> candidates, User createdBy, String note) {
        DedupeGroup group = DedupeGroup.builder()
                .entityType(entityType)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .note(note)
                .build();
        group = groupRepository.save(group);

        for (DedupeCandidate c : candidates) {
            DedupeLink link = DedupeLink.builder()
                    .group(group)
                    .entityId(c.entityId())
                    .score(c.score())
                    .reason(c.reason())
                    .createdAt(LocalDateTime.now())
                    .build();
            linkRepository.save(link);
        }

        auditService.logAdminAction("DEDUPE_GROUP_CREATED",
                "Dedupe group %s created with %d links".formatted(group.getId(), candidates.size()));
        meterRegistry.counter("dedupe.groups.created").increment();

        return group;
    }

    @Transactional(readOnly = true)
    public List<DedupeLink> getGroupLinks(UUID groupId) {
        DedupeGroup group = groupRepository.findById(groupId).orElseThrow();
        return linkRepository.findByGroup(group);
    }

    @Transactional
    public void dismissGroup(UUID groupId, User actor, String reason) {
        DedupeGroup group = groupRepository.findById(groupId).orElseThrow();
        group.setStatus("DISMISSED");
        groupRepository.save(group);
        auditService.logAdminAction("DEDUPE_GROUP_DISMISSED",
                "Dedupe group %s dismissed: %s".formatted(groupId, reason));
    }

    @Transactional
    public void mergeGroup(UUID groupId, UUID canonicalEntityId, User actor) {
        DedupeGroup group = groupRepository.findById(groupId).orElseThrow();
        List<DedupeLink> links = linkRepository.findByGroup(group);

        String entityType = group.getEntityType() != null ? group.getEntityType().toUpperCase() : "";
        switch (entityType) {
            case "NEEDS":
            case "NEEDS_REQUEST":
                mergeNeedsRequests(canonicalEntityId, links);
                break;
            default:
                // For unsupported types, mark merged but record audit only
                break;
        }

        group.setStatus("MERGED");
        groupRepository.save(group);
        auditService.logAdminAction("DEDUPE_GROUP_MERGED",
                "Dedupe group %s merged into %s with %d links".formatted(groupId, canonicalEntityId, links.size()));
        meterRegistry.counter("dedupe.groups.merged", "entityType", entityType).increment();
    }

    @Transactional
    public int mergeRequests(List<UUID> requestIds, String reason) {
        if (requestIds == null || requestIds.isEmpty()) {
            return 0;
        }
        
        UUID canonicalId = requestIds.get(0);
        int updated = 0;
        
        for (int i = 1; i < requestIds.size(); i++) {
            UUID duplicateId = requestIds.get(i);
            if (duplicateId == null || duplicateId.equals(canonicalId)) continue;
            
            NeedsRequest req = needsRequestRepository.findById(duplicateId).orElse(null);
            if (req != null) {
                req.setStatus("merged");
                needsRequestRepository.save(req);
                updated++;
            }
        }
        
        if (updated > 0) {
            auditService.logAdminAction("DEDUPE_REQUESTS_MERGED",
                    "Merged %d requests into %s. Reason: %s".formatted(updated, canonicalId, reason != null ? reason : "N/A"));
            meterRegistry.counter("dedupe.requests.merged").increment(updated);
        }
        
        return updated;
    }

    private void mergeNeedsRequests(UUID canonicalId, List<DedupeLink> links) {
        for (DedupeLink link : links) {
            UUID duplicateId = link.getEntityId();
            if (duplicateId == null || duplicateId.equals(canonicalId)) continue;
            NeedsRequest req = needsRequestRepository.findById(duplicateId).orElse(null);
            if (req != null) {
                req.setStatus("merged");
                needsRequestRepository.save(req);
            }
        }
    }

    public record DedupeCandidate(UUID entityId, Double score, String reason) {}
}


