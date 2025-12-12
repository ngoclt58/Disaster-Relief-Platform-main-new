package com.relief.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAnalyticsService.class);

    private final List<Metric> metrics = new ArrayList<>();

    public void track(String name, double value, Map<String, String> labels) {
        Metric m = new Metric();
        m.setId(UUID.randomUUID().toString());
        m.setName(name);
        m.setValue(value);
        m.setLabels(labels != null ? labels : new HashMap<>());
        m.setTimestamp(LocalDateTime.now());
        metrics.add(m);
    }

    public List<Metric> recent(int limit) {
        return metrics.stream()
                .sorted(Comparator.comparing(Metric::getTimestamp).reversed())
                .limit(limit)
                .toList();
    }

    public Map<String, Double> aggregate(String name) {
        Map<String, Double> agg = new HashMap<>();
        for (Metric m : metrics) {
            if (!Objects.equals(m.getName(), name)) continue;
            String key = m.getLabels().getOrDefault("key", "total");
            agg.put(key, agg.getOrDefault(key, 0.0) + m.getValue());
        }
        return agg;
    }

    public static class Metric {
        private String id;
        private String name;
        private double value;
        private Map<String, String> labels;
        private LocalDateTime timestamp;

        // Explicit getters and setters for Lombok compatibility
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public Map<String, String> getLabels() { return labels; }
        public void setLabels(Map<String, String> labels) { this.labels = labels; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}




