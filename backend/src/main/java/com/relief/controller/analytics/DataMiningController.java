package com.relief.controller.analytics;

import com.relief.service.analytics.DataMiningService;
import com.relief.service.analytics.DataMiningService.MiningJob;
import com.relief.service.analytics.DataMiningService.MiningResult;
import com.relief.service.analytics.DataMiningService.DataPattern;
import com.relief.service.analytics.DataMiningService.DataInsight;
import com.relief.service.analytics.DataMiningService.PredictiveModel;
import com.relief.service.analytics.DataMiningService.ModelPrediction;
import com.relief.service.analytics.DataMiningService.AnomalyDetection;
import com.relief.service.analytics.DataMiningService.TrendAnalysis;
import com.relief.service.analytics.DataMiningService.CorrelationAnalysis;
import com.relief.service.analytics.DataMiningService.ClusteringResult;
import com.relief.service.analytics.DataMiningService.MiningAnalytics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data mining controller for discovering hidden patterns and insights in historical data
 */
@RestController
@RequestMapping("/analytics/data-mining")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Mining", description = "Discover hidden patterns and insights in historical data")
public class DataMiningController {

    private final DataMiningService dataMiningService;

    @lombok.Data
    public static class CreateMiningJobRequest {
        private String name;
        private String description;
        private String algorithm;
        private java.util.List<String> dataSources;
        private java.util.Map<String, Object> parameters;
        private String userId;
    }

    @PostMapping("/jobs")
    @Operation(summary = "Create data mining job")
    public ResponseEntity<MiningJob> createMiningJob(@RequestBody CreateMiningJobRequest request) {
        MiningJob job = dataMiningService.createMiningJob(
                request.getName(),
                request.getDescription(),
                request.getAlgorithm(),
                request.getDataSources() != null ? request.getDataSources() : List.of(),
                request.getParameters() != null ? request.getParameters() : Map.of(),
                request.getUserId()
        );
        return ResponseEntity.ok(job);
    }

    @PostMapping("/jobs/{jobId}/execute")
    @Operation(summary = "Execute data mining job")
    public ResponseEntity<MiningJob> executeJob(@PathVariable String jobId) {
        MiningJob job = dataMiningService.executeJob(jobId);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Get data mining job")
    public ResponseEntity<MiningJob> getJob(@PathVariable String jobId) {
        MiningJob job = dataMiningService.getJob(jobId);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/jobs")
    @Operation(summary = "Get user mining jobs")
    public ResponseEntity<List<MiningJob>> getUserJobs(@RequestParam String userId) {
        List<MiningJob> jobs = dataMiningService.getUserJobs(userId);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{jobId}/result")
    @Operation(summary = "Get mining result")
    public ResponseEntity<MiningResult> getMiningResult(@PathVariable String jobId) {
        MiningResult result = dataMiningService.getMiningResult(jobId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/patterns/discover")
    @Operation(summary = "Discover data patterns")
    public ResponseEntity<List<DataPattern>> discoverPatterns(
            @RequestParam String dataSource,
            @RequestParam String patternType,
            @RequestBody Map<String, Object> filters) {
        
        List<DataPattern> patterns = dataMiningService.discoverPatterns(dataSource, patternType, filters);
        return ResponseEntity.ok(patterns);
    }

    @PostMapping("/insights/generate")
    @Operation(summary = "Generate data insights")
    public ResponseEntity<List<DataInsight>> generateInsights(
            @RequestParam String dataSource,
            @RequestParam String insightType,
            @RequestBody Map<String, Object> parameters) {
        
        List<DataInsight> insights = dataMiningService.generateInsights(dataSource, insightType, parameters);
        return ResponseEntity.ok(insights);
    }

    @PostMapping("/models")
    @Operation(summary = "Create predictive model")
    public ResponseEntity<PredictiveModel> createPredictiveModel(
            @RequestParam String name,
            @RequestParam String modelType,
            @RequestParam String targetVariable,
            @RequestBody List<String> features,
            @RequestBody Map<String, Object> parameters,
            @RequestParam String userId) {
        
        PredictiveModel model = dataMiningService.createPredictiveModel(name, modelType, targetVariable, features, parameters, userId);
        return ResponseEntity.ok(model);
    }

    @PostMapping("/models/{modelId}/predict")
    @Operation(summary = "Make prediction using model")
    public ResponseEntity<ModelPrediction> makePrediction(
            @PathVariable String modelId,
            @RequestBody Map<String, Object> inputData) {
        
        ModelPrediction prediction = dataMiningService.makePrediction(modelId, inputData);
        return ResponseEntity.ok(prediction);
    }

    @PostMapping("/anomalies/detect")
    @Operation(summary = "Detect anomalies")
    public ResponseEntity<AnomalyDetection> detectAnomalies(
            @RequestParam String dataSource,
            @RequestParam String detectionType,
            @RequestBody Map<String, Object> parameters) {
        
        AnomalyDetection detection = dataMiningService.detectAnomalies(dataSource, detectionType, parameters);
        return ResponseEntity.ok(detection);
    }

    @PostMapping("/trends/analyze")
    @Operation(summary = "Analyze trends")
    public ResponseEntity<TrendAnalysis> analyzeTrends(
            @RequestParam String dataSource,
            @RequestParam String trendType,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        TrendAnalysis analysis = dataMiningService.analyzeTrends(dataSource, trendType, start, end);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/correlations/find")
    @Operation(summary = "Find correlations")
    public ResponseEntity<CorrelationAnalysis> findCorrelations(
            @RequestParam String dataSource,
            @RequestBody List<String> variables) {
        
        CorrelationAnalysis analysis = dataMiningService.findCorrelations(dataSource, variables);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/clustering/perform")
    @Operation(summary = "Perform clustering")
    public ResponseEntity<ClusteringResult> performClustering(
            @RequestParam String dataSource,
            @RequestParam String algorithm,
            @RequestBody Map<String, Object> parameters) {
        
        ClusteringResult result = dataMiningService.performClustering(dataSource, algorithm, parameters);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get mining analytics")
    public ResponseEntity<MiningAnalytics> getMiningAnalytics(@RequestParam String dataSource) {
        MiningAnalytics analytics = dataMiningService.getMiningAnalytics(dataSource);
        return ResponseEntity.ok(analytics);
    }

    @DeleteMapping("/jobs/{jobId}")
    @Operation(summary = "Delete mining job")
    public ResponseEntity<Void> deleteJob(@PathVariable String jobId) {
        dataMiningService.deleteJob(jobId);
        return ResponseEntity.ok().build();
    }
}


