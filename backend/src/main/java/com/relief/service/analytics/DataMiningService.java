package com.relief.service.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data mining service for discovering hidden patterns and insights in historical data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataMiningService {

    private static final Logger log = LoggerFactory.getLogger(DataMiningService.class);

    public MiningJob createMiningJob(String name, String description, String algorithm, 
                                   List<String> dataSources, Map<String, Object> parameters, String userId) {
        MiningJob job = new MiningJob();
        job.setId(UUID.randomUUID().toString());
        job.setName(name);
        job.setDescription(description);
        job.setAlgorithm(algorithm);
        job.setDataSources(dataSources);
        job.setParameters(parameters);
        job.setUserId(userId);
        job.setCreatedAt(LocalDateTime.now());
        job.setStatus(JobStatus.PENDING);
        job.setIsActive(true);
        
        log.info("Created data mining job: {} for user: {}", job.getId(), userId);
        return job;
    }

    public MiningJob executeJob(String jobId) {
        MiningJob job = new MiningJob();
        job.setId(jobId);
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        
        log.info("Started data mining job: {}", jobId);
        return job;
    }

    public MiningResult getMiningResult(String jobId) {
        MiningResult result = new MiningResult();
        result.setJobId(jobId);
        result.setPatterns(Collections.emptyList());
        result.setInsights(Collections.emptyList());
        result.setRecommendations(Collections.emptyList());
        result.setConfidenceScore(0.0);
        result.setGeneratedAt(LocalDateTime.now());
        
        log.info("Generated mining result for job: {}", jobId);
        return result;
    }

    public List<DataPattern> discoverPatterns(String dataSource, String patternType, 
                                            Map<String, Object> filters) {
        List<DataPattern> patterns = new ArrayList<>();
        
        // Sample pattern discovery
        DataPattern pattern = new DataPattern();
        pattern.setId(UUID.randomUUID().toString());
        pattern.setPatternType(patternType);
        pattern.setDescription("Discovered pattern in relief operations");
        pattern.setConfidence(0.85);
        pattern.setFrequency(0.72);
        pattern.setDataPoints(Collections.emptyList());
        pattern.setInsights(Arrays.asList("Peak activity during morning hours", "Higher success rate in urban areas"));
        pattern.setDiscoveredAt(LocalDateTime.now());
        
        patterns.add(pattern);
        
        log.info("Discovered {} patterns in data source: {}", patterns.size(), dataSource);
        return patterns;
    }

    public List<DataInsight> generateInsights(String dataSource, String insightType, 
                                            Map<String, Object> parameters) {
        List<DataInsight> insights = new ArrayList<>();
        
        // Sample insight generation
        DataInsight insight = new DataInsight();
        insight.setId(UUID.randomUUID().toString());
        insight.setInsightType(insightType);
        insight.setTitle("Resource Allocation Optimization");
        insight.setDescription("Analysis shows 30% improvement potential in resource distribution");
        insight.setConfidence(0.92);
        insight.setImpact(InsightImpact.HIGH);
        insight.setDataSource(dataSource);
        insight.setParameters(parameters);
        insight.setGeneratedAt(LocalDateTime.now());
        insight.setRecommendations(Arrays.asList("Reallocate 20% of resources to high-priority areas", "Implement dynamic routing based on real-time demand"));
        
        insights.add(insight);
        
        log.info("Generated {} insights for data source: {}", insights.size(), dataSource);
        return insights;
    }

    public PredictiveModel createPredictiveModel(String name, String modelType, String targetVariable, 
                                               List<String> features, Map<String, Object> parameters, String userId) {
        PredictiveModel model = new PredictiveModel();
        model.setId(UUID.randomUUID().toString());
        model.setName(name);
        model.setModelType(modelType);
        model.setTargetVariable(targetVariable);
        model.setFeatures(features);
        model.setParameters(parameters);
        model.setUserId(userId);
        model.setCreatedAt(LocalDateTime.now());
        model.setStatus(ModelStatus.TRAINING);
        model.setAccuracy(0.0);
        
        log.info("Created predictive model: {} for user: {}", model.getId(), userId);
        return model;
    }

    public ModelPrediction makePrediction(String modelId, Map<String, Object> inputData) {
        ModelPrediction prediction = new ModelPrediction();
        prediction.setId(UUID.randomUUID().toString());
        prediction.setModelId(modelId);
        prediction.setInputData(inputData);
        prediction.setPrediction(Collections.emptyMap());
        prediction.setConfidence(0.0);
        prediction.setGeneratedAt(LocalDateTime.now());
        
        log.info("Generated prediction using model: {}", modelId);
        return prediction;
    }

    public AnomalyDetection detectAnomalies(String dataSource, String detectionType, 
                                          Map<String, Object> parameters) {
        AnomalyDetection detection = new AnomalyDetection();
        detection.setId(UUID.randomUUID().toString());
        detection.setDataSource(dataSource);
        detection.setDetectionType(detectionType);
        detection.setParameters(parameters);
        detection.setAnomalies(Collections.emptyList());
        detection.setThreshold(0.8);
        detection.setDetectedAt(LocalDateTime.now());
        
        log.info("Detected anomalies in data source: {}", dataSource);
        return detection;
    }

    public TrendAnalysis analyzeTrends(String dataSource, String trendType, 
                                     LocalDateTime startDate, LocalDateTime endDate) {
        TrendAnalysis analysis = new TrendAnalysis();
        analysis.setId(UUID.randomUUID().toString());
        analysis.setDataSource(dataSource);
        analysis.setTrendType(trendType);
        analysis.setStartDate(startDate);
        analysis.setEndDate(endDate);
        analysis.setTrends(Collections.emptyList());
        analysis.setForecast(Collections.emptyMap());
        analysis.setGeneratedAt(LocalDateTime.now());
        
        log.info("Analyzed trends for data source: {} from {} to {}", dataSource, startDate, endDate);
        return analysis;
    }

    public CorrelationAnalysis findCorrelations(String dataSource, List<String> variables) {
        CorrelationAnalysis analysis = new CorrelationAnalysis();
        analysis.setId(UUID.randomUUID().toString());
        analysis.setDataSource(dataSource);
        analysis.setVariables(variables);
        analysis.setCorrelations(Collections.emptyList());
        analysis.setSignificance(0.05);
        analysis.setGeneratedAt(LocalDateTime.now());
        
        log.info("Found correlations for {} variables in data source: {}", variables.size(), dataSource);
        return analysis;
    }

    public ClusteringResult performClustering(String dataSource, String algorithm, 
                                            Map<String, Object> parameters) {
        ClusteringResult result = new ClusteringResult();
        result.setId(UUID.randomUUID().toString());
        result.setDataSource(dataSource);
        result.setAlgorithm(algorithm);
        result.setParameters(parameters);
        result.setClusters(Collections.emptyList());
        result.setSilhouetteScore(0.0);
        result.setGeneratedAt(LocalDateTime.now());
        
        log.info("Performed clustering on data source: {} using algorithm: {}", dataSource, algorithm);
        return result;
    }

    public MiningJob getJob(String jobId) {
        // Implementation for getting job
        MiningJob job = new MiningJob();
        job.setId(jobId);
        job.setName("Sample Mining Job");
        job.setDescription("Sample data mining job");
        job.setAlgorithm("KMEANS");
        job.setDataSources(Arrays.asList("relief_operations", "resource_usage"));
        job.setParameters(Collections.emptyMap());
        job.setUserId("user-123");
        job.setCreatedAt(LocalDateTime.now());
        job.setStatus(JobStatus.COMPLETED);
        job.setIsActive(true);
        
        return job;
    }

    public List<MiningJob> getUserJobs(String userId) {
        // Return a small set of synthetic mining jobs for the overview
        MiningJob jobsTrend = new MiningJob();
        jobsTrend.setId("job-trends-7d");
        jobsTrend.setName("Needs Trends - Last 7 Days");
        jobsTrend.setDescription("Analyze needs trends over the last 7 days");
        jobsTrend.setAlgorithm("TIME_SERIES");
        jobsTrend.setDataSources(List.of("needs_requests"));
        jobsTrend.setParameters(Map.of("window_days", 7));
        jobsTrend.setUserId(userId);
        jobsTrend.setCreatedAt(LocalDateTime.now().minusDays(2));
        jobsTrend.setStartedAt(LocalDateTime.now().minusDays(2).plusMinutes(5));
        jobsTrend.setCompletedAt(LocalDateTime.now().minusDays(2).plusMinutes(7));
        jobsTrend.setStatus(JobStatus.COMPLETED);
        jobsTrend.setIsActive(true);

        MiningJob jobsHotspots = new MiningJob();
        jobsHotspots.setId("job-hotspots");
        jobsHotspots.setName("Location Hotspots");
        jobsHotspots.setDescription("Cluster historical locations to find hotspots");
        jobsHotspots.setAlgorithm("DBSCAN");
        jobsHotspots.setDataSources(List.of("location_history"));
        jobsHotspots.setParameters(Map.of("min_points", 10, "eps", 0.5));
        jobsHotspots.setUserId(userId);
        jobsHotspots.setCreatedAt(LocalDateTime.now().minusDays(5));
        jobsHotspots.setStartedAt(LocalDateTime.now().minusDays(5).plusMinutes(3));
        jobsHotspots.setCompletedAt(LocalDateTime.now().minusDays(5).plusMinutes(8));
        jobsHotspots.setStatus(JobStatus.COMPLETED);
        jobsHotspots.setIsActive(true);

        return Arrays.asList(jobsTrend, jobsHotspots);
    }

    public MiningAnalytics getMiningAnalytics(String dataSource) {
        MiningAnalytics analytics = new MiningAnalytics();
        analytics.setDataSource(dataSource);
        analytics.setTotalJobs(12);
        analytics.setSuccessfulJobs(10);
        analytics.setAverageExecutionTime(95);
        analytics.setDiscoveredPatterns(4);
        analytics.setGeneratedInsights(7);
        analytics.setModelAccuracy(0.82);
        analytics.setLastAnalyzed(LocalDateTime.now().minusHours(1));
        
        return analytics;
    }

    public void deleteJob(String jobId) {
        log.info("Deleted mining job: {}", jobId);
    }

    // Data classes
    public static class MiningJob {
        private String id;
        private String name;
        private String description;
        private String algorithm;
        private List<String> dataSources;
        private Map<String, Object> parameters;
        private String userId;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private JobStatus status;
        private boolean isActive;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

        public List<String> getDataSources() { return dataSources; }
        public void setDataSources(List<String> dataSources) { this.dataSources = dataSources; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        // Compatibility setter for Lombok-style naming
        public void setIsActive(boolean active) { this.isActive = active; }
    }

    public static class MiningResult {
        private String jobId;
        private List<DataPattern> patterns;
        private List<DataInsight> insights;
        private List<String> recommendations;
        private double confidenceScore;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }

        public List<DataPattern> getPatterns() { return patterns; }
        public void setPatterns(List<DataPattern> patterns) { this.patterns = patterns; }

        public List<DataInsight> getInsights() { return insights; }
        public void setInsights(List<DataInsight> insights) { this.insights = insights; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class DataPattern {
        private String id;
        private String patternType;
        private String description;
        private double confidence;
        private double frequency;
        private List<Object> dataPoints;
        private List<String> insights;
        private LocalDateTime discoveredAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getPatternType() { return patternType; }
        public void setPatternType(String patternType) { this.patternType = patternType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public double getFrequency() { return frequency; }
        public void setFrequency(double frequency) { this.frequency = frequency; }

        public List<Object> getDataPoints() { return dataPoints; }
        public void setDataPoints(List<Object> dataPoints) { this.dataPoints = dataPoints; }

        public List<String> getInsights() { return insights; }
        public void setInsights(List<String> insights) { this.insights = insights; }

        public LocalDateTime getDiscoveredAt() { return discoveredAt; }
        public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }
    }

    public static class DataInsight {
        private String id;
        private String insightType;
        private String title;
        private String description;
        private double confidence;
        private InsightImpact impact;
        private String dataSource;
        private Map<String, Object> parameters;
        private LocalDateTime generatedAt;
        private List<String> recommendations;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getInsightType() { return insightType; }
        public void setInsightType(String insightType) { this.insightType = insightType; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public InsightImpact getImpact() { return impact; }
        public void setImpact(InsightImpact impact) { this.impact = impact; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    public static class PredictiveModel {
        private String id;
        private String name;
        private String modelType;
        private String targetVariable;
        private List<String> features;
        private Map<String, Object> parameters;
        private String userId;
        private LocalDateTime createdAt;
        private ModelStatus status;
        private double accuracy;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getModelType() { return modelType; }
        public void setModelType(String modelType) { this.modelType = modelType; }

        public String getTargetVariable() { return targetVariable; }
        public void setTargetVariable(String targetVariable) { this.targetVariable = targetVariable; }

        public List<String> getFeatures() { return features; }
        public void setFeatures(List<String> features) { this.features = features; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public ModelStatus getStatus() { return status; }
        public void setStatus(ModelStatus status) { this.status = status; }

        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    }

    public static class ModelPrediction {
        private String id;
        private String modelId;
        private Map<String, Object> inputData;
        private Map<String, Object> prediction;
        private double confidence;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }

        public Map<String, Object> getInputData() { return inputData; }
        public void setInputData(Map<String, Object> inputData) { this.inputData = inputData; }

        public Map<String, Object> getPrediction() { return prediction; }
        public void setPrediction(Map<String, Object> prediction) { this.prediction = prediction; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class AnomalyDetection {
        private String id;
        private String dataSource;
        private String detectionType;
        private Map<String, Object> parameters;
        private List<Anomaly> anomalies;
        private double threshold;
        private LocalDateTime detectedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public String getDetectionType() { return detectionType; }
        public void setDetectionType(String detectionType) { this.detectionType = detectionType; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public List<Anomaly> getAnomalies() { return anomalies; }
        public void setAnomalies(List<Anomaly> anomalies) { this.anomalies = anomalies; }

        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }

        public LocalDateTime getDetectedAt() { return detectedAt; }
        public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    }

    public static class Anomaly {
        private String id;
        private String type;
        private String description;
        private double severity;
        private Map<String, Object> data;
        private LocalDateTime detectedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getSeverity() { return severity; }
        public void setSeverity(double severity) { this.severity = severity; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }

        public LocalDateTime getDetectedAt() { return detectedAt; }
        public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    }

    public static class TrendAnalysis {
        private String id;
        private String dataSource;
        private String trendType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<Trend> trends;
        private Map<String, Object> forecast;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public String getTrendType() { return trendType; }
        public void setTrendType(String trendType) { this.trendType = trendType; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public List<Trend> getTrends() { return trends; }
        public void setTrends(List<Trend> trends) { this.trends = trends; }

        public Map<String, Object> getForecast() { return forecast; }
        public void setForecast(Map<String, Object> forecast) { this.forecast = forecast; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class Trend {
        private String id;
        private String name;
        private String direction;
        private double strength;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Map<String, Object> data;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }

        public double getStrength() { return strength; }
        public void setStrength(double strength) { this.strength = strength; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }

    public static class CorrelationAnalysis {
        private String id;
        private String dataSource;
        private List<String> variables;
        private List<Correlation> correlations;
        private double significance;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public List<String> getVariables() { return variables; }
        public void setVariables(List<String> variables) { this.variables = variables; }

        public List<Correlation> getCorrelations() { return correlations; }
        public void setCorrelations(List<Correlation> correlations) { this.correlations = correlations; }

        public double getSignificance() { return significance; }
        public void setSignificance(double significance) { this.significance = significance; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class Correlation {
        private String variable1;
        private String variable2;
        private double coefficient;
        private double pValue;
        private String strength;

        // Getters and setters
        public String getVariable1() { return variable1; }
        public void setVariable1(String variable1) { this.variable1 = variable1; }

        public String getVariable2() { return variable2; }
        public void setVariable2(String variable2) { this.variable2 = variable2; }

        public double getCoefficient() { return coefficient; }
        public void setCoefficient(double coefficient) { this.coefficient = coefficient; }

        public double getPValue() { return pValue; }
        public void setPValue(double pValue) { this.pValue = pValue; }

        public String getStrength() { return strength; }
        public void setStrength(String strength) { this.strength = strength; }
    }

    public static class ClusteringResult {
        private String id;
        private String dataSource;
        private String algorithm;
        private Map<String, Object> parameters;
        private List<Cluster> clusters;
        private double silhouetteScore;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public List<Cluster> getClusters() { return clusters; }
        public void setClusters(List<Cluster> clusters) { this.clusters = clusters; }

        public double getSilhouetteScore() { return silhouetteScore; }
        public void setSilhouetteScore(double silhouetteScore) { this.silhouetteScore = silhouetteScore; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class Cluster {
        private String id;
        private String name;
        private int size;
        private Map<String, Object> centroid;
        private List<String> characteristics;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public Map<String, Object> getCentroid() { return centroid; }
        public void setCentroid(Map<String, Object> centroid) { this.centroid = centroid; }

        public List<String> getCharacteristics() { return characteristics; }
        public void setCharacteristics(List<String> characteristics) { this.characteristics = characteristics; }
    }

    public static class MiningAnalytics {
        private String dataSource;
        private int totalJobs;
        private int successfulJobs;
        private int averageExecutionTime;
        private int discoveredPatterns;
        private int generatedInsights;
        private double modelAccuracy;
        private LocalDateTime lastAnalyzed;

        // Getters and setters
        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public int getTotalJobs() { return totalJobs; }
        public void setTotalJobs(int totalJobs) { this.totalJobs = totalJobs; }

        public int getSuccessfulJobs() { return successfulJobs; }
        public void setSuccessfulJobs(int successfulJobs) { this.successfulJobs = successfulJobs; }

        public int getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(int averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public int getDiscoveredPatterns() { return discoveredPatterns; }
        public void setDiscoveredPatterns(int discoveredPatterns) { this.discoveredPatterns = discoveredPatterns; }

        public int getGeneratedInsights() { return generatedInsights; }
        public void setGeneratedInsights(int generatedInsights) { this.generatedInsights = generatedInsights; }

        public double getModelAccuracy() { return modelAccuracy; }
        public void setModelAccuracy(double modelAccuracy) { this.modelAccuracy = modelAccuracy; }

        public LocalDateTime getLastAnalyzed() { return lastAnalyzed; }
        public void setLastAnalyzed(LocalDateTime lastAnalyzed) { this.lastAnalyzed = lastAnalyzed; }
    }

    public enum JobStatus {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }

    public enum InsightImpact {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ModelStatus {
        TRAINING, READY, FAILED, DEPRECATED
    }
}


