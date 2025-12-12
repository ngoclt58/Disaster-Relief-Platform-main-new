package com.relief.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for AI-powered resource demand forecasting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceDemandForecastingService {

    private final Map<String, ForecastingModel> models = new ConcurrentHashMap<>();
    private final Map<String, List<DemandForecast>> forecasts = new ConcurrentHashMap<>();

    public ForecastingModel createModel(String name, String description, String resourceType,
                                       Map<String, Object> parameters) {
        ForecastingModel model = new ForecastingModel();
        model.setId(UUID.randomUUID().toString());
        model.setName(name);
        model.setDescription(description);
        model.setResourceType(resourceType);
        model.setParameters(parameters);
        model.setCreatedAt(LocalDateTime.now());
        model.setAccuracy(0.0);
        model.setStatus(ModelStatus.TRAINING);
        
        models.put(model.getId(), model);
        
        log.info("Created forecasting model: {} for resource type: {}", model.getId(), resourceType);
        return model;
    }

    public void trainModel(String modelId, List<HistoricalDemand> trainingData) {
        ForecastingModel model = models.get(modelId);
        if (model == null) {
            throw new IllegalArgumentException("Model not found: " + modelId);
        }

        model.setStatus(ModelStatus.TRAINING);
        
        // Simulate ML training
        double accuracy = calculateModelAccuracy(trainingData, model);
        model.setAccuracy(accuracy);
        model.setStatus(ModelStatus.READY);
        model.setTrainedAt(LocalDateTime.now());
        
        log.info("Model {} trained with accuracy: {}", modelId, accuracy);
    }

    public DemandForecast forecastDemand(String modelId, ForecastInput input) {
        ForecastingModel model = models.get(modelId);
        if (model == null || model.getStatus() != ModelStatus.READY) {
            throw new IllegalArgumentException("Model not ready: " + modelId);
        }

        DemandForecast forecast = new DemandForecast();
        forecast.setId(UUID.randomUUID().toString());
        forecast.setModelId(modelId);
        forecast.setResourceType(model.getResourceType());
        forecast.setInput(input);
        forecast.setForecastedAt(LocalDateTime.now());
        forecast.setProjectedDemand(calculateDemand(input, model));
        forecast.setConfidence(calculateConfidence(input, model));
        forecast.setTimeHorizon(predictTimeHorizon(input, model));
        forecast.setPeakDemandTime(predictPeakDemand(input, model));
        forecast.setRecommendedResources(generateRecommendations(forecast));

        forecasts.computeIfAbsent(modelId, k -> new ArrayList<>()).add(forecast);
        
        log.info("Demand forecast generated: projected={}, confidence={}", 
                forecast.getProjectedDemand(), forecast.getConfidence());
        return forecast;
    }

    public List<DemandForecast> getForecasts(String modelId, LocalDateTime startTime, LocalDateTime endTime) {
        return forecasts.getOrDefault(modelId, new ArrayList<>()).stream()
                .filter(f -> f.getForecastedAt().isAfter(startTime) && f.getForecastedAt().isBefore(endTime))
                .sorted((a, b) -> b.getForecastedAt().compareTo(a.getForecastedAt()))
                .collect(Collectors.toList());
    }

    public ForecastingModel getModel(String modelId) {
        return models.get(modelId);
    }

    public List<ForecastingModel> getModels() {
        return new ArrayList<>(models.values());
    }

    private double calculateModelAccuracy(List<HistoricalDemand> trainingData, ForecastingModel model) {
        if (trainingData.isEmpty()) return 0.0;

        double mae = 0.0;
        for (HistoricalDemand data : trainingData) {
            double predicted = calculateDemand(data.getInput(), model);
            double actual = data.getActualDemand();
            mae += Math.abs(predicted - actual);
        }

        double mape = (mae / trainingData.size()) / 
                (trainingData.stream().mapToDouble(HistoricalDemand::getActualDemand).average().orElse(1.0));
        
        return 1.0 - Math.min(mape, 1.0);
    }

    private double calculateDemand(ForecastInput input, ForecastingModel model) {
        double demand = 0.0;
        
        // Disaster type factor
        if (input.getDisasterType() != null) {
            demand += getDisasterTypeMultiplier(input.getDisasterType());
        }
        
        // Severity factor
        demand += (input.getSeverity() != null ? input.getSeverity() : 5) * 10;
        
        // Population density factor
        demand += (input.getPopulationDensity() != null ? input.getPopulationDensity() : 100) / 10;
        
        // Historical demand factor
        if (input.getHistoricalDemand() != null) {
            demand += input.getHistoricalDemand() * 0.5;
        }
        
        return Math.max(0, demand);
    }

    private double getDisasterTypeMultiplier(String disasterType) {
        switch (disasterType.toUpperCase()) {
            case "EARTHQUAKE": return 50.0;
            case "FLOOD": return 40.0;
            case "HURRICANE": return 45.0;
            case "WILDFIRE": return 35.0;
            case "TORNADO": return 30.0;
            case "TSUNAMI": return 55.0;
            default: return 25.0;
        }
    }

    private double calculateConfidence(ForecastInput input, ForecastingModel model) {
        double confidence = 0.3;
        
        if (input.getDisasterType() != null) confidence += 0.2;
        if (input.getSeverity() != null) confidence += 0.2;
        if (input.getPopulationDensity() != null) confidence += 0.15;
        if (input.getHistoricalDemand() != null) confidence += 0.15;
        
        return Math.min(1.0, confidence);
    }

    private ForecastTimeline predictTimeHorizon(ForecastInput input, ForecastingModel model) {
        ForecastTimeline timeline = new ForecastTimeline();
        LocalDateTime now = LocalDateTime.now();
        
        timeline.setImmediateNeed(0, 6); // 0-6 hours
        timeline.setShortTermNeed(6, 24); // 6-24 hours
        timeline.setMediumTermNeed(24, 72); // 24-72 hours
        timeline.setLongTermNeed(72, 168); // 72-168 hours (1 week)
        
        return timeline;
    }

    private LocalDateTime predictPeakDemand(ForecastInput input, ForecastingModel model) {
        // Predict peak demand in 12-36 hours based on disaster type
        return LocalDateTime.now().plusHours(24);
    }

    private List<String> generateRecommendations(DemandForecast forecast) {
        List<String> recommendations = new ArrayList<>();
        
        if (forecast.getProjectedDemand() > 100) {
            recommendations.add("Mobilize all available resources");
            recommendations.add("Request assistance from neighboring regions");
            recommendations.add("Activate emergency procurement protocols");
        } else if (forecast.getProjectedDemand() > 50) {
            recommendations.add("Increase inventory levels");
            recommendations.add("Alert suppliers for potential orders");
            recommendations.add("Pre-position resources in key locations");
        } else {
            recommendations.add("Monitor situation closely");
            recommendations.add("Maintain normal inventory levels");
        }
        
        return recommendations;
    }

    // Data classes
    public static class ForecastingModel {
        private String id;
        private String name;
        private String description;
        private String resourceType;
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

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }

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

    public static class DemandForecast {
        private String id;
        private String modelId;
        private String resourceType;
        private ForecastInput input;
        private LocalDateTime forecastedAt;
        private double projectedDemand;
        private double confidence;
        private ForecastTimeline timeHorizon;
        private LocalDateTime peakDemandTime;
        private List<String> recommendedResources;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }

        public ForecastInput getInput() { return input; }
        public void setInput(ForecastInput input) { this.input = input; }

        public LocalDateTime getForecastedAt() { return forecastedAt; }
        public void setForecastedAt(LocalDateTime forecastedAt) { this.forecastedAt = forecastedAt; }

        public double getProjectedDemand() { return projectedDemand; }
        public void setProjectedDemand(double projectedDemand) { this.projectedDemand = projectedDemand; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public ForecastTimeline getTimeHorizon() { return timeHorizon; }
        public void setTimeHorizon(ForecastTimeline timeHorizon) { this.timeHorizon = timeHorizon; }

        public LocalDateTime getPeakDemandTime() { return peakDemandTime; }
        public void setPeakDemandTime(LocalDateTime peakDemandTime) { this.peakDemandTime = peakDemandTime; }

        public List<String> getRecommendedResources() { return recommendedResources; }
        public void setRecommendedResources(List<String> recommendedResources) { this.recommendedResources = recommendedResources; }
    }

    public static class ForecastInput {
        private String disasterType;
        private Integer severity;
        private Integer populationDensity;
        private Double historicalDemand;

        // Getters and setters
        public String getDisasterType() { return disasterType; }
        public void setDisasterType(String disasterType) { this.disasterType = disasterType; }

        public Integer getSeverity() { return severity; }
        public void setSeverity(Integer severity) { this.severity = severity; }

        public Integer getPopulationDensity() { return populationDensity; }
        public void setPopulationDensity(Integer populationDensity) { this.populationDensity = populationDensity; }

        public Double getHistoricalDemand() { return historicalDemand; }
        public void setHistoricalDemand(Double historicalDemand) { this.historicalDemand = historicalDemand; }
    }

    public static class ForecastTimeline {
        private int immediateHoursStart;
        private int immediateHoursEnd;
        private int shortHoursStart;
        private int shortHoursEnd;
        private int mediumHoursStart;
        private int mediumHoursEnd;
        private int longHoursStart;
        private int longHoursEnd;

        public void setImmediateNeed(int start, int end) {
            this.immediateHoursStart = start;
            this.immediateHoursEnd = end;
        }

        public void setShortTermNeed(int start, int end) {
            this.shortHoursStart = start;
            this.shortHoursEnd = end;
        }

        public void setMediumTermNeed(int start, int end) {
            this.mediumHoursStart = start;
            this.mediumHoursEnd = end;
        }

        public void setLongTermNeed(int start, int end) {
            this.longHoursStart = start;
            this.longHoursEnd = end;
        }

        // Getters and setters
        public int getImmediateHoursStart() { return immediateHoursStart; }
        public int getImmediateHoursEnd() { return immediateHoursEnd; }
        public int getShortHoursStart() { return shortHoursStart; }
        public int getShortHoursEnd() { return shortHoursEnd; }
        public int getMediumHoursStart() { return mediumHoursStart; }
        public int getMediumHoursEnd() { return mediumHoursEnd; }
        public int getLongHoursStart() { return longHoursStart; }
        public int getLongHoursEnd() { return longHoursEnd; }
    }

    public static class HistoricalDemand {
        private ForecastInput input;
        private double actualDemand;

        public ForecastInput getInput() { return input; }
        public void setInput(ForecastInput input) { this.input = input; }

        public double getActualDemand() { return actualDemand; }
        public void setActualDemand(double actualDemand) { this.actualDemand = actualDemand; }
    }

    public enum ModelStatus {
        CREATED, TRAINING, READY, ERROR
    }
}

