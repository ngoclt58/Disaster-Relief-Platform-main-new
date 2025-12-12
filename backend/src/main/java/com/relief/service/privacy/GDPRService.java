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
public class GDPRService {

    private final Map<String, DataSubjectRequest> requests = new ConcurrentHashMap<>();
    private final Map<String, ConsentRecord> consents = new ConcurrentHashMap<>();

    public DataSubjectRequest createRequest(String subjectId, String type, String details) {
        DataSubjectRequest r = new DataSubjectRequest();
        r.setId(UUID.randomUUID().toString());
        r.setSubjectId(subjectId);
        r.setType(type);
        r.setDetails(details);
        r.setStatus("OPEN");
        r.setCreatedAt(LocalDateTime.now());
        requests.put(r.getId(), r);
        return r;
    }

    public DataSubjectRequest updateRequestStatus(String requestId, String status, String resolution) {
        DataSubjectRequest r = requests.get(requestId);
        if (r == null) throw new IllegalArgumentException("Request not found");
        r.setStatus(status);
        r.setResolution(resolution);
        r.setUpdatedAt(LocalDateTime.now());
        return r;
    }

    public List<DataSubjectRequest> getRequests(String subjectId) {
        return requests.values().stream()
                .filter(r -> subjectId == null || Objects.equals(r.getSubjectId(), subjectId))
                .sorted(Comparator.comparing(DataSubjectRequest::getCreatedAt).reversed())
                .toList();
    }

    public ConsentRecord recordConsent(String subjectId, String purpose, boolean granted) {
        ConsentRecord c = new ConsentRecord();
        c.setId(UUID.randomUUID().toString());
        c.setSubjectId(subjectId);
        c.setPurpose(purpose);
        c.setGranted(granted);
        c.setTimestamp(LocalDateTime.now());
        consents.put(c.getId(), c);
        return c;
    }

    public List<ConsentRecord> getConsentHistory(String subjectId) {
        return consents.values().stream()
                .filter(c -> Objects.equals(c.getSubjectId(), subjectId))
                .sorted(Comparator.comparing(ConsentRecord::getTimestamp).reversed())
                .toList();
    }

    @lombok.Data
    public static class DataSubjectRequest {
        private String id;
        private String subjectId;
        private String type; // ACCESS, ERASURE, PORTABILITY, RECTIFICATION, RESTRICTION
        private String details;
        private String status; // OPEN, IN_PROGRESS, RESOLVED, REJECTED
        private String resolution;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @lombok.Data
    public static class ConsentRecord {
        private String id;
        private String subjectId;
        private String purpose;
        private boolean granted;
        private LocalDateTime timestamp;
    }
}




