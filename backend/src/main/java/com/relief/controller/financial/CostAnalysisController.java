package com.relief.controller.financial;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.financial.CostAnalysisService;
import com.relief.service.financial.CostAnalysisService.CostAnalysis;
import com.relief.service.financial.CostAnalysisService.CostBreakdown;
import com.relief.service.financial.CostAnalysisService.CostOptimization;
import com.relief.service.financial.CostAnalysisService.CostTrend;
import com.relief.service.financial.CostAnalysisService.CostDriver;
import com.relief.service.financial.CostAnalysisService.CostCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cost analysis controller
 */
@RestController
@RequestMapping("/cost-analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cost Analysis", description = "Cost analysis and optimization APIs")
public class CostAnalysisController {

    private final CostAnalysisService costAnalysisService;
    private final UserRepository userRepository;

    private UUID getUserIdFromPrincipal(UserDetails principal) {
        String username = principal.getUsername();
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByPhone(username)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username)));
            return user.getId();
        }
    }

    @PostMapping("/analyze")
    @Operation(summary = "Perform cost analysis")
    public ResponseEntity<CostAnalysis> performCostAnalysis(
            @RequestBody CostAnalysisRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        CostAnalysis analysis = costAnalysisService.performCostAnalysis(
            request.getCategory(),
            request.getStartDate(),
            request.getEndDate(),
            request.getFilters(),
            userId
        );
        
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/breakdown/{category}")
    @Operation(summary = "Get cost breakdown by category")
    public ResponseEntity<CostBreakdown> getCostBreakdown(
            @PathVariable String category,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        CostBreakdown breakdown = costAnalysisService.getCostBreakdown(
            category, startDate, endDate
        );
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/trends/{category}")
    @Operation(summary = "Get cost trends for category")
    public ResponseEntity<List<CostTrend>> getCostTrends(
            @PathVariable String category,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "30") int days) {
        
        List<CostTrend> trends = costAnalysisService.getCostTrends(
            category, startDate, endDate, days
        );
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/drivers")
    @Operation(summary = "Get cost drivers")
    public ResponseEntity<List<CostDriver>> getCostDrivers(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        List<CostDriver> drivers = costAnalysisService.getCostDrivers(
            category, startDate, endDate
        );
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get cost categories")
    public ResponseEntity<List<CostCategory>> getCostCategories() {
        List<CostCategory> categories = costAnalysisService.getCostCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/optimize")
    @Operation(summary = "Get cost optimization recommendations")
    public ResponseEntity<CostOptimization> getCostOptimization(
            @RequestBody CostOptimizationRequest request) {
        
        CostOptimization optimization = costAnalysisService.getCostOptimization(
            request.getCategory(),
            request.getBudget(),
            request.getConstraints()
        );
        
        return ResponseEntity.ok(optimization);
    }

    @GetMapping("/comparison")
    @Operation(summary = "Compare costs across periods")
    public ResponseEntity<Map<String, Object>> compareCosts(
            @RequestParam String category,
            @RequestParam LocalDateTime period1Start,
            @RequestParam LocalDateTime period1End,
            @RequestParam LocalDateTime period2Start,
            @RequestParam LocalDateTime period2End) {
        
        Map<String, Object> comparison = costAnalysisService.compareCosts(
            category, period1Start, period1End, period2Start, period2End
        );
        
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get cost forecast")
    public ResponseEntity<Map<String, Object>> getCostForecast(
            @RequestParam String category,
            @RequestParam int months) {
        
        Map<String, Object> forecast = costAnalysisService.getCostForecast(category, months);
        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/variance")
    @Operation(summary = "Get cost variance analysis")
    public ResponseEntity<Map<String, Object>> getCostVariance(
            @RequestParam String category,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        
        Map<String, Object> variance = costAnalysisService.getCostVariance(
            category, startDate, endDate
        );
        
        return ResponseEntity.ok(variance);
    }

    @PostMapping("/benchmark")
    @Operation(summary = "Benchmark costs against industry standards")
    public ResponseEntity<Map<String, Object>> benchmarkCosts(
            @RequestBody BenchmarkRequest request) {
        
        Map<String, Object> benchmark = costAnalysisService.benchmarkCosts(
            request.getCategory(),
            request.getRegion(),
            request.getOrganizationSize()
        );
        
        return ResponseEntity.ok(benchmark);
    }

    @GetMapping("/efficiency")
    @Operation(summary = "Get cost efficiency metrics")
    public ResponseEntity<Map<String, Object>> getCostEfficiency(
            @RequestParam String category,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        Map<String, Object> efficiency = costAnalysisService.getCostEfficiency(
            category, startDate, endDate
        );
        
        return ResponseEntity.ok(efficiency);
    }

    // Request DTOs
    public static class CostAnalysisRequest {
        private String category;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Map<String, Object> filters;

        // Getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public Map<String, Object> getFilters() { return filters; }
        public void setFilters(Map<String, Object> filters) { this.filters = filters; }
    }

    public static class CostOptimizationRequest {
        private String category;
        private BigDecimal budget;
        private Map<String, Object> constraints;

        // Getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getBudget() { return budget; }
        public void setBudget(BigDecimal budget) { this.budget = budget; }

        public Map<String, Object> getConstraints() { return constraints; }
        public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }
    }

    public static class BenchmarkRequest {
        private String category;
        private String region;
        private String organizationSize;

        // Getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getOrganizationSize() { return organizationSize; }
        public void setOrganizationSize(String organizationSize) { this.organizationSize = organizationSize; }
    }
}


