package com.relief.service.privacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditTrailService {

    private final Map<String, AuditEvent> events = new ConcurrentHashMap<>();

    public AuditEvent log(String actorId, String action, String entityType, String entityId, Map<String, Object> metadata) {
        AuditEvent e = new AuditEvent();
        e.setId(UUID.randomUUID().toString());
        e.setActorId(actorId);
        e.setAction(action);
        e.setEntityType(entityType);
        e.setEntityId(entityId);
        e.setMetadata(metadata != null ? metadata : new HashMap<>());
        e.setTimestamp(LocalDateTime.now());
        events.put(e.getId(), e);
        return e;
    }

    public List<AuditEvent> recent(int limit, String entityType, String entityId) {
        return events.values().stream()
                .filter(e -> entityType == null || Objects.equals(e.getEntityType(), entityType))
                .filter(e -> entityId == null || Objects.equals(e.getEntityId(), entityId))
                .sorted(Comparator.comparing(AuditEvent::getTimestamp).reversed())
                .limit(limit)
                .toList();
    }

    @lombok.Data
    public static class AuditEvent {
        private String id;
        private String actorId;
        private String action;
        private String entityType;
        private String entityId;
        private Map<String, Object> metadata;
        private LocalDateTime timestamp;
    }
}




