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
 * Stream processing service for real-time analysis of incoming data streams
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StreamProcessingService {

    private static final Logger log = LoggerFactory.getLogger(StreamProcessingService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, StreamProcessor> processors = new ConcurrentHashMap<>();
    private final Map<String, List<StreamRule>> rules = new ConcurrentHashMap<>();

    public StreamProcessor createProcessor(String name, String description, String dataSource, 
                                        Map<String, Object> configuration) {
        StreamProcessor processor = new StreamProcessor();
        processor.setId(UUID.randomUUID().toString());
        processor.setName(name);
        processor.setDescription(description);
        processor.setDataSource(dataSource);
        processor.setConfiguration(configuration);
        processor.setCreatedAt(LocalDateTime.now());
        processor.setStatus(ProcessorStatus.ACTIVE);
        processor.setIsRunning(false);
        
        processors.put(processor.getId(), processor);
        
        log.info("Created stream processor: {} for data source: {}", processor.getId(), dataSource);
        return processor;
    }

    public void startProcessor(String processorId) {
        StreamProcessor processor = processors.get(processorId);
        if (processor != null) {
            processor.setIsRunning(true);
            processor.setStartedAt(LocalDateTime.now());
            
            // Start processing in background
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    processStreamData(processor);
                } catch (Exception e) {
                    log.error("Error processing stream data for processor: {}", processorId, e);
                }
            }, 0, 1, TimeUnit.SECONDS);
            
            log.info("Started stream processor: {}", processorId);
        }
    }

    public void stopProcessor(String processorId) {
        StreamProcessor processor = processors.get(processorId);
        if (processor != null) {
            processor.setIsRunning(false);
            processor.setStoppedAt(LocalDateTime.now());
            log.info("Stopped stream processor: {}", processorId);
        }
    }

    public void addStreamRule(String processorId, StreamRule rule) {
        rules.computeIfAbsent(processorId, k -> new ArrayList<>()).add(rule);
        log.info("Added rule to processor: {} - rule: {}", processorId, rule.getName());
    }

    public void removeStreamRule(String processorId, String ruleId) {
        rules.getOrDefault(processorId, new ArrayList<>())
              .removeIf(rule -> rule.getId().equals(ruleId));
        log.info("Removed rule from processor: {} - rule: {}", processorId, ruleId);
    }

    public StreamData processData(String processorId, Map<String, Object> data) {
        StreamProcessor processor = processors.get(processorId);
        if (processor == null || !processor.isRunning()) {
            return null;
        }

        StreamData streamData = new StreamData();
        streamData.setId(UUID.randomUUID().toString());
        streamData.setProcessorId(processorId);
        streamData.setData(data);
        streamData.setTimestamp(LocalDateTime.now());
        streamData.setProcessedAt(LocalDateTime.now());

        // Apply rules
        List<StreamRule> processorRules = rules.getOrDefault(processorId, new ArrayList<>());
        for (StreamRule rule : processorRules) {
            if (evaluateRule(rule, data)) {
                streamData.getTriggers().add(rule.getId());
                executeRuleAction(rule, streamData);
            }
        }

        log.debug("Processed stream data: {} for processor: {}", streamData.getId(), processorId);
        return streamData;
    }

    public StreamMetrics getProcessorMetrics(String processorId) {
        StreamProcessor processor = processors.get(processorId);
        if (processor == null) {
            return null;
        }

        StreamMetrics metrics = new StreamMetrics();
        metrics.setProcessorId(processorId);
        metrics.setTotalProcessed(0);
        metrics.setProcessingRate(0.0);
        metrics.setErrorRate(0.0);
        metrics.setAverageLatency(0.0);
        metrics.setLastProcessed(LocalDateTime.now());
        metrics.setUptime(0);

        return metrics;
    }

    public List<StreamProcessor> getProcessors() {
        return new ArrayList<>(processors.values());
    }

    public StreamProcessor getProcessor(String processorId) {
        return processors.get(processorId);
    }

    public void deleteProcessor(String processorId) {
        stopProcessor(processorId);
        processors.remove(processorId);
        rules.remove(processorId);
        log.info("Deleted stream processor: {}", processorId);
    }

    private void processStreamData(StreamProcessor processor) {
        // Simulate stream processing
        Map<String, Object> sampleData = new HashMap<>();
        sampleData.put("timestamp", LocalDateTime.now().toString());
        sampleData.put("source", processor.getDataSource());
        sampleData.put("value", Math.random() * 100);
        
        processData(processor.getId(), sampleData);
    }

    private boolean evaluateRule(StreamRule rule, Map<String, Object> data) {
        // Simple rule evaluation logic
        switch (rule.getCondition()) {
            case "value_greater_than":
                return evaluateNumericCondition(data, rule.getField(), rule.getValue(), ">");
            case "value_less_than":
                return evaluateNumericCondition(data, rule.getField(), rule.getValue(), "<");
            case "value_equals":
                return evaluateEqualsCondition(data, rule.getField(), rule.getValue());
            case "contains":
                return evaluateContainsCondition(data, rule.getField(), rule.getValue());
            default:
                return false;
        }
    }

    private boolean evaluateNumericCondition(Map<String, Object> data, String field, Object value, String operator) {
        Object fieldValue = data.get(field);
        if (fieldValue instanceof Number && value instanceof Number) {
            double dataVal = ((Number) fieldValue).doubleValue();
            double ruleVal = ((Number) value).doubleValue();
            
            switch (operator) {
                case ">": return dataVal > ruleVal;
                case "<": return dataVal < ruleVal;
                case ">=": return dataVal >= ruleVal;
                case "<=": return dataVal <= ruleVal;
                default: return false;
            }
        }
        return false;
    }

    private boolean evaluateEqualsCondition(Map<String, Object> data, String field, Object value) {
        return Objects.equals(data.get(field), value);
    }

    private boolean evaluateContainsCondition(Map<String, Object> data, String field, Object value) {
        Object fieldValue = data.get(field);
        return fieldValue != null && fieldValue.toString().contains(value.toString());
    }

    private void executeRuleAction(StreamRule rule, StreamData streamData) {
        switch (rule.getAction()) {
            case "alert":
                log.warn("Stream alert triggered: {} - {}", rule.getName(), streamData.getData());
                break;
            case "notify":
                log.info("Stream notification: {} - {}", rule.getName(), streamData.getData());
                break;
            case "log":
                log.info("Stream log: {} - {}", rule.getName(), streamData.getData());
                break;
            case "forward":
                // Forward to another processor or system
                log.info("Forwarding data: {} - {}", rule.getName(), streamData.getData());
                break;
        }
    }

    // Data classes
    public static class StreamProcessor {
        private String id;
        private String name;
        private String description;
        private String dataSource;
        private Map<String, Object> configuration;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime stoppedAt;
        private ProcessorStatus status;
        private boolean isRunning;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

        public LocalDateTime getStoppedAt() { return stoppedAt; }
        public void setStoppedAt(LocalDateTime stoppedAt) { this.stoppedAt = stoppedAt; }

        public ProcessorStatus getStatus() { return status; }
        public void setStatus(ProcessorStatus status) { this.status = status; }

        public boolean isRunning() { return isRunning; }
        public void setIsRunning(boolean isRunning) { this.isRunning = isRunning; }
    }

    public static class StreamRule {
        private String id;
        private String name;
        private String description;
        private String condition;
        private String field;
        private Object value;
        private String action;
        private Map<String, Object> parameters;
        private boolean isActive;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    public static class StreamData {
        private String id;
        private String processorId;
        private Map<String, Object> data;
        private LocalDateTime timestamp;
        private LocalDateTime processedAt;
        private List<String> triggers;

        public StreamData() {
            this.triggers = new ArrayList<>();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getProcessorId() { return processorId; }
        public void setProcessorId(String processorId) { this.processorId = processorId; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

        public List<String> getTriggers() { return triggers; }
        public void setTriggers(List<String> triggers) { this.triggers = triggers; }
    }

    public static class StreamMetrics {
        private String processorId;
        private long totalProcessed;
        private double processingRate;
        private double errorRate;
        private double averageLatency;
        private LocalDateTime lastProcessed;
        private long uptime;

        // Getters and setters
        public String getProcessorId() { return processorId; }
        public void setProcessorId(String processorId) { this.processorId = processorId; }

        public long getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(long totalProcessed) { this.totalProcessed = totalProcessed; }

        public double getProcessingRate() { return processingRate; }
        public void setProcessingRate(double processingRate) { this.processingRate = processingRate; }

        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }

        public double getAverageLatency() { return averageLatency; }
        public void setAverageLatency(double averageLatency) { this.averageLatency = averageLatency; }

        public LocalDateTime getLastProcessed() { return lastProcessed; }
        public void setLastProcessed(LocalDateTime lastProcessed) { this.lastProcessed = lastProcessed; }

        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }
    }

    public enum ProcessorStatus {
        ACTIVE, INACTIVE, ERROR, MAINTENANCE
    }
}


