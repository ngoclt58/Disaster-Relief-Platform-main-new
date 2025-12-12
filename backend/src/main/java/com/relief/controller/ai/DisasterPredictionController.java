package com.relief.controller.ai;

import com.relief.service.ai.DisasterPredictionService;
import com.relief.service.ai.DisasterPredictionService.DisasterPredictionModel;
import com.relief.service.ai.DisasterPredictionService.DisasterPrediction;
import com.relief.service.ai.DisasterPredictionService.PredictionInput;
import com.relief.service.ai.DisasterPredictionService.HistoricalData;
import com.relief.service.ai.DisasterPredictionService.TestData;
import com.relief.service.ai.DisasterPredictionService.PredictionEvaluation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/disaster-prediction")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Disaster Prediction", description = "ML models to predict disaster likelihood")
public class DisasterPredictionController {

    private final DisasterPredictionService disasterPredictionService;

    @PostMapping("/models")
    @Operation(summary = "Create disaster prediction model")
    public ResponseEntity<DisasterPredictionModel> createModel(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String disasterType,
            @RequestBody Map<String, Object> features,
            @RequestBody Map<String, Object> parameters) {
        
        DisasterPredictionModel model = disasterPredictionService.createModel(name, description, disasterType, features, parameters);
        return ResponseEntity.ok(model);
    }

    @PostMapping("/models/{modelId}/train")
    @Operation(summary = "Train prediction model")
    public ResponseEntity<Void> trainModel(
            @PathVariable String modelId,
            @RequestBody List<HistoricalData> trainingData) {
        
        disasterPredictionService.trainModel(modelId, trainingData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/models/{modelId}/predict")
    @Operation(summary = "Make disaster prediction")
    public ResponseEntity<DisasterPrediction> predictDisaster(
            @PathVariable String modelId,
            @RequestBody PredictionInput input) {
        
        DisasterPrediction prediction = disasterPredictionService.predictDisaster(modelId, input);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/models/{modelId}/predictions")
    @Operation(summary = "Get predictions")
    public ResponseEntity<List<DisasterPrediction>> getPredictions(
            @PathVariable String modelId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<DisasterPrediction> predictions = disasterPredictionService.getPredictions(modelId, start, end);
        return ResponseEntity.ok(predictions);
    }

    @PostMapping("/models/{modelId}/evaluate")
    @Operation(summary = "Evaluate model performance")
    public ResponseEntity<PredictionEvaluation> evaluateModel(
            @PathVariable String modelId,
            @RequestBody List<TestData> testData) {
        
        PredictionEvaluation evaluation = disasterPredictionService.evaluateModel(modelId, testData);
        return ResponseEntity.ok(evaluation);
    }

    @GetMapping("/models/{modelId}")
    @Operation(summary = "Get model")
    public ResponseEntity<DisasterPredictionModel> getModel(@PathVariable String modelId) {
        DisasterPredictionModel model = disasterPredictionService.getModel(modelId);
        return ResponseEntity.ok(model);
    }

    @GetMapping("/models")
    @Operation(summary = "Get all models")
    public ResponseEntity<List<DisasterPredictionModel>> getModels() {
        List<DisasterPredictionModel> models = disasterPredictionService.getModels();
        return ResponseEntity.ok(models);
    }
}



