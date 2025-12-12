package com.relief.service;

import com.relief.entity.DedupeGroup;
import com.relief.entity.DedupeLink;
import com.relief.repository.DedupeGroupRepository;
import com.relief.repository.DedupeLinkRepository;
import com.relief.repository.NeedsRequestRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class DedupeServiceTest {

    @Test
    void mergeGroupMarksMergedAndAudits() {
        DedupeGroupRepository groupRepo = mock(DedupeGroupRepository.class);
        DedupeLinkRepository linkRepo = mock(DedupeLinkRepository.class);
        NeedsRequestRepository needsRepo = mock(NeedsRequestRepository.class);
        AuditService audit = mock(AuditService.class);
        DedupeService service = new DedupeService(groupRepo, linkRepo, needsRepo, audit, new SimpleMeterRegistry());

        UUID groupId = UUID.randomUUID();
        DedupeGroup group = DedupeGroup.builder().id(groupId).entityType("NEEDS").status("OPEN").createdAt(LocalDateTime.now()).build();
        when(groupRepo.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(linkRepo.findByGroup(group)).thenReturn(List.of(DedupeLink.builder().entityId(UUID.randomUUID()).createdAt(LocalDateTime.now()).build()));

        service.mergeGroup(groupId, UUID.randomUUID(), null);

        verify(groupRepo, times(1)).save(group);
        verify(audit, times(1)).logAdminAction(eq("DEDUPE_GROUP_MERGED"), anyString());
    }
}



