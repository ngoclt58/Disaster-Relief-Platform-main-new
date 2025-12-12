package com.relief.controller.ai;

import com.relief.service.ai.ResourceDemandForecastingService;
import com.relief.service.ai.ResourceDemandForecastingService.ForecastingModel;
import com.relief.service.ai.ResourceDemandForecastingService.DemandForecast;
import com.relief.service.ai.ResourceDemandForecastingService.ForecastInput;
import com.relief.service.ai.ResourceDemandForecastingService.HistoricalDemand;
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
@RequestMapping("/api/ai/resource-forecasting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resource Demand Forecasting", description = "AI-powered prediction of resource needs")
public class ResourceForecastingController {

    private final ResourceDemandForecastingService forecastingService;

    @PostMapping("/models")
    @Operation(summary = "Create forecasting model")
    public ResponseEntity<ForecastingModel> createModel(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String resourceType,
            @RequestBody Map<String, Object> parameters) {
        
        ForecastingModel model = forecastingService.createModel(name, description, resourceType, parameters);
        return ResponseEntity.ok(model);
    }

    @PostMapping("/models/{modelId}/train")
    @Operation(summary = "Train forecasting model")
    public ResponseEntity<Void> trainModel(
            @PathVariable String modelId,
            @RequestBody List<HistoricalDemand> trainingData) {
        
        forecastingService.trainModel(modelId, trainingData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/models/{modelId}/forecast")
    @Operation(summary = "Generate demand forecast")
    public ResponseEntity<DemandForecast> forecastDemand(
            @PathVariable String modelId,
            @RequestBody ForecastInput input) {
        
        DemandForecast forecast = forecastingService.forecastDemand(modelId, input);
        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/models/{modelId}/forecasts")
    @Operation(summary = "Get forecasts")
    public ResponseEntity<List<DemandForecast>> getForecasts(
            @PathVariable String modelId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<DemandForecast> forecasts = forecastingService.getForecasts(modelId, start, end);
        return ResponseEntity.ok(forecasts);
    }

    @GetMapping("/models/{modelId}")
    @Operation(summary = "Get model")
    public ResponseEntity<ForecastingModel> getModel(@PathVariable String modelId) {
        ForecastingModel model = forecastingService.getModel(modelId);
        return ResponseEntity.ok(model);
    }

    @GetMapping("/models")
    @Operation(summary = "Get all models")
    public ResponseEntity<List<ForecastingModel>> getModels() {
        List<ForecastingModel> models = forecastingService.getModels();
        return ResponseEntity.ok(models);
    }
}

