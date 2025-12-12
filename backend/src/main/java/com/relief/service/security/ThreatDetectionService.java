package com.relief.service.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ThreatDetectionService {

    private static final Logger log = LoggerFactory.getLogger(ThreatDetectionService.class);

    private final Map<String, SecurityEvent> events = new ConcurrentHashMap<>();
    private final Map<String, ThreatAlert> alerts = new ConcurrentHashMap<>();

    public SecurityEvent recordEvent(String userId, String ip, String action, Map<String, Object> context) {
        SecurityEvent e = new SecurityEvent();
        e.setId(UUID.randomUUID().toString());
        e.setUserId(userId);
        e.setIp(ip);
        e.setAction(action);
        e.setContext(context != null ? context : new HashMap<>());
        e.setTimestamp(LocalDateTime.now());
        e.setAnomalyScore(calculateAnomalyScore(e));
        events.put(e.getId(), e);

        if (e.getAnomalyScore() >= 0.8) {
            createThreatAlert(e);
        }
        return e;
    }

    public List<SecurityEvent> getRecentEvents(int limit) {
        return events.values().stream()
                .sorted(Comparator.comparing(SecurityEvent::getTimestamp).reversed())
                .limit(limit)
                .toList();
    }

    public List<ThreatAlert> getActiveAlerts() {
        return alerts.values().stream()
                .filter(a -> !a.isAcknowledged())
                .sorted(Comparator.comparing(ThreatAlert::getCreatedAt).reversed())
                .toList();
    }

    public void acknowledgeAlert(String alertId, String userId) {
        ThreatAlert a = alerts.get(alertId);
        if (a != null) {
            a.setAcknowledged(true);
            a.setAcknowledgedBy(userId);
            a.setAcknowledgedAt(LocalDateTime.now());
        }
    }

    private void createThreatAlert(SecurityEvent e) {
        ThreatAlert a = new ThreatAlert();
        a.setId(UUID.randomUUID().toString());
        a.setEventId(e.getId());
        a.setSeverity(e.getAnomalyScore() > 0.95 ? "CRITICAL" : e.getAnomalyScore() > 0.9 ? "HIGH" : "MEDIUM");
        a.setTitle("Anomalous security activity detected");
        a.setMessage("Anomalous event from IP %s with action %s".formatted(e.getIp(), e.getAction()));
        a.setCreatedAt(LocalDateTime.now());
        a.setAcknowledged(false);
        alerts.put(a.getId(), a);
        log.warn("Threat alert created: {} severity={} score={}", a.getId(), a.getSeverity(), e.getAnomalyScore());
    }

    private double calculateAnomalyScore(SecurityEvent e) {
        double score = 0.0;
        if (e.getAction() != null && e.getAction().toLowerCase().contains("admin")) score += 0.3;
        if (e.getIp() != null && (e.getIp().startsWith("192.168.") || e.getIp().startsWith("10."))) score += 0.1;
        Object failed = e.getContext().get("failedAttempts");
        if (failed instanceof Number n) score += Math.min(0.6, n.doubleValue() * 0.1);
        return Math.min(1.0, score);
    }

    public static class SecurityEvent {
        private String id;
        private String userId;
        private String ip;
        private String action;
        private Map<String, Object> context;
        private LocalDateTime timestamp;
        private double anomalyScore;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public double getAnomalyScore() { return anomalyScore; }
        public void setAnomalyScore(double anomalyScore) { this.anomalyScore = anomalyScore; }
    }

    public static class ThreatAlert {
        private String id;
        private String eventId;
        private String title;
        private String message;
        private String severity;
        private boolean acknowledged;
        private String acknowledgedBy;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public boolean isAcknowledged() { return acknowledged; }
        public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

        public String getAcknowledgedBy() { return acknowledgedBy; }
        public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

        public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
        public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}




