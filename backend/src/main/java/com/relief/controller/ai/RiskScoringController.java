package com.relief.controller.ai;

import com.relief.service.ai.RiskScoringService;
import com.relief.service.ai.RiskScoringService.RiskScore;
import com.relief.service.ai.RiskScoringService.RiskInput;
import com.relief.service.ai.RiskScoringService.RiskComparison;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ai/risk-scoring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Risk Scoring", description = "Automated risk assessment for different areas")
public class RiskScoringController {

    private final RiskScoringService riskScoringService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate risk score")
    public ResponseEntity<RiskScore> calculateRiskScore(@RequestBody RiskInput input) {
        RiskScore score = riskScoringService.calculateRiskScore(input);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/scores")
    @Operation(summary = "Get risk scores")
    public ResponseEntity<List<RiskScore>> getRiskScores(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<RiskScore> scores = riskScoringService.getRiskScores(start, end);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/scores/{scoreId}")
    @Operation(summary = "Get risk score")
    public ResponseEntity<RiskScore> getRiskScore(@PathVariable String scoreId) {
        RiskScore score = riskScoringService.getRiskScore(scoreId);
        return ResponseEntity.ok(score);
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare risk scores")
    public ResponseEntity<RiskComparison> compareRiskScores(@RequestBody List<String> scoreIds) {
        RiskComparison comparison = riskScoringService.compareRiskScores(scoreIds);
        return ResponseEntity.ok(comparison);
    }
}



