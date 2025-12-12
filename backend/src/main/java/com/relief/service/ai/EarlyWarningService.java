package com.relief.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EarlyWarningService {

    private static final Logger log = LoggerFactory.getLogger(EarlyWarningService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, WarningRule> rules = new ConcurrentHashMap<>();
    private final Map<String, List<EarlyWarning>> warnings = new ConcurrentHashMap<>();
    private final List<WarningSubscriber> subscribers = new ArrayList<>();

    public WarningRule createRule(String name, String description, String triggerCondition,
                                Map<String, Object> thresholds, String action) {
        WarningRule rule = new WarningRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(name);
        rule.setDescription(description);
        rule.setTriggerCondition(triggerCondition);
        rule.setThresholds(thresholds);
        rule.setAction(action);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setIsActive(true);
        rule.setSeverity("MEDIUM");
        
        rules.put(rule.getId(), rule);
        
        log.info("Created early warning rule: {}", rule.getId());
        return rule;
    }

    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::checkWarningConditions, 0, 60, TimeUnit.SECONDS);
        log.info("Early warning monitoring started");
    }

    public void stopMonitoring() {
        scheduler.shutdown();
        log.info("Early warning monitoring stopped");
    }

    private void checkWarningConditions() {
        for (WarningRule rule : rules.values()) {
            if (!rule.isActive()) continue;
            
            if (evaluateCondition(rule)) {
                EarlyWarning warning = createWarning(rule);
                warnings.computeIfAbsent(rule.getId(), k -> new ArrayList<>()).add(warning);
                
                // Notify subscribers
                notifySubscribers(warning);
                
                log.warn("Early warning triggered: {} - {}", rule.getName(), warning.getMessage());
            }
        }
    }

    private boolean evaluateCondition(WarningRule rule) {
        // Simplified condition evaluation
        Map<String, Object> thresholds = rule.getThresholds();
        
        // Simulate checking various metrics
        Random random = new Random();
        for (Map.Entry<String, Object> entry : thresholds.entrySet()) {
            String key = entry.getKey();
            Object threshold = entry.getValue();
            
            // Simulate metric value
            double metricValue = random.nextDouble() * 100;
            
            if (threshold instanceof Number) {
                if (metricValue > ((Number) threshold).doubleValue()) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private EarlyWarning createWarning(WarningRule rule) {
        EarlyWarning warning = new EarlyWarning();
        warning.setId(UUID.randomUUID().toString());
        warning.setRuleId(rule.getId());
        warning.setTitle(rule.getName());
        warning.setMessage(generateMessage(rule));
        warning.setSeverity(rule.getSeverity());
        warning.setTriggeredAt(LocalDateTime.now());
        warning.setAction(rule.getAction());
        warning.setIsAcknowledged(false);
        
        return warning;
    }

    private String generateMessage(WarningRule rule) {
        return String.format("Early warning: %s - %s. Triggered due to: %s", 
                rule.getName(), rule.getDescription(), rule.getTriggerCondition());
    }

    private void notifySubscribers(EarlyWarning warning) {
        for (WarningSubscriber subscriber : subscribers) {
            try {
                subscriber.onWarning(warning);
            } catch (Exception e) {
                log.error("Failed to notify subscriber: {}", subscriber.getId(), e);
            }
        }
    }

    public void subscribe(WarningSubscriber subscriber) {
        subscribers.add(subscriber);
        log.info("Subscriber added: {}", subscriber.getId());
    }

    public void unsubscribe(String subscriberId) {
        subscribers.removeIf(s -> s.getId().equals(subscriberId));
        log.info("Subscriber removed: {}", subscriberId);
    }

    public List<EarlyWarning> getActiveWarnings() {
        return warnings.values().stream()
                .flatMap(List::stream)
                .filter(w -> !w.isAcknowledged())
                .sorted((a, b) -> b.getTriggeredAt().compareTo(a.getTriggeredAt()))
                .collect(Collectors.toList());
    }

    public List<EarlyWarning> getWarningsByRule(String ruleId, LocalDateTime startTime, LocalDateTime endTime) {
        return warnings.getOrDefault(ruleId, new ArrayList<>()).stream()
                .filter(w -> w.getTriggeredAt().isAfter(startTime) && w.getTriggeredAt().isBefore(endTime))
                .sorted((a, b) -> b.getTriggeredAt().compareTo(a.getTriggeredAt()))
                .collect(Collectors.toList());
    }

    public void acknowledgeWarning(String warningId, String acknowledgedBy) {
        warnings.values().stream()
                .flatMap(List::stream)
                .filter(w -> w.getId().equals(warningId))
                .forEach(w -> {
                    w.setIsAcknowledged(true);
                    w.setAcknowledgedAt(LocalDateTime.now());
                    w.setAcknowledgedBy(acknowledgedBy);
                });
        
        log.info("Warning acknowledged: {} by {}", warningId, acknowledgedBy);
    }

    public WarningRule getRule(String ruleId) {
        return rules.get(ruleId);
    }

    public List<WarningRule> getRules() {
        return new ArrayList<>(rules.values());
    }

    public void updateRule(String ruleId, String name, String description, String triggerCondition,
                          Map<String, Object> thresholds, String action, String severity) {
        WarningRule rule = rules.get(ruleId);
        if (rule != null) {
            rule.setName(name);
            rule.setDescription(description);
            rule.setTriggerCondition(triggerCondition);
            rule.setThresholds(thresholds);
            rule.setAction(action);
            rule.setSeverity(severity);
            rule.setUpdatedAt(LocalDateTime.now());
            log.info("Updated warning rule: {}", ruleId);
        }
    }

    public void deleteRule(String ruleId) {
        rules.remove(ruleId);
        warnings.remove(ruleId);
        log.info("Deleted warning rule: {}", ruleId);
    }

    // Data classes
    public static class WarningRule {
        private String id;
        private String name;
        private String description;
        private String triggerCondition;
        private Map<String, Object> thresholds;
        private String action;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isActive;
        private String severity;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTriggerCondition() { return triggerCondition; }
        public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }

        public Map<String, Object> getThresholds() { return thresholds; }
        public void setThresholds(Map<String, Object> thresholds) { this.thresholds = thresholds; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public boolean isActive() { return isActive; }
        public void setIsActive(boolean isActive) { this.isActive = isActive; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    public static class EarlyWarning {
        private String id;
        private String ruleId;
        private String title;
        private String message;
        private String severity;
        private LocalDateTime triggeredAt;
        private String action;
        private boolean isAcknowledged;
        private LocalDateTime acknowledgedAt;
        private String acknowledgedBy;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public LocalDateTime getTriggeredAt() { return triggeredAt; }
        public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public boolean isAcknowledged() { return isAcknowledged; }
        public void setIsAcknowledged(boolean isAcknowledged) { this.isAcknowledged = isAcknowledged; }

        public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
        public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

        public String getAcknowledgedBy() { return acknowledgedBy; }
        public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
    }

    public static class WarningSubscriber {
        private String id;
        private String type;
        private String endpoint;

        public WarningSubscriber(String id, String type, String endpoint) {
            this.id = id;
            this.type = type;
            this.endpoint = endpoint;
        }

        public String getId() { return id; }

        public void onWarning(EarlyWarning warning) {
            // Notification logic
        }
    }
}

