package com.relief.service.realtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Trend analysis service to identify emerging patterns and trends in real-time data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrendAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(TrendAnalysisService.class);

    private final Map<String, TrendAnalyzer> analyzers = new ConcurrentHashMap<>();
    private final Map<String, List<DataPoint>> dataPoints = new ConcurrentHashMap<>();
    private final Map<String, TrendResult> trends = new ConcurrentHashMap<>();

    public TrendAnalyzer createAnalyzer(String name, String description, String dataSource, 
                                      String metric, Map<String, Object> configuration) {
        TrendAnalyzer analyzer = new TrendAnalyzer();
        analyzer.setId(UUID.randomUUID().toString());
        analyzer.setName(name);
        analyzer.setDescription(description);
        analyzer.setDataSource(dataSource);
        analyzer.setMetric(metric);
        analyzer.setConfiguration(configuration);
        analyzer.setCreatedAt(LocalDateTime.now());
        analyzer.setIsActive(true);
        analyzer.setWindowSize(60); // 60 minutes default window
        
        analyzers.put(analyzer.getId(), analyzer);
        
        log.info("Created trend analyzer: {} for metric: {}", analyzer.getId(), metric);
        return analyzer;
    }

    public void addDataPoint(String analyzerId, DataPoint dataPoint) {
        TrendAnalyzer analyzer = analyzers.get(analyzerId);
        if (analyzer == null || !analyzer.isActive()) {
            return;
        }

        dataPoints.computeIfAbsent(analyzerId, k -> new ArrayList<>()).add(dataPoint);
        
        // Keep only recent data points (within window size)
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(analyzer.getWindowSize());
        dataPoints.get(analyzerId).removeIf(dp -> dp.getTimestamp().isBefore(cutoff));
        
        // Analyze trend
        analyzeTrend(analyzerId);
        
        log.debug("Added data point to analyzer: {} - value: {}", analyzerId, dataPoint.getValue());
    }

    public TrendResult analyzeTrend(String analyzerId) {
        TrendAnalyzer analyzer = analyzers.get(analyzerId);
        if (analyzer == null) {
            return null;
        }

        List<DataPoint> points = dataPoints.getOrDefault(analyzerId, new ArrayList<>());
        if (points.size() < 3) {
            return null;
        }

        TrendResult result = new TrendResult();
        result.setId(UUID.randomUUID().toString());
        result.setAnalyzerId(analyzerId);
        result.setAnalyzedAt(LocalDateTime.now());
        result.setDataPoints(points);
        result.setTrendDirection(calculateTrendDirection(points));
        result.setTrendStrength(calculateTrendStrength(points));
        result.setConfidence(calculateConfidence(points));
        result.setSlope(calculateSlope(points));
        result.setR2(calculateRSquared(points));
        result.setIsSignificant(result.getConfidence() > 0.7);

        // Detect trend type
        result.setTrendType(detectTrendType(points));
        
        // Generate forecast
        result.setForecast(generateForecast(points, 5));
        
        // Detect anomalies
        result.setAnomalies(detectAnomalies(points));

        trends.put(result.getId(), result);
        
        log.info("Analyzed trend for analyzer: {} - direction: {}, strength: {}", 
                analyzerId, result.getTrendDirection(), result.getTrendStrength());
        return result;
    }

    public List<TrendResult> getTrends(String analyzerId, LocalDateTime startTime, LocalDateTime endTime) {
        return trends.values().stream()
                .filter(result -> result.getAnalyzerId().equals(analyzerId))
                .filter(result -> result.getAnalyzedAt().isAfter(startTime) && 
                                result.getAnalyzedAt().isBefore(endTime))
                .sorted((a, b) -> b.getAnalyzedAt().compareTo(a.getAnalyzedAt()))
                .collect(Collectors.toList());
    }

    public TrendAlert createAlert(String analyzerId, String name, String condition, 
                                String threshold, String action) {
        TrendAlert alert = new TrendAlert();
        alert.setId(UUID.randomUUID().toString());
        alert.setAnalyzerId(analyzerId);
        alert.setName(name);
        alert.setCondition(condition);
        alert.setThreshold(threshold);
        alert.setAction(action);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setIsActive(true);
        alert.setTriggerCount(0);
        
        log.info("Created trend alert: {} for analyzer: {}", alert.getId(), analyzerId);
        return alert;
    }

    public List<TrendAlert> checkAlerts(String analyzerId) {
        TrendAnalyzer analyzer = analyzers.get(analyzerId);
        if (analyzer == null) {
            return new ArrayList<>();
        }

        List<TrendAlert> triggeredAlerts = new ArrayList<>();
        TrendResult latestTrend = getLatestTrend(analyzerId);
        
        if (latestTrend != null) {
            // Check for alert conditions
            if (latestTrend.getTrendStrength() > 0.8) {
                TrendAlert alert = new TrendAlert();
                alert.setId(UUID.randomUUID().toString());
                alert.setAnalyzerId(analyzerId);
                alert.setName("High Trend Strength Alert");
                alert.setCondition("trend_strength");
                alert.setThreshold("0.8");
                alert.setAction("notify");
                alert.setTriggeredAt(LocalDateTime.now());
                alert.setTriggerCount(1);
                triggeredAlerts.add(alert);
            }
            
            if (latestTrend.getConfidence() < 0.5) {
                TrendAlert alert = new TrendAlert();
                alert.setId(UUID.randomUUID().toString());
                alert.setAnalyzerId(analyzerId);
                alert.setName("Low Confidence Alert");
                alert.setCondition("confidence");
                alert.setThreshold("0.5");
                alert.setAction("warn");
                alert.setTriggeredAt(LocalDateTime.now());
                alert.setTriggerCount(1);
                triggeredAlerts.add(alert);
            }
        }
        
        return triggeredAlerts;
    }

    public TrendAnalytics getAnalytics(String analyzerId) {
        TrendAnalyzer analyzer = analyzers.get(analyzerId);
        if (analyzer == null) {
            return null;
        }

        TrendAnalytics analytics = new TrendAnalytics();
        analytics.setAnalyzerId(analyzerId);
        analytics.setTotalDataPoints(dataPoints.getOrDefault(analyzerId, new ArrayList<>()).size());
        analytics.setTotalTrends(trends.values().stream()
                .filter(result -> result.getAnalyzerId().equals(analyzerId))
                .count());
        analytics.setAverageConfidence(trends.values().stream()
                .filter(result -> result.getAnalyzerId().equals(analyzerId))
                .mapToDouble(TrendResult::getConfidence)
                .average()
                .orElse(0.0));
        analytics.setLastAnalyzed(LocalDateTime.now());

        return analytics;
    }

    public TrendAnalyzer getAnalyzer(String analyzerId) {
        return analyzers.get(analyzerId);
    }

    public List<TrendAnalyzer> getAnalyzers() {
        return new ArrayList<>(analyzers.values());
    }

    public void updateAnalyzer(String analyzerId, String name, String description, 
                              Map<String, Object> configuration) {
        TrendAnalyzer analyzer = analyzers.get(analyzerId);
        if (analyzer != null) {
            analyzer.setName(name);
            analyzer.setDescription(description);
            analyzer.setConfiguration(configuration);
            analyzer.setUpdatedAt(LocalDateTime.now());
            log.info("Updated trend analyzer: {}", analyzerId);
        }
    }

    public void deleteAnalyzer(String analyzerId) {
        analyzers.remove(analyzerId);
        dataPoints.remove(analyzerId);
        trends.entrySet().removeIf(entry -> entry.getValue().getAnalyzerId().equals(analyzerId));
        log.info("Deleted trend analyzer: {}", analyzerId);
    }

    private String calculateTrendDirection(List<DataPoint> points) {
        if (points.size() < 2) return "INSUFFICIENT_DATA";
        
        points.sort(Comparator.comparing(DataPoint::getTimestamp));
        double firstValue = points.get(0).getValue();
        double lastValue = points.get(points.size() - 1).getValue();
        
        double change = lastValue - firstValue;
        double changePercent = (change / Math.abs(firstValue)) * 100;
        
        if (changePercent > 5) return "INCREASING";
        if (changePercent < -5) return "DECREASING";
        return "STABLE";
    }

    private double calculateTrendStrength(List<DataPoint> points) {
        if (points.size() < 3) return 0.0;
        
        // Calculate linear regression slope
        double slope = calculateSlope(points);
        double r2 = calculateRSquared(points);
        
        // Combine slope magnitude with R-squared for strength
        return Math.min(1.0, Math.abs(slope) * r2);
    }

    private double calculateConfidence(List<DataPoint> points) {
        if (points.size() < 3) return 0.0;
        
        double r2 = calculateRSquared(points);
        double dataConsistency = calculateDataConsistency(points);
        
        return (r2 + dataConsistency) / 2.0;
    }

    private double calculateSlope(List<DataPoint> points) {
        if (points.size() < 2) return 0.0;
        
        points.sort(Comparator.comparing(DataPoint::getTimestamp));
        
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            double x = i; // Use index as x value
            double y = points.get(i).getValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }

    private double calculateRSquared(List<DataPoint> points) {
        if (points.size() < 3) return 0.0;
        
        points.sort(Comparator.comparing(DataPoint::getTimestamp));
        
        double slope = calculateSlope(points);
        double intercept = calculateIntercept(points, slope);
        
        double sumSquaredResiduals = 0;
        double sumSquaredTotal = 0;
        double meanY = points.stream().mapToDouble(DataPoint::getValue).average().orElse(0.0);
        
        for (int i = 0; i < points.size(); i++) {
            double x = i;
            double y = points.get(i).getValue();
            double predictedY = slope * x + intercept;
            
            sumSquaredResiduals += Math.pow(y - predictedY, 2);
            sumSquaredTotal += Math.pow(y - meanY, 2);
        }
        
        return 1 - (sumSquaredResiduals / sumSquaredTotal);
    }

    private double calculateIntercept(List<DataPoint> points, double slope) {
        double meanY = points.stream().mapToDouble(DataPoint::getValue).average().orElse(0.0);
        double meanX = (points.size() - 1) / 2.0; // Mean of indices
        return meanY - slope * meanX;
    }

    private double calculateDataConsistency(List<DataPoint> points) {
        if (points.size() < 2) return 0.0;
        
        double mean = points.stream().mapToDouble(DataPoint::getValue).average().orElse(0.0);
        double variance = points.stream()
                .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
                .average().orElse(0.0);
        
        double standardDeviation = Math.sqrt(variance);
        double coefficientOfVariation = standardDeviation / Math.abs(mean);
        
        return Math.max(0, 1.0 - coefficientOfVariation);
    }

    private String detectTrendType(List<DataPoint> points) {
        if (points.size() < 5) return "INSUFFICIENT_DATA";
        
        String direction = calculateTrendDirection(points);
        double strength = calculateTrendStrength(points);
        
        if (strength < 0.3) return "RANDOM";
        if (strength < 0.6) return "WEAK_" + direction;
        if (strength < 0.8) return "MODERATE_" + direction;
        return "STRONG_" + direction;
    }

    private List<DataPoint> generateForecast(List<DataPoint> points, int steps) {
        if (points.size() < 3) return new ArrayList<>();
        
        points.sort(Comparator.comparing(DataPoint::getTimestamp));
        double slope = calculateSlope(points);
        double intercept = calculateIntercept(points, slope);
        
        List<DataPoint> forecast = new ArrayList<>();
        LocalDateTime lastTime = points.get(points.size() - 1).getTimestamp();
        
        for (int i = 1; i <= steps; i++) {
            double x = points.size() + i - 1;
            double predictedValue = slope * x + intercept;
            
            DataPoint forecastPoint = new DataPoint();
            forecastPoint.setId(UUID.randomUUID().toString());
            forecastPoint.setValue(predictedValue);
            forecastPoint.setTimestamp(lastTime.plusMinutes(i * 5)); // 5-minute intervals
            forecastPoint.setIsForecast(true);
            
            forecast.add(forecastPoint);
        }
        
        return forecast;
    }

    private List<DataPoint> detectAnomalies(List<DataPoint> points) {
        if (points.size() < 5) return new ArrayList<>();
        
        List<DataPoint> anomalies = new ArrayList<>();
        double mean = points.stream().mapToDouble(DataPoint::getValue).average().orElse(0.0);
        double standardDeviation = Math.sqrt(points.stream()
                .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
                .average().orElse(0.0));
        
        double threshold = 2 * standardDeviation; // 2-sigma rule
        
        for (DataPoint point : points) {
            if (Math.abs(point.getValue() - mean) > threshold) {
                point.setIsAnomaly(true);
                anomalies.add(point);
            }
        }
        
        return anomalies;
    }

    private TrendResult getLatestTrend(String analyzerId) {
        return trends.values().stream()
                .filter(result -> result.getAnalyzerId().equals(analyzerId))
                .max(Comparator.comparing(TrendResult::getAnalyzedAt))
                .orElse(null);
    }

    // Data classes
    public static class TrendAnalyzer {
        private String id;
        private String name;
        private String description;
        private String dataSource;
        private String metric;
        private Map<String, Object> configuration;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isActive;
        private int windowSize;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }

        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        // Compatibility setter for Lombok-style naming
        public void setIsActive(boolean active) { this.isActive = active; }

        public int getWindowSize() { return windowSize; }
        public void setWindowSize(int windowSize) { this.windowSize = windowSize; }
    }

    public static class DataPoint {
        private String id;
        private double value;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata;
        private boolean isForecast;
        private boolean isAnomaly;

        public DataPoint() {
            this.metadata = new HashMap<>();
            this.isForecast = false;
            this.isAnomaly = false;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

        public boolean isForecast() { return isForecast; }
        public void setForecast(boolean forecast) { isForecast = forecast; }
        // Compatibility setter for Lombok-style naming
        public void setIsForecast(boolean forecast) { this.isForecast = forecast; }

        public boolean isAnomaly() { return isAnomaly; }
        public void setAnomaly(boolean anomaly) { isAnomaly = anomaly; }
        // Compatibility setter for Lombok-style naming
        public void setIsAnomaly(boolean anomaly) { this.isAnomaly = anomaly; }
    }

    public static class TrendResult {
        private String id;
        private String analyzerId;
        private LocalDateTime analyzedAt;
        private List<DataPoint> dataPoints;
        private String trendDirection;
        private double trendStrength;
        private double confidence;
        private double slope;
        private double r2;
        private boolean isSignificant;
        private String trendType;
        private List<DataPoint> forecast;
        private List<DataPoint> anomalies;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getAnalyzerId() { return analyzerId; }
        public void setAnalyzerId(String analyzerId) { this.analyzerId = analyzerId; }

        public LocalDateTime getAnalyzedAt() { return analyzedAt; }
        public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }

        public List<DataPoint> getDataPoints() { return dataPoints; }
        public void setDataPoints(List<DataPoint> dataPoints) { this.dataPoints = dataPoints; }

        public String getTrendDirection() { return trendDirection; }
        public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

        public double getTrendStrength() { return trendStrength; }
        public void setTrendStrength(double trendStrength) { this.trendStrength = trendStrength; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public double getSlope() { return slope; }
        public void setSlope(double slope) { this.slope = slope; }

        public double getR2() { return r2; }
        public void setR2(double r2) { this.r2 = r2; }

        public boolean isSignificant() { return isSignificant; }
        public void setSignificant(boolean significant) { isSignificant = significant; }
        // Compatibility setter for Lombok-style naming
        public void setIsSignificant(boolean significant) { this.isSignificant = significant; }

        public String getTrendType() { return trendType; }
        public void setTrendType(String trendType) { this.trendType = trendType; }

        public List<DataPoint> getForecast() { return forecast; }
        public void setForecast(List<DataPoint> forecast) { this.forecast = forecast; }

        public List<DataPoint> getAnomalies() { return anomalies; }
        public void setAnomalies(List<DataPoint> anomalies) { this.anomalies = anomalies; }
    }

    public static class TrendAlert {
        private String id;
        private String analyzerId;
        private String name;
        private String condition;
        private String threshold;
        private String action;
        private LocalDateTime createdAt;
        private LocalDateTime triggeredAt;
        private boolean isActive;
        private int triggerCount;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getAnalyzerId() { return analyzerId; }
        public void setAnalyzerId(String analyzerId) { this.analyzerId = analyzerId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getThreshold() { return threshold; }
        public void setThreshold(String threshold) { this.threshold = threshold; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getTriggeredAt() { return triggeredAt; }
        public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        // Compatibility setter for Lombok-style naming
        public void setIsActive(boolean active) { this.isActive = active; }

        public int getTriggerCount() { return triggerCount; }
        public void setTriggerCount(int triggerCount) { this.triggerCount = triggerCount; }
    }

    public static class TrendAnalytics {
        private String analyzerId;
        private long totalDataPoints;
        private long totalTrends;
        private double averageConfidence;
        private LocalDateTime lastAnalyzed;

        // Getters and setters
        public String getAnalyzerId() { return analyzerId; }
        public void setAnalyzerId(String analyzerId) { this.analyzerId = analyzerId; }

        public long getTotalDataPoints() { return totalDataPoints; }
        public void setTotalDataPoints(long totalDataPoints) { this.totalDataPoints = totalDataPoints; }

        public long getTotalTrends() { return totalTrends; }
        public void setTotalTrends(long totalTrends) { this.totalTrends = totalTrends; }

        public double getAverageConfidence() { return averageConfidence; }
        public void setAverageConfidence(double averageConfidence) { this.averageConfidence = averageConfidence; }

        public LocalDateTime getLastAnalyzed() { return lastAnalyzed; }
        public void setLastAnalyzed(LocalDateTime lastAnalyzed) { this.lastAnalyzed = lastAnalyzed; }
    }
}


