package com.relief.controller.security;

import com.relief.service.security.SecurityAnalyticsService;
import com.relief.service.security.SecurityAnalyticsService.Metric;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/security/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Security Analytics", description = "Real-time security monitoring and metrics")
public class SecurityAnalyticsController {

    private final SecurityAnalyticsService analyticsService;

    @PostMapping("/track")
    @Operation(summary = "Track security metric")
    public ResponseEntity<Void> track(@RequestParam String name, @RequestParam double value, @RequestBody(required = false) Map<String, String> labels) {
        analyticsService.track(name, value, labels);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent metrics")
    public ResponseEntity<List<Metric>> recent(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(analyticsService.recent(limit));
    }

    @GetMapping("/aggregate")
    @Operation(summary = "Aggregate metrics by key label")
    public ResponseEntity<Map<String, Double>> aggregate(@RequestParam String name) {
        return ResponseEntity.ok(analyticsService.aggregate(name));
    }
}




