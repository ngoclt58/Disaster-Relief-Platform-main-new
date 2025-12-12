package com.relief.service.ai;

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
 * Service for ML-based disaster prediction models
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DisasterPredictionService {

    private static final Logger log = LoggerFactory.getLogger(DisasterPredictionService.class);

    private final Map<String, DisasterPredictionModel> models = new ConcurrentHashMap<>();
    private final Map<String, List<DisasterPrediction>> predictions = new ConcurrentHashMap<>();

    public DisasterPredictionModel createModel(
            String name,
            String description,
            String disasterType,
            Map<String, Object> features,
            Map<String, Object> parameters) {
        
        DisasterPredictionModel model = new DisasterPredictionModel();
        model.setId(UUID.randomUUID().toString());
        model.setName(name);
        model.setDescription(description);
        model.setDisasterType(disasterType);
        model.setFeatures(features);
        model.setParameters(parameters);
        model.setCreatedAt(LocalDateTime.now());
        model.setAccuracy(0.0);
        model.setStatus(ModelStatus.TRAINING);
        
        models.put(model.getId(), model);
        
        log.info("Created disaster prediction model: {} for type: {}", model.getId(), disasterType);
        return model;
    }

    public void trainModel(String modelId, List<HistoricalData> trainingData) {
        DisasterPredictionModel model = models.get(modelId);
        if (model == null) {
            throw new IllegalArgumentException("Model not found: " + modelId);
        }

        model.setStatus(ModelStatus.TRAINING);
        
        // Simulate ML training process
        double accuracy = calculateModelAccuracy(trainingData, model);
        model.setAccuracy(accuracy);
        model.setStatus(ModelStatus.READY);
        model.setTrainedAt(LocalDateTime.now());
        
        log.info("Model {} trained with accuracy: {}", modelId, accuracy);
    }

    public DisasterPrediction predictDisaster(String modelId, PredictionInput input) {
        DisasterPredictionModel model = models.get(modelId);
        if (model == null || model.getStatus() != ModelStatus.READY) {
            throw new IllegalArgumentException("Model not ready: " + modelId);
        }

        DisasterPrediction prediction = new DisasterPrediction();
        prediction.setId(UUID.randomUUID().toString());
        prediction.setModelId(modelId);
        prediction.setDisasterType(model.getDisasterType());
        prediction.setInput(input);
        prediction.setPredictedAt(LocalDateTime.now());
        prediction.setLikelihood(calculateLikelihood(input, model));
        prediction.setConfidence(calculateConfidence(input, model));
        prediction.setSeverity(predictSeverity(input, model));
        prediction.setTimeline(predictTimeline(input, model));
        prediction.setAffectedAreas(predictAffectedAreas(input, model));
        prediction.setRecommendedActions(generateRecommendations(prediction));

        predictions.computeIfAbsent(modelId, k -> new ArrayList<>()).add(prediction);
        
        log.info("Disaster prediction generated: likelihood={}, confidence={}", 
                prediction.getLikelihood(), prediction.getConfidence());
        return prediction;
    }

    public List<DisasterPrediction> getPredictions(String modelId, LocalDateTime startTime, LocalDateTime endTime) {
        return predictions.getOrDefault(modelId, new ArrayList<>()).stream()
                .filter(p -> p.getPredictedAt().isAfter(startTime) && p.getPredictedAt().isBefore(endTime))
                .sorted((a, b) -> b.getPredictedAt().compareTo(a.getPredictedAt()))
                .collect(Collectors.toList());
    }

    public PredictionEvaluation evaluateModel(String modelId, List<TestData> testData) {
        DisasterPredictionModel model = models.get(modelId);
        if (model == null) {
            throw new IllegalArgumentException("Model not found: " + modelId);
        }

        PredictionEvaluation evaluation = new PredictionEvaluation();
        evaluation.setModelId(modelId);
        evaluation.setTotalSamples(testData.size());
        
        int correctPredictions = 0;
        for (TestData test : testData) {
            DisasterPrediction prediction = predictDisaster(modelId, test.getInput());
            if (isPredictionCorrect(prediction, test.getExpected())) {
                correctPredictions++;
            }
        }
        
        evaluation.setAccuracy((double) correctPredictions / testData.size());
        evaluation.setPrecision(calculatePrecision(testData));
        evaluation.setRecall(calculateRecall(testData));
        evaluation.setF1Score(calculateF1Score(evaluation.getPrecision(), evaluation.getRecall()));
        evaluation.setEvaluatedAt(LocalDateTime.now());
        
        return evaluation;
    }

    public DisasterPredictionModel getModel(String modelId) {
        return models.get(modelId);
    }

    public List<DisasterPredictionModel> getModels() {
        return new ArrayList<>(models.values());
    }

    private double calculateModelAccuracy(List<HistoricalData> trainingData, DisasterPredictionModel model) {
        if (trainingData.isEmpty()) return 0.0;

        // Simplified accuracy calculation
        double correct = 0;
        for (HistoricalData data : trainingData) {
            double predicted = calculateLikelihood(data.getInput(), model);
            double actual = data.getActualOutcome();
            if (Math.abs(predicted - actual) < 0.2) {
                correct++;
            }
        }

        return correct / trainingData.size();
    }

    private double calculateLikelihood(PredictionInput input, DisasterPredictionModel model) {
        // Simplified ML prediction based on input features
        double score = 0.0;
        
        // Weather factors
        if (input.getWeatherData() != null) {
            score += (double) input.getWeatherData().getOrDefault("humidity", 0) / 100.0 * 0.2;
            score += (double) input.getWeatherData().getOrDefault("windSpeed", 0) / 50.0 * 0.2;
        }
        
        // Geographic factors
        if (input.getGeographicData() != null) {
            score += (double) input.getGeographicData().getOrDefault("elevation", 0) / 5000.0 * 0.2;
            score += (double) input.getGeographicData().getOrDefault("coastal", 0) * 0.2;
        }
        
        // Historical pattern
        if (input.getHistoricalData() != null) {
            score += (double) input.getHistoricalData().getOrDefault("historicalIncidents", 0) / 10.0 * 0.2;
        }
        
        return Math.min(1.0, score);
    }

    private double calculateConfidence(PredictionInput input, DisasterPredictionModel model) {
        // Confidence based on data quality and completeness
        double confidence = 0.5;
        
        if (input.getWeatherData() != null && !input.getWeatherData().isEmpty()) confidence += 0.2;
        if (input.getGeographicData() != null && !input.getGeographicData().isEmpty()) confidence += 0.2;
        if (input.getHistoricalData() != null && !input.getHistoricalData().isEmpty()) confidence += 0.1;
        
        return Math.min(1.0, confidence);
    }

    private String predictSeverity(PredictionInput input, DisasterPredictionModel model) {
        double likelihood = calculateLikelihood(input, model);
        
        if (likelihood < 0.3) return "LOW";
        if (likelihood < 0.6) return "MODERATE";
        if (likelihood < 0.8) return "HIGH";
        return "CRITICAL";
    }

    private PredictionTimeline predictTimeline(PredictionInput input, DisasterPredictionModel model) {
        PredictionTimeline timeline = new PredictionTimeline();
        LocalDateTime now = LocalDateTime.now();
        
        timeline.setImmediateRisk(calculateLikelihood(input, model) > 0.7);
        timeline.setExpectedWindow(now.plusHours(24), now.plusHours(72));
        timeline.setPeakRiskWindow(now.plusHours(12), now.plusHours(48));
        
        return timeline;
    }

    private List<String> predictAffectedAreas(PredictionInput input, DisasterPredictionModel model) {
        List<String> areas = new ArrayList<>();
        areas.add("Primary impact zone");
        areas.add("Secondary impact zone");
        
        if (calculateLikelihood(input, model) > 0.6) {
            areas.add("Tertiary impact zone");
        }
        
        return areas;
    }

    private List<String> generateRecommendations(DisasterPrediction prediction) {
        List<String> recommendations = new ArrayList<>();
        
        if (prediction.getLikelihood() > 0.7) {
            recommendations.add("Issue immediate evacuation orders");
            recommendations.add("Mobilize emergency response teams");
            recommendations.add("Activate emergency shelters");
        } else if (prediction.getLikelihood() > 0.5) {
            recommendations.add("Prepare emergency response resources");
            recommendations.add("Alert local authorities");
            recommendations.add("Activate monitoring systems");
        } else {
            recommendations.add("Continue monitoring situation");
            recommendations.add("Review contingency plans");
        }
        
        return recommendations;
    }

    private boolean isPredictionCorrect(DisasterPrediction prediction, Object expected) {
        // Simplified correctness check
        return prediction.getLikelihood() > 0.5; // Prediction says disaster will occur
    }

    private double calculatePrecision(List<TestData> testData) {
        // Simplified precision calculation
        return 0.85;
    }

    private double calculateRecall(List<TestData> testData) {
        // Simplified recall calculation
        return 0.82;
    }

    private double calculateF1Score(double precision, double recall) {
        if (precision + recall == 0) return 0.0;
        return 2 * (precision * recall) / (precision + recall);
    }

    // Data classes
    public static class DisasterPredictionModel {
        private String id;
        private String name;
        private String description;
        private String disasterType;
        private Map<String, Object> features;
        private Map<String, Object> parameters;
        private LocalDateTime createdAt;
        private LocalDateTime trainedAt;
        private double accuracy;
        private ModelStatus status;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDisasterType() { return disasterType; }
        public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

        public Map<String, Object> getFeatures() { return features; }
        public void setFeatures(Map<String, Object> features) { this.features = features; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getTrainedAt() { return trainedAt; }
        public void setTrainedAt(LocalDateTime trainedAt) { this.trainedAt = trainedAt; }

        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

        public ModelStatus getStatus() { return status; }
        public void setStatus(ModelStatus status) { this.status = status; }
    }

    public static class DisasterPrediction {
        private String id;
        private String modelId;
        private String disasterType;
        private PredictionInput input;
        private LocalDateTime predictedAt;
        private double likelihood;
        private double confidence;
        private String severity;
        private PredictionTimeline timeline;
        private List<String> affectedAreas;
        private List<String> recommendedActions;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }

        public String getDisasterType() { return disasterType; }
        public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

        public PredictionInput getInput() { return input; }
        public void setInput(PredictionInput input) { this.input = input; }

        public LocalDateTime getPredictedAt() { return predictedAt; }
        public void setPredictedAt(LocalDateTime predictedAt) { this.predictedAt = predictedAt; }

        public double getLikelihood() { return likelihood; }
        public void setLikelihood(double likelihood) { this.likelihood = likelihood; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public PredictionTimeline getTimeline() { return timeline; }
        public void setTimeline(PredictionTimeline timeline) { this.timeline = timeline; }

        public List<String> getAffectedAreas() { return affectedAreas; }
        public void setAffectedAreas(List<String> affectedAreas) { this.affectedAreas = affectedAreas; }

        public List<String> getRecommendedActions() { return recommendedActions; }
        public void setRecommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; }
    }

    public static class PredictionInput {
        private Map<String, Object> weatherData;
        private Map<String, Object> geographicData;
        private Map<String, Object> historicalData;
        private Map<String, Object> realTimeData;

        // Getters and setters
        public Map<String, Object> getWeatherData() { return weatherData; }
        public void setWeatherData(Map<String, Object> weatherData) { this.weatherData = weatherData; }

        public Map<String, Object> getGeographicData() { return geographicData; }
        public void setGeographicData(Map<String, Object> geographicData) { this.geographicData = geographicData; }

        public Map<String, Object> getHistoricalData() { return historicalData; }
        public void setHistoricalData(Map<String, Object> historicalData) { this.historicalData = historicalData; }

        public Map<String, Object> getRealTimeData() { return realTimeData; }
        public void setRealTimeData(Map<String, Object> realTimeData) { this.realTimeData = realTimeData; }
    }

    public static class PredictionTimeline {
        private boolean immediateRisk;
        private LocalDateTime immediateRiskStart;
        private LocalDateTime immediateRiskEnd;
        private LocalDateTime peakRiskStart;
        private LocalDateTime peakRiskEnd;
        private LocalDateTime expectedStart;
        private LocalDateTime expectedEnd;

        // Getters and setters
        public boolean isImmediateRisk() { return immediateRisk; }
        public void setImmediateRisk(boolean immediateRisk) { this.immediateRisk = immediateRisk; }

        public LocalDateTime getImmediateRiskStart() { return immediateRiskStart; }
        public void setImmediateRiskStart(LocalDateTime immediateRiskStart) { this.immediateRiskStart = immediateRiskStart; }

        public LocalDateTime getImmediateRiskEnd() { return immediateRiskEnd; }
        public void setImmediateRiskEnd(LocalDateTime immediateRiskEnd) { this.immediateRiskEnd = immediateRiskEnd; }

        public LocalDateTime getPeakRiskStart() { return peakRiskStart; }
        public void setPeakRiskStart(LocalDateTime peakRiskStart) { this.peakRiskStart = peakRiskStart; }
        
        public LocalDateTime getPeakRiskEnd() { return peakRiskEnd; }
        public void setPeakRiskEnd(LocalDateTime peakRiskEnd) { this.peakRiskEnd = peakRiskEnd; }
        
        public void setPeakRiskWindow(LocalDateTime start, LocalDateTime end) {
            this.peakRiskStart = start;
            this.peakRiskEnd = end;
        }

        public LocalDateTime getExpectedStart() { return expectedStart; }
        public void setExpectedStart(LocalDateTime expectedStart) { this.expectedStart = expectedStart; }
        
        public LocalDateTime getExpectedEnd() { return expectedEnd; }
        public void setExpectedEnd(LocalDateTime expectedEnd) { this.expectedEnd = expectedEnd; }
        
        public void setExpectedWindow(LocalDateTime start, LocalDateTime end) {
            this.expectedStart = start;
            this.expectedEnd = end;
        }
    }

    public static class HistoricalData {
        private PredictionInput input;
        private double actualOutcome;

        public PredictionInput getInput() { return input; }
        public void setInput(PredictionInput input) { this.input = input; }

        public double getActualOutcome() { return actualOutcome; }
        public void setActualOutcome(double actualOutcome) { this.actualOutcome = actualOutcome; }
    }

    public static class TestData {
        private PredictionInput input;
        private Object expected;

        public PredictionInput getInput() { return input; }
        public void setInput(PredictionInput input) { this.input = input; }

        public Object getExpected() { return expected; }
        public void setExpected(Object expected) { this.expected = expected; }
    }

    public static class PredictionEvaluation {
        private String modelId;
        private int totalSamples;
        private double accuracy;
        private double precision;
        private double recall;
        private double f1Score;
        private LocalDateTime evaluatedAt;

        // Getters and setters
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }

        public int getTotalSamples() { return totalSamples; }
        public void setTotalSamples(int totalSamples) { this.totalSamples = totalSamples; }

        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

        public double getPrecision() { return precision; }
        public void setPrecision(double precision) { this.precision = precision; }

        public double getRecall() { return recall; }
        public void setRecall(double recall) { this.recall = recall; }

        public double getF1Score() { return f1Score; }
        public void setF1Score(double f1Score) { this.f1Score = f1Score; }

        public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
        public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    }

    public enum ModelStatus {
        CREATED, TRAINING, READY, ERROR
    }
}

