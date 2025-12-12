package com.relief.controller.realtime;

import com.relief.service.realtime.StreamProcessingService;
import com.relief.service.realtime.StreamProcessingService.StreamProcessor;
import com.relief.service.realtime.StreamProcessingService.StreamRule;
import com.relief.service.realtime.StreamProcessingService.StreamData;
import com.relief.service.realtime.StreamProcessingService.StreamMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Stream processing controller for real-time analysis of incoming data streams
 */
@RestController
@RequestMapping("/api/realtime/stream-processing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stream Processing", description = "Real-time analysis of incoming data streams")
public class StreamProcessingController {

    private final StreamProcessingService streamProcessingService;

    @PostMapping("/processors")
    @Operation(summary = "Create stream processor")
    public ResponseEntity<StreamProcessor> createProcessor(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String dataSource,
            @RequestBody Map<String, Object> configuration) {
        
        StreamProcessor processor = streamProcessingService.createProcessor(name, description, dataSource, configuration);
        return ResponseEntity.ok(processor);
    }

    @PostMapping("/processors/{processorId}/start")
    @Operation(summary = "Start stream processor")
    public ResponseEntity<Void> startProcessor(@PathVariable String processorId) {
        streamProcessingService.startProcessor(processorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/processors/{processorId}/stop")
    @Operation(summary = "Stop stream processor")
    public ResponseEntity<Void> stopProcessor(@PathVariable String processorId) {
        streamProcessingService.stopProcessor(processorId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/processors/{processorId}")
    @Operation(summary = "Get stream processor")
    public ResponseEntity<StreamProcessor> getProcessor(@PathVariable String processorId) {
        StreamProcessor processor = streamProcessingService.getProcessor(processorId);
        return ResponseEntity.ok(processor);
    }

    @GetMapping("/processors")
    @Operation(summary = "Get all stream processors")
    public ResponseEntity<List<StreamProcessor>> getProcessors() {
        List<StreamProcessor> processors = streamProcessingService.getProcessors();
        return ResponseEntity.ok(processors);
    }

    @PostMapping("/processors/{processorId}/rules")
    @Operation(summary = "Add stream rule")
    public ResponseEntity<Void> addStreamRule(
            @PathVariable String processorId,
            @RequestBody StreamRule rule) {
        
        streamProcessingService.addStreamRule(processorId, rule);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/processors/{processorId}/rules/{ruleId}")
    @Operation(summary = "Remove stream rule")
    public ResponseEntity<Void> removeStreamRule(
            @PathVariable String processorId,
            @PathVariable String ruleId) {
        
        streamProcessingService.removeStreamRule(processorId, ruleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/processors/{processorId}/process")
    @Operation(summary = "Process stream data")
    public ResponseEntity<StreamData> processData(
            @PathVariable String processorId,
            @RequestBody Map<String, Object> data) {
        
        StreamData streamData = streamProcessingService.processData(processorId, data);
        return ResponseEntity.ok(streamData);
    }

    @GetMapping("/processors/{processorId}/metrics")
    @Operation(summary = "Get processor metrics")
    public ResponseEntity<StreamMetrics> getProcessorMetrics(@PathVariable String processorId) {
        StreamMetrics metrics = streamProcessingService.getProcessorMetrics(processorId);
        return ResponseEntity.ok(metrics);
    }

    @DeleteMapping("/processors/{processorId}")
    @Operation(summary = "Delete stream processor")
    public ResponseEntity<Void> deleteProcessor(@PathVariable String processorId) {
        streamProcessingService.deleteProcessor(processorId);
        return ResponseEntity.ok().build();
    }
}


