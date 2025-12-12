package com.relief.service.realtime;

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

/**
 * Event correlation service to connect related events across different data sources
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventCorrelationService {

    private static final Logger log = LoggerFactory.getLogger(EventCorrelationService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, CorrelationRule> rules = new ConcurrentHashMap<>();
    private final Map<String, List<Event>> eventBuffer = new ConcurrentHashMap<>();
    private final Map<String, CorrelationResult> correlations = new ConcurrentHashMap<>();

    public CorrelationRule createRule(String name, String description, String pattern, 
                                    Map<String, Object> conditions, String action) {
        CorrelationRule rule = new CorrelationRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(name);
        rule.setDescription(description);
        rule.setPattern(pattern);
        rule.setConditions(conditions);
        rule.setAction(action);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setIsActive(true);
        rule.setPriority(1);
        
        rules.put(rule.getId(), rule);
        
        log.info("Created correlation rule: {} with pattern: {}", rule.getId(), pattern);
        return rule;
    }

    public void processEvent(Event event) {
        // Add event to buffer
        eventBuffer.computeIfAbsent(event.getSource(), k -> new ArrayList<>()).add(event);
        
        // Clean old events from buffer
        cleanEventBuffer();
        
        // Check for correlations
        checkCorrelations(event);
        
        log.debug("Processed event: {} from source: {}", event.getId(), event.getSource());
    }

    public CorrelationResult correlateEvents(String ruleId, List<String> eventIds) {
        CorrelationRule rule = rules.get(ruleId);
        if (rule == null) {
            return null;
        }

        List<Event> events = eventIds.stream()
                .map(this::findEventById)
                .filter(Objects::nonNull)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (events.size() < 2) {
            return null;
        }

        CorrelationResult result = new CorrelationResult();
        result.setId(UUID.randomUUID().toString());
        result.setRuleId(ruleId);
        result.setEvents(events);
        result.setCorrelationScore(calculateCorrelationScore(events, rule));
        result.setCorrelatedAt(LocalDateTime.now());
        result.setIsSignificant(result.getCorrelationScore() > 0.7);

        correlations.put(result.getId(), result);
        
        log.info("Correlated {} events with score: {}", events.size(), result.getCorrelationScore());
        return result;
    }

    public List<CorrelationResult> findCorrelations(String source, LocalDateTime startTime, LocalDateTime endTime) {
        return correlations.values().stream()
                .filter(result -> {
                    boolean timeMatch = result.getCorrelatedAt().isAfter(startTime) && 
                                     result.getCorrelatedAt().isBefore(endTime);
                    boolean sourceMatch = source == null || result.getEvents().stream()
                            .anyMatch(event -> event.getSource().equals(source));
                    return timeMatch && sourceMatch;
                })
                .sorted((a, b) -> b.getCorrelatedAt().compareTo(a.getCorrelatedAt()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public EventPattern detectPattern(String source, String eventType, LocalDateTime startTime, LocalDateTime endTime) {
        List<Event> events = eventBuffer.getOrDefault(source, new ArrayList<>()).stream()
                .filter(event -> event.getType().equals(eventType))
                .filter(event -> event.getTimestamp().isAfter(startTime) && event.getTimestamp().isBefore(endTime))
                .sorted(Comparator.comparing(Event::getTimestamp))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (events.size() < 3) {
            return null;
        }

        EventPattern pattern = new EventPattern();
        pattern.setId(UUID.randomUUID().toString());
        pattern.setSource(source);
        pattern.setEventType(eventType);
        pattern.setEvents(events);
        pattern.setPatternType(detectPatternType(events));
        pattern.setConfidence(calculatePatternConfidence(events));
        pattern.setDetectedAt(LocalDateTime.now());
        pattern.setStartTime(startTime);
        pattern.setEndTime(endTime);

        log.info("Detected pattern: {} for source: {} with confidence: {}", 
                pattern.getPatternType(), source, pattern.getConfidence());
        return pattern;
    }

    public CorrelationAnalytics getAnalytics(String source) {
        CorrelationAnalytics analytics = new CorrelationAnalytics();
        analytics.setSource(source);
        analytics.setTotalEvents(0);
        analytics.setTotalCorrelations(0);
        analytics.setAverageCorrelationScore(0.0);
        analytics.setPatternCount(0);
        analytics.setLastAnalyzed(LocalDateTime.now());

        // Calculate analytics from stored data
        List<Event> sourceEvents = eventBuffer.getOrDefault(source, new ArrayList<>());
        analytics.setTotalEvents(sourceEvents.size());

        List<CorrelationResult> sourceCorrelations = correlations.values().stream()
                .filter(result -> result.getEvents().stream()
                        .anyMatch(event -> event.getSource().equals(source)))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        analytics.setTotalCorrelations(sourceCorrelations.size());
        analytics.setAverageCorrelationScore(sourceCorrelations.stream()
                .mapToDouble(CorrelationResult::getCorrelationScore)
                .average()
                .orElse(0.0));

        return analytics;
    }

    public CorrelationRule getRule(String ruleId) {
        return rules.get(ruleId);
    }

    public List<CorrelationRule> getRules() {
        return new ArrayList<>(rules.values());
    }

    public void updateRule(String ruleId, String name, String description, String pattern, 
                          Map<String, Object> conditions, String action) {
        CorrelationRule rule = rules.get(ruleId);
        if (rule != null) {
            rule.setName(name);
            rule.setDescription(description);
            rule.setPattern(pattern);
            rule.setConditions(conditions);
            rule.setAction(action);
            rule.setUpdatedAt(LocalDateTime.now());
            log.info("Updated correlation rule: {}", ruleId);
        }
    }

    public void deleteRule(String ruleId) {
        rules.remove(ruleId);
        log.info("Deleted correlation rule: {}", ruleId);
    }

    private void cleanEventBuffer() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        eventBuffer.values().forEach(events -> 
            events.removeIf(event -> event.getTimestamp().isBefore(cutoff)));
    }

    private void checkCorrelations(Event event) {
        for (CorrelationRule rule : rules.values()) {
            if (rule.isActive() && matchesRule(event, rule)) {
                List<Event> relatedEvents = findRelatedEvents(event, rule);
                if (relatedEvents.size() >= 2) {
                    correlateEvents(rule.getId(), 
                            relatedEvents.stream().map(Event::getId).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
                }
            }
        }
    }

    private boolean matchesRule(Event event, CorrelationRule rule) {
        // Simple rule matching logic
        Map<String, Object> conditions = rule.getConditions();
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String field = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = event.getData().get(field);
            
            if (!Objects.equals(actualValue, expectedValue)) {
                return false;
            }
        }
        return true;
    }

    private List<Event> findRelatedEvents(Event event, CorrelationRule rule) {
        List<Event> relatedEvents = new ArrayList<>();
        relatedEvents.add(event);
        
        // Find events within time window
        LocalDateTime timeWindow = event.getTimestamp().minusMinutes(30);
        for (List<Event> events : eventBuffer.values()) {
            for (Event e : events) {
                if (e.getTimestamp().isAfter(timeWindow) && 
                    e.getTimestamp().isBefore(event.getTimestamp()) &&
                    !e.getId().equals(event.getId())) {
                    relatedEvents.add(e);
                }
            }
        }
        
        return relatedEvents;
    }

    private Event findEventById(String eventId) {
        return eventBuffer.values().stream()
                .flatMap(List::stream)
                .filter(event -> event.getId().equals(eventId))
                .findFirst()
                .orElse(null);
    }

    private double calculateCorrelationScore(List<Event> events, CorrelationRule rule) {
        // Simple correlation scoring based on time proximity and data similarity
        if (events.size() < 2) return 0.0;
        
        double timeScore = calculateTimeScore(events);
        double dataScore = calculateDataScore(events);
        
        return (timeScore + dataScore) / 2.0;
    }

    private double calculateTimeScore(List<Event> events) {
        if (events.size() < 2) return 0.0;
        
        events.sort(Comparator.comparing(Event::getTimestamp));
        long totalTimeDiff = 0;
        for (int i = 1; i < events.size(); i++) {
            totalTimeDiff += java.time.Duration.between(
                    events.get(i-1).getTimestamp(), 
                    events.get(i).getTimestamp()
            ).toMinutes();
        }
        
        // Score decreases as time difference increases
        return Math.max(0, 1.0 - (totalTimeDiff / 60.0));
    }

    private double calculateDataScore(List<Event> events) {
        if (events.size() < 2) return 0.0;
        
        // Simple data similarity based on common fields
        Set<String> commonFields = new HashSet<>(events.get(0).getData().keySet());
        for (Event event : events.subList(1, events.size())) {
            commonFields.retainAll(event.getData().keySet());
        }
        
        return (double) commonFields.size() / events.get(0).getData().size();
    }

    private String detectPatternType(List<Event> events) {
        // Simple pattern detection
        if (events.size() < 3) return "INSUFFICIENT_DATA";
        
        // Check for sequential pattern
        boolean isSequential = true;
        for (int i = 1; i < events.size(); i++) {
            if (events.get(i).getTimestamp().isBefore(events.get(i-1).getTimestamp())) {
                isSequential = false;
                break;
            }
        }
        
        if (isSequential) return "SEQUENTIAL";
        
        // Check for burst pattern (many events in short time)
        long timeSpan = java.time.Duration.between(
                events.get(0).getTimestamp(), 
                events.get(events.size()-1).getTimestamp()
        ).toMinutes();
        
        if (timeSpan < 5 && events.size() > 5) return "BURST";
        
        return "RANDOM";
    }

    private double calculatePatternConfidence(List<Event> events) {
        if (events.size() < 3) return 0.0;
        
        double timeConsistency = calculateTimeConsistency(events);
        double dataConsistency = calculateDataConsistency(events);
        
        return (timeConsistency + dataConsistency) / 2.0;
    }

    private double calculateTimeConsistency(List<Event> events) {
        if (events.size() < 3) return 0.0;
        
        events.sort(Comparator.comparing(Event::getTimestamp));
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < events.size(); i++) {
            intervals.add(java.time.Duration.between(
                    events.get(i-1).getTimestamp(), 
                    events.get(i).getTimestamp()
            ).toMinutes());
        }
        
        // Calculate coefficient of variation
        double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = intervals.stream()
                .mapToDouble(interval -> Math.pow(interval - mean, 2))
                .average().orElse(0.0);
        
        return mean > 0 ? Math.max(0, 1.0 - (Math.sqrt(variance) / mean)) : 0.0;
    }

    private double calculateDataConsistency(List<Event> events) {
        if (events.size() < 2) return 0.0;
        
        // Calculate similarity between consecutive events
        double totalSimilarity = 0.0;
        for (int i = 1; i < events.size(); i++) {
            totalSimilarity += calculateEventSimilarity(events.get(i-1), events.get(i));
        }
        
        return totalSimilarity / (events.size() - 1);
    }

    private double calculateEventSimilarity(Event event1, Event event2) {
        Set<String> allFields = new HashSet<>(event1.getData().keySet());
        allFields.addAll(event2.getData().keySet());
        
        int matchingFields = 0;
        for (String field : allFields) {
            Object value1 = event1.getData().get(field);
            Object value2 = event2.getData().get(field);
            if (Objects.equals(value1, value2)) {
                matchingFields++;
            }
        }
        
        return (double) matchingFields / allFields.size();
    }

    // Data classes
    public static class Event {
        private String id;
        private String source;
        private String type;
        private Map<String, Object> data;
        private LocalDateTime timestamp;
        private String severity;
        private Map<String, Object> metadata;

        public Event() {
            this.data = new HashMap<>();
            this.metadata = new HashMap<>();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class CorrelationRule {
        private String id;
        private String name;
        private String description;
        private String pattern;
        private Map<String, Object> conditions;
        private String action;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isActive;
        private int priority;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }

        public Map<String, Object> getConditions() { return conditions; }
        public void setConditions(Map<String, Object> conditions) { this.conditions = conditions; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        // Compatibility setter for Lombok-style naming
        public void setIsActive(boolean active) { this.isActive = active; }

        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }

    public static class CorrelationResult {
        private String id;
        private String ruleId;
        private List<Event> events;
        private double correlationScore;
        private LocalDateTime correlatedAt;
        private boolean isSignificant;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }

        public List<Event> getEvents() { return events; }
        public void setEvents(List<Event> events) { this.events = events; }

        public double getCorrelationScore() { return correlationScore; }
        public void setCorrelationScore(double correlationScore) { this.correlationScore = correlationScore; }

        public LocalDateTime getCorrelatedAt() { return correlatedAt; }
        public void setCorrelatedAt(LocalDateTime correlatedAt) { this.correlatedAt = correlatedAt; }

        public boolean isSignificant() { return isSignificant; }
        public void setSignificant(boolean significant) { isSignificant = significant; }
        // Compatibility setter for Lombok-style naming
        public void setIsSignificant(boolean significant) { this.isSignificant = significant; }
    }

    public static class EventPattern {
        private String id;
        private String source;
        private String eventType;
        private List<Event> events;
        private String patternType;
        private double confidence;
        private LocalDateTime detectedAt;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public List<Event> getEvents() { return events; }
        public void setEvents(List<Event> events) { this.events = events; }

        public String getPatternType() { return patternType; }
        public void setPatternType(String patternType) { this.patternType = patternType; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public LocalDateTime getDetectedAt() { return detectedAt; }
        public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }

    public static class CorrelationAnalytics {
        private String source;
        private long totalEvents;
        private long totalCorrelations;
        private double averageCorrelationScore;
        private long patternCount;
        private LocalDateTime lastAnalyzed;

        // Getters and setters
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public long getTotalEvents() { return totalEvents; }
        public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }

        public long getTotalCorrelations() { return totalCorrelations; }
        public void setTotalCorrelations(long totalCorrelations) { this.totalCorrelations = totalCorrelations; }

        public double getAverageCorrelationScore() { return averageCorrelationScore; }
        public void setAverageCorrelationScore(double averageCorrelationScore) { this.averageCorrelationScore = averageCorrelationScore; }

        public long getPatternCount() { return patternCount; }
        public void setPatternCount(long patternCount) { this.patternCount = patternCount; }

        public LocalDateTime getLastAnalyzed() { return lastAnalyzed; }
        public void setLastAnalyzed(LocalDateTime lastAnalyzed) { this.lastAnalyzed = lastAnalyzed; }
    }
}


