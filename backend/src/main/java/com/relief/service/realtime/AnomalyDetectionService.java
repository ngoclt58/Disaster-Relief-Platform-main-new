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
 * Anomaly detection service for automatic detection of unusual patterns or behaviors
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);

    private final Map<String, AnomalyDetector> detectors = new ConcurrentHashMap<>();
    private final Map<String, List<Anomaly>> anomalies = new ConcurrentHashMap<>();
    private final Map<String, DetectionModel> models = new ConcurrentHashMap<>();

    public AnomalyDetector createDetector(String name, String description, String dataSource, 
                                        String detectionType, Map<String, Object> configuration) {
        AnomalyDetector detector = new AnomalyDetector();
        detector.setId(UUID.randomUUID().toString());
        detector.setName(name);
        detector.setDescription(description);
        detector.setDataSource(dataSource);
        detector.setDetectionType(detectionType);
        detector.setConfiguration(configuration);
        detector.setCreatedAt(LocalDateTime.now());
        detector.setIsActive(true);
        detector.setSensitivity(0.5); // Default sensitivity
        
        detectors.put(detector.getId(), detector);
        
        log.info("Created anomaly detector: {} for data source: {}", detector.getId(), dataSource);
        return detector;
    }

    public DetectionModel trainModel(String detectorId, List<DataPoint> trainingData, 
                                   Map<String, Object> parameters) {
        AnomalyDetector detector = detectors.get(detectorId);
        if (detector == null) {
            return null;
        }

        DetectionModel model = new DetectionModel();
        model.setId(UUID.randomUUID().toString());
        model.setDetectorId(detectorId);
        model.setModelType(detector.getDetectionType());
        model.setParameters(parameters);
        model.setTrainingData(trainingData);
        model.setTrainedAt(LocalDateTime.now());
        model.setAccuracy(calculateModelAccuracy(trainingData, detector.getDetectionType()));
        model.setIsReady(true);

        models.put(model.getId(), model);
        detector.setModelId(model.getId());
        
        log.info("Trained anomaly detection model: {} with accuracy: {}", 
                model.getId(), model.getAccuracy());
        return model;
    }

    public Anomaly detectAnomaly(String detectorId, DataPoint dataPoint) {
        AnomalyDetector detector = detectors.get(detectorId);
        if (detector == null || !detector.isActive()) {
            return null;
        }

        DetectionModel model = models.get(detector.getModelId());
        if (model == null || !model.isReady()) {
            return null;
        }

        double anomalyScore = calculateAnomalyScore(dataPoint, model, detector);
        boolean isAnomaly = anomalyScore > detector.getSensitivity();

        if (isAnomaly) {
            Anomaly anomaly = new Anomaly();
            anomaly.setId(UUID.randomUUID().toString());
            anomaly.setDetectorId(detectorId);
            anomaly.setDataPoint(dataPoint);
            anomaly.setAnomalyScore(anomalyScore);
            anomaly.setSeverity(calculateSeverity(anomalyScore));
            anomaly.setDetectedAt(LocalDateTime.now());
            anomaly.setDescription(generateDescription(anomaly, detector));
            anomaly.setIsResolved(false);

            anomalies.computeIfAbsent(detectorId, k -> new ArrayList<>()).add(anomaly);
            
            log.warn("Detected anomaly: {} with score: {} and severity: {}", 
                    anomaly.getId(), anomalyScore, anomaly.getSeverity());
            return anomaly;
        }

        return null;
    }

    public List<Anomaly> getAnomalies(String detectorId, LocalDateTime startTime, LocalDateTime endTime) {
        return anomalies.getOrDefault(detectorId, new ArrayList<>()).stream()
                .filter(anomaly -> anomaly.getDetectedAt().isAfter(startTime) && 
                                 anomaly.getDetectedAt().isBefore(endTime))
                .sorted((a, b) -> b.getDetectedAt().compareTo(a.getDetectedAt()))
                .collect(Collectors.toList());
    }

    public AnomalySummary getAnomalySummary(String detectorId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Anomaly> detectorAnomalies = getAnomalies(detectorId, startTime, endTime);
        
        AnomalySummary summary = new AnomalySummary();
        summary.setDetectorId(detectorId);
        summary.setTotalAnomalies(detectorAnomalies.size());
        summary.setHighSeverityCount((int) detectorAnomalies.stream()
                .filter(a -> "HIGH".equals(a.getSeverity()))
                .count());
        summary.setMediumSeverityCount((int) detectorAnomalies.stream()
                .filter(a -> "MEDIUM".equals(a.getSeverity()))
                .count());
        summary.setLowSeverityCount((int) detectorAnomalies.stream()
                .filter(a -> "LOW".equals(a.getSeverity()))
                .count());
        summary.setResolvedCount((int) detectorAnomalies.stream()
                .filter(Anomaly::isResolved)
                .count());
        summary.setAverageScore(detectorAnomalies.stream()
                .mapToDouble(Anomaly::getAnomalyScore)
                .average()
                .orElse(0.0));
        summary.setLastDetected(detectorAnomalies.stream()
                .map(Anomaly::getDetectedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null));

        return summary;
    }

    public void resolveAnomaly(String anomalyId, String resolution, String resolvedBy) {
        anomalies.values().stream()
                .flatMap(List::stream)
                .filter(anomaly -> anomaly.getId().equals(anomalyId))
                .findFirst()
                .ifPresent(anomaly -> {
                    anomaly.setIsResolved(true);
                    anomaly.setResolution(resolution);
                    anomaly.setResolvedBy(resolvedBy);
                    anomaly.setResolvedAt(LocalDateTime.now());
                    log.info("Resolved anomaly: {} by: {}", anomalyId, resolvedBy);
                });
    }

    public AnomalyPattern detectPattern(String detectorId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Anomaly> detectorAnomalies = getAnomalies(detectorId, startTime, endTime);
        if (detectorAnomalies.size() < 3) {
            return null;
        }

        AnomalyPattern pattern = new AnomalyPattern();
        pattern.setId(UUID.randomUUID().toString());
        pattern.setDetectorId(detectorId);
        pattern.setAnomalies(detectorAnomalies);
        pattern.setPatternType(analyzePatternType(detectorAnomalies));
        pattern.setConfidence(calculatePatternConfidence(detectorAnomalies));
        pattern.setDetectedAt(LocalDateTime.now());
        pattern.setStartTime(startTime);
        pattern.setEndTime(endTime);

        log.info("Detected anomaly pattern: {} with confidence: {}", 
                pattern.getPatternType(), pattern.getConfidence());
        return pattern;
    }

    public DetectionAnalytics getAnalytics(String detectorId) {
        AnomalyDetector detector = detectors.get(detectorId);
        if (detector == null) {
            return null;
        }

        DetectionAnalytics analytics = new DetectionAnalytics();
        analytics.setDetectorId(detectorId);
        analytics.setTotalAnomalies(anomalies.getOrDefault(detectorId, new ArrayList<>()).size());
        analytics.setDetectionRate(0.0);
        analytics.setFalsePositiveRate(0.0);
        analytics.setAverageScore(anomalies.getOrDefault(detectorId, new ArrayList<>()).stream()
                .mapToDouble(Anomaly::getAnomalyScore)
                .average()
                .orElse(0.0));
        analytics.setLastDetected(LocalDateTime.now());

        return analytics;
    }

    public AnomalyDetector getDetector(String detectorId) {
        return detectors.get(detectorId);
    }

    public List<AnomalyDetector> getDetectors() {
        return new ArrayList<>(detectors.values());
    }

    public void updateDetector(String detectorId, String name, String description, 
                              Map<String, Object> configuration, double sensitivity) {
        AnomalyDetector detector = detectors.get(detectorId);
        if (detector != null) {
            detector.setName(name);
            detector.setDescription(description);
            detector.setConfiguration(configuration);
            detector.setSensitivity(sensitivity);
            detector.setUpdatedAt(LocalDateTime.now());
            log.info("Updated anomaly detector: {}", detectorId);
        }
    }

    public void deleteDetector(String detectorId) {
        detectors.remove(detectorId);
        anomalies.remove(detectorId);
        models.entrySet().removeIf(entry -> entry.getValue().getDetectorId().equals(detectorId));
        log.info("Deleted anomaly detector: {}", detectorId);
    }

    private double calculateAnomalyScore(DataPoint dataPoint, DetectionModel model, AnomalyDetector detector) {
        switch (detector.getDetectionType()) {
            case "STATISTICAL":
                return calculateStatisticalScore(dataPoint, model);
            case "ISOLATION_FOREST":
                return calculateIsolationForestScore(dataPoint, model);
            case "ONE_CLASS_SVM":
                return calculateOneClassSVMScore(dataPoint, model);
            case "DENSITY_BASED":
                return calculateDensityBasedScore(dataPoint, model);
            default:
                return calculateStatisticalScore(dataPoint, model);
        }
    }

    private double calculateStatisticalScore(DataPoint dataPoint, DetectionModel model) {
        List<DataPoint> trainingData = model.getTrainingData();
        if (trainingData.isEmpty()) return 0.0;

        double mean = trainingData.stream().mapToDouble(DataPoint::getValue).average().orElse(0.0);
        double variance = trainingData.stream()
                .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
                .average().orElse(0.0);
        double standardDeviation = Math.sqrt(variance);

        if (standardDeviation == 0) return 0.0;

        double zScore = Math.abs(dataPoint.getValue() - mean) / standardDeviation;
        return Math.min(1.0, zScore / 3.0); // Normalize to 0-1 range
    }

    private double calculateIsolationForestScore(DataPoint dataPoint, DetectionModel model) {
        // Simplified isolation forest implementation
        List<DataPoint> trainingData = model.getTrainingData();
        if (trainingData.isEmpty()) return 0.0;

        int isolationCount = 0;
        int totalTrees = 10; // Number of isolation trees

        for (int i = 0; i < totalTrees; i++) {
            if (isIsolated(dataPoint, trainingData, 0, 10)) {
                isolationCount++;
            }
        }

        return (double) isolationCount / totalTrees;
    }

    private boolean isIsolated(DataPoint dataPoint, List<DataPoint> data, int depth, int maxDepth) {
        if (depth >= maxDepth || data.size() <= 1) {
            return true;
        }

        // Random split
        double minValue = data.stream().mapToDouble(DataPoint::getValue).min().orElse(0.0);
        double maxValue = data.stream().mapToDouble(DataPoint::getValue).max().orElse(0.0);
        double splitValue = minValue + Math.random() * (maxValue - minValue);

        List<DataPoint> left = data.stream()
                .filter(p -> p.getValue() < splitValue)
                .collect(Collectors.toList());
        List<DataPoint> right = data.stream()
                .filter(p -> p.getValue() >= splitValue)
                .collect(Collectors.toList());

        if (left.isEmpty() || right.isEmpty()) {
            return true;
        }

        if (dataPoint.getValue() < splitValue) {
            return isIsolated(dataPoint, left, depth + 1, maxDepth);
        } else {
            return isIsolated(dataPoint, right, depth + 1, maxDepth);
        }
    }

    private double calculateOneClassSVMScore(DataPoint dataPoint, DetectionModel model) {
        // Simplified one-class SVM implementation
        List<DataPoint> trainingData = model.getTrainingData();
        if (trainingData.isEmpty()) return 0.0;

        double mean = trainingData.stream().mapToDouble(DataPoint::getValue).average().orElse(0.0);
        double distance = Math.abs(dataPoint.getValue() - mean);
        double maxDistance = trainingData.stream()
                .mapToDouble(p -> Math.abs(p.getValue() - mean))
                .max().orElse(1.0);

        return Math.min(1.0, distance / maxDistance);
    }

    private double calculateDensityBasedScore(DataPoint dataPoint, DetectionModel model) {
        List<DataPoint> trainingData = model.getTrainingData();
        if (trainingData.isEmpty()) return 0.0;

        int k = Math.min(5, trainingData.size());
        double[] distances = trainingData.stream()
                .mapToDouble(p -> Math.abs(p.getValue() - dataPoint.getValue()))
                .sorted()
                .toArray();

        double kthDistance = distances[Math.min(k - 1, distances.length - 1)];
        double density = 1.0 / (kthDistance + 1e-6); // Add small epsilon to avoid division by zero

        // Normalize density score
        double maxDensity = trainingData.stream()
                .mapToDouble(p -> {
                    double[] pDistances = trainingData.stream()
                            .mapToDouble(other -> Math.abs(other.getValue() - p.getValue()))
                            .sorted()
                            .toArray();
                    double pKthDistance = pDistances[Math.min(k - 1, pDistances.length - 1)];
                    return 1.0 / (pKthDistance + 1e-6);
                })
                .max().orElse(1.0);

        return 1.0 - (density / maxDensity);
    }

    private double calculateModelAccuracy(List<DataPoint> trainingData, String detectionType) {
        // Simplified accuracy calculation
        if (trainingData.size() < 10) return 0.5;

        // Cross-validation simulation
        int correctPredictions = 0;
        int totalPredictions = 0;

        for (int i = 0; i < trainingData.size(); i++) {
            DataPoint testPoint = trainingData.get(i);
            List<DataPoint> trainSet = new ArrayList<>(trainingData);
            trainSet.remove(i);

            DetectionModel tempModel = new DetectionModel();
            tempModel.setTrainingData(trainSet);
            tempModel.setModelType(detectionType);

            double score = calculateAnomalyScore(testPoint, tempModel, 
                    new AnomalyDetector() {{
                        setDetectionType(detectionType);
                        setSensitivity(0.5);
                    }});

            // Assume ground truth: points at extremes are anomalies
            double mean = trainSet.stream().mapToDouble(DataPoint::getValue).average().orElse(0.0);
            double std = Math.sqrt(trainSet.stream()
                    .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
                    .average().orElse(0.0));
            boolean isActualAnomaly = Math.abs(testPoint.getValue() - mean) > 2 * std;
            boolean isPredictedAnomaly = score > 0.5;

            if (isActualAnomaly == isPredictedAnomaly) {
                correctPredictions++;
            }
            totalPredictions++;
        }

        return totalPredictions > 0 ? (double) correctPredictions / totalPredictions : 0.5;
    }

    private String calculateSeverity(double anomalyScore) {
        if (anomalyScore >= 0.8) return "HIGH";
        if (anomalyScore >= 0.6) return "MEDIUM";
        return "LOW";
    }

    private String generateDescription(Anomaly anomaly, AnomalyDetector detector) {
        return String.format("Anomaly detected in %s: value %.2f with score %.2f", 
                detector.getDataSource(), 
                anomaly.getDataPoint().getValue(), 
                anomaly.getAnomalyScore());
    }

    private String analyzePatternType(List<Anomaly> anomalies) {
        if (anomalies.size() < 3) return "INSUFFICIENT_DATA";

        // Check for temporal patterns
        anomalies.sort(Comparator.comparing(Anomaly::getDetectedAt));
        
        // Check for burst pattern
        long timeSpan = java.time.Duration.between(
                anomalies.get(0).getDetectedAt(),
                anomalies.get(anomalies.size() - 1).getDetectedAt()
        ).toMinutes();

        if (timeSpan < 10 && anomalies.size() > 5) {
            return "BURST";
        }

        // Check for sequential pattern
        boolean isSequential = true;
        for (int i = 1; i < anomalies.size(); i++) {
            if (anomalies.get(i).getDetectedAt().isBefore(anomalies.get(i-1).getDetectedAt())) {
                isSequential = false;
                break;
            }
        }

        if (isSequential) return "SEQUENTIAL";

        return "RANDOM";
    }

    private double calculatePatternConfidence(List<Anomaly> anomalies) {
        if (anomalies.size() < 3) return 0.0;

        // Calculate confidence based on temporal consistency and severity distribution
        double temporalConsistency = calculateTemporalConsistency(anomalies);
        double severityConsistency = calculateSeverityConsistency(anomalies);

        return (temporalConsistency + severityConsistency) / 2.0;
    }

    private double calculateTemporalConsistency(List<Anomaly> anomalies) {
        if (anomalies.size() < 2) return 0.0;

        anomalies.sort(Comparator.comparing(Anomaly::getDetectedAt));
        List<Long> intervals = new ArrayList<>();
        
        for (int i = 1; i < anomalies.size(); i++) {
            intervals.add(java.time.Duration.between(
                    anomalies.get(i-1).getDetectedAt(),
                    anomalies.get(i).getDetectedAt()
            ).toMinutes());
        }

        double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = intervals.stream()
                .mapToDouble(interval -> Math.pow(interval - mean, 2))
                .average().orElse(0.0);

        return mean > 0 ? Math.max(0, 1.0 - (Math.sqrt(variance) / mean)) : 0.0;
    }

    private double calculateSeverityConsistency(List<Anomaly> anomalies) {
        Map<String, Long> severityCounts = anomalies.stream()
                .collect(Collectors.groupingBy(Anomaly::getSeverity, Collectors.counting()));

        long maxCount = severityCounts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return (double) maxCount / anomalies.size();
    }

    // Data classes
    public static class AnomalyDetector {
        private String id;
        private String name;
        private String description;
        private String dataSource;
        private String detectionType;
        private Map<String, Object> configuration;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isActive;
        private double sensitivity;
        private String modelId;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public String getDetectionType() { return detectionType; }
        public void setDetectionType(String detectionType) { this.detectionType = detectionType; }

        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        /**
         * Backwards-compatible alias used by older code paths.
         */
        public void setIsActive(boolean isActive) { this.isActive = isActive; }

        public double getSensitivity() { return sensitivity; }
        public void setSensitivity(double sensitivity) { this.sensitivity = sensitivity; }

        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
    }

    public static class DetectionModel {
        private String id;
        private String detectorId;
        private String modelType;
        private Map<String, Object> parameters;
        private List<DataPoint> trainingData;
        private LocalDateTime trainedAt;
        private double accuracy;
        private boolean isReady;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDetectorId() { return detectorId; }
        public void setDetectorId(String detectorId) { this.detectorId = detectorId; }

        public String getModelType() { return modelType; }
        public void setModelType(String modelType) { this.modelType = modelType; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public List<DataPoint> getTrainingData() { return trainingData; }
        public void setTrainingData(List<DataPoint> trainingData) { this.trainingData = trainingData; }

        public LocalDateTime getTrainedAt() { return trainedAt; }
        public void setTrainedAt(LocalDateTime trainedAt) { this.trainedAt = trainedAt; }

        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

        public boolean isReady() { return isReady; }
        public void setReady(boolean ready) { isReady = ready; }

        /**
         * Backwards-compatible alias used by older code paths.
         */
        public void setIsReady(boolean isReady) { this.isReady = isReady; }
    }

    public static class Anomaly {
        private String id;
        private String detectorId;
        private DataPoint dataPoint;
        private double anomalyScore;
        private String severity;
        private LocalDateTime detectedAt;
        private String description;
        private boolean isResolved;
        private String resolution;
        private String resolvedBy;
        private LocalDateTime resolvedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDetectorId() { return detectorId; }
        public void setDetectorId(String detectorId) { this.detectorId = detectorId; }

        public DataPoint getDataPoint() { return dataPoint; }
        public void setDataPoint(DataPoint dataPoint) { this.dataPoint = dataPoint; }

        public double getAnomalyScore() { return anomalyScore; }
        public void setAnomalyScore(double anomalyScore) { this.anomalyScore = anomalyScore; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public LocalDateTime getDetectedAt() { return detectedAt; }
        public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isResolved() { return isResolved; }
        public void setResolved(boolean resolved) { isResolved = resolved; }

        /**
         * Backwards-compatible alias used by older code paths.
         */
        public void setIsResolved(boolean isResolved) { this.isResolved = isResolved; }

        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }

        public String getResolvedBy() { return resolvedBy; }
        public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    }

    public static class AnomalySummary {
        private String detectorId;
        private int totalAnomalies;
        private int highSeverityCount;
        private int mediumSeverityCount;
        private int lowSeverityCount;
        private int resolvedCount;
        private double averageScore;
        private LocalDateTime lastDetected;

        // Getters and setters
        public String getDetectorId() { return detectorId; }
        public void setDetectorId(String detectorId) { this.detectorId = detectorId; }

        public int getTotalAnomalies() { return totalAnomalies; }
        public void setTotalAnomalies(int totalAnomalies) { this.totalAnomalies = totalAnomalies; }

        public int getHighSeverityCount() { return highSeverityCount; }
        public void setHighSeverityCount(int highSeverityCount) { this.highSeverityCount = highSeverityCount; }

        public int getMediumSeverityCount() { return mediumSeverityCount; }
        public void setMediumSeverityCount(int mediumSeverityCount) { this.mediumSeverityCount = mediumSeverityCount; }

        public int getLowSeverityCount() { return lowSeverityCount; }
        public void setLowSeverityCount(int lowSeverityCount) { this.lowSeverityCount = lowSeverityCount; }

        public int getResolvedCount() { return resolvedCount; }
        public void setResolvedCount(int resolvedCount) { this.resolvedCount = resolvedCount; }

        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

        public LocalDateTime getLastDetected() { return lastDetected; }
        public void setLastDetected(LocalDateTime lastDetected) { this.lastDetected = lastDetected; }
    }

    public static class AnomalyPattern {
        private String id;
        private String detectorId;
        private List<Anomaly> anomalies;
        private String patternType;
        private double confidence;
        private LocalDateTime detectedAt;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDetectorId() { return detectorId; }
        public void setDetectorId(String detectorId) { this.detectorId = detectorId; }

        public List<Anomaly> getAnomalies() { return anomalies; }
        public void setAnomalies(List<Anomaly> anomalies) { this.anomalies = anomalies; }

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

    public static class DetectionAnalytics {
        private String detectorId;
        private long totalAnomalies;
        private double detectionRate;
        private double falsePositiveRate;
        private double averageScore;
        private LocalDateTime lastDetected;

        // Getters and setters
        public String getDetectorId() { return detectorId; }
        public void setDetectorId(String detectorId) { this.detectorId = detectorId; }

        public long getTotalAnomalies() { return totalAnomalies; }
        public void setTotalAnomalies(long totalAnomalies) { this.totalAnomalies = totalAnomalies; }

        public double getDetectionRate() { return detectionRate; }
        public void setDetectionRate(double detectionRate) { this.detectionRate = detectionRate; }

        public double getFalsePositiveRate() { return falsePositiveRate; }
        public void setFalsePositiveRate(double falsePositiveRate) { this.falsePositiveRate = falsePositiveRate; }

        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

        public LocalDateTime getLastDetected() { return lastDetected; }
        public void setLastDetected(LocalDateTime lastDetected) { this.lastDetected = lastDetected; }
    }

    public static class DataPoint {
        private LocalDateTime timestamp;
        private double value;
        private Map<String, Object> metadata;

        public DataPoint() {
        }

        public DataPoint(LocalDateTime timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}


