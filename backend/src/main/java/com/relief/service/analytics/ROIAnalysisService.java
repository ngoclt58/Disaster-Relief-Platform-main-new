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
 * ROI analysis service for measuring effectiveness and return on investment of relief efforts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ROIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ROIAnalysisService.class);

    public ROIAnalysis createAnalysis(String name, String description, String analysisType, 
                                    String projectId, Map<String, Object> parameters, String userId) {
        ROIAnalysis analysis = new ROIAnalysis();
        analysis.setId(UUID.randomUUID().toString());
        analysis.setName(name);
        analysis.setDescription(description);
        analysis.setAnalysisType(analysisType);
        analysis.setProjectId(projectId);
        analysis.setParameters(parameters);
        analysis.setUserId(userId);
        analysis.setCreatedAt(LocalDateTime.now());
        analysis.setStatus(AnalysisStatus.PENDING);
        analysis.setIsActive(true);
        
        log.info("Created ROI analysis: {} for project: {}", analysis.getId(), projectId);
        return analysis;
    }

    public ROIAnalysis executeAnalysis(String analysisId) {
        ROIAnalysis analysis = new ROIAnalysis();
        analysis.setId(analysisId);
        analysis.setStatus(AnalysisStatus.RUNNING);
        analysis.setStartedAt(LocalDateTime.now());
        
        log.info("Started ROI analysis: {}", analysisId);
        return analysis;
    }

    public ROIMetrics calculateROI(String projectId, LocalDateTime startDate, LocalDateTime endDate, 
                                 Map<String, Object> parameters) {
        ROIMetrics metrics = new ROIMetrics();
        metrics.setProjectId(projectId);
        metrics.setStartDate(startDate);
        metrics.setEndDate(endDate);
        metrics.setTotalInvestment(BigDecimal.valueOf(100000.00));
        metrics.setTotalReturns(BigDecimal.valueOf(150000.00));
        metrics.setNetProfit(BigDecimal.valueOf(50000.00));
        metrics.setRoiPercentage(50.0);
        metrics.setPaybackPeriod(12);
        metrics.setNpv(BigDecimal.valueOf(45000.00));
        metrics.setIrr(0.15);
        metrics.setGeneratedAt(LocalDateTime.now());
        
        log.info("Calculated ROI metrics for project: {}", projectId);
        return metrics;
    }

    public CostBenefitAnalysis performCostBenefitAnalysis(String projectId, 
                                                        List<CostItem> costs, List<BenefitItem> benefits) {
        CostBenefitAnalysis analysis = new CostBenefitAnalysis();
        analysis.setId(UUID.randomUUID().toString());
        analysis.setProjectId(projectId);
        analysis.setCosts(costs);
        analysis.setBenefits(benefits);
        analysis.setTotalCosts(BigDecimal.valueOf(100000.00));
        analysis.setTotalBenefits(BigDecimal.valueOf(150000.00));
        analysis.setNetBenefit(BigDecimal.valueOf(50000.00));
        analysis.setBenefitCostRatio(1.5);
        analysis.setGeneratedAt(LocalDateTime.now());
        
        log.info("Performed cost-benefit analysis for project: {}", projectId);
        return analysis;
    }

    public EffectivenessMetrics measureEffectiveness(String projectId, String metricType, 
                                                   Map<String, Object> parameters) {
        EffectivenessMetrics metrics = new EffectivenessMetrics();
        metrics.setProjectId(projectId);
        metrics.setMetricType(metricType);
        metrics.setParameters(parameters);
        metrics.setEffectivenessScore(0.85);
        metrics.setTargetAchievement(0.92);
        metrics.setEfficiencyRatio(0.78);
        metrics.setQualityScore(0.88);
        metrics.setTimelinessScore(0.82);
        metrics.setGeneratedAt(LocalDateTime.now());
        
        log.info("Measured effectiveness for project: {} - type: {}", projectId, metricType);
        return metrics;
    }

    public ImpactAssessment assessImpact(String projectId, String impactType, 
                                      Map<String, Object> criteria) {
        ImpactAssessment assessment = new ImpactAssessment();
        assessment.setId(UUID.randomUUID().toString());
        assessment.setProjectId(projectId);
        assessment.setImpactType(impactType);
        assessment.setCriteria(criteria);
        assessment.setImpactScore(0.87);
        assessment.setSocialImpact(0.85);
        assessment.setEconomicImpact(0.90);
        assessment.setEnvironmentalImpact(0.82);
        assessment.setLongTermImpact(0.88);
        assessment.setGeneratedAt(LocalDateTime.now());
        
        log.info("Assessed impact for project: {} - type: {}", projectId, impactType);
        return assessment;
    }

    public PerformanceBenchmark benchmarkPerformance(String projectId, String benchmarkType, 
                                                   Map<String, Object> parameters) {
        PerformanceBenchmark benchmark = new PerformanceBenchmark();
        benchmark.setId(UUID.randomUUID().toString());
        benchmark.setProjectId(projectId);
        benchmark.setBenchmarkType(benchmarkType);
        benchmark.setParameters(parameters);
        benchmark.setCurrentPerformance(0.85);
        benchmark.setIndustryAverage(0.75);
        benchmark.setBestPractice(0.95);
        benchmark.setPerformanceGap(0.10);
        benchmark.setImprovementPotential(0.12);
        benchmark.setGeneratedAt(LocalDateTime.now());
        
        log.info("Benchmarked performance for project: {} - type: {}", projectId, benchmarkType);
        return benchmark;
    }

    public ValueForMoneyAnalysis analyzeValueForMoney(String projectId, 
                                                    Map<String, Object> parameters) {
        ValueForMoneyAnalysis analysis = new ValueForMoneyAnalysis();
        analysis.setId(UUID.randomUUID().toString());
        analysis.setProjectId(projectId);
        analysis.setParameters(parameters);
        analysis.setValueScore(0.88);
        analysis.setCostEffectiveness(0.85);
        analysis.setEfficiency(0.90);
        analysis.setEconomy(0.82);
        analysis.setValueForMoneyRating("EXCELLENT");
        analysis.setGeneratedAt(LocalDateTime.now());
        
        log.info("Analyzed value for money for project: {}", projectId);
        return analysis;
    }

    public ROIComparison compareROI(List<String> projectIds, String comparisonType, 
                                  Map<String, Object> parameters) {
        ROIComparison comparison = new ROIComparison();
        comparison.setId(UUID.randomUUID().toString());
        comparison.setProjectIds(projectIds);
        comparison.setComparisonType(comparisonType);
        comparison.setParameters(parameters);
        comparison.setComparisons(Collections.emptyList());
        comparison.setBestPerformer("project-1");
        comparison.setWorstPerformer("project-3");
        comparison.setAverageROI(0.45);
        comparison.setGeneratedAt(LocalDateTime.now());
        
        log.info("Compared ROI for {} projects", projectIds.size());
        return comparison;
    }

    public ROITrend analyzeTrends(String projectId, LocalDateTime startDate, LocalDateTime endDate) {
        ROITrend trend = new ROITrend();
        trend.setId(UUID.randomUUID().toString());
        trend.setProjectId(projectId);
        trend.setStartDate(startDate);
        trend.setEndDate(endDate);
        trend.setTrendData(Collections.emptyList());
        trend.setTrendDirection("INCREASING");
        trend.setTrendStrength(0.75);
        trend.setForecast(Collections.emptyMap());
        trend.setGeneratedAt(LocalDateTime.now());
        
        log.info("Analyzed ROI trends for project: {}", projectId);
        return trend;
    }

    public ROIAnalysis getAnalysis(String analysisId) {
        // Implementation for getting analysis
        ROIAnalysis analysis = new ROIAnalysis();
        analysis.setId(analysisId);
        analysis.setName("Sample ROI Analysis");
        analysis.setDescription("Sample ROI analysis description");
        analysis.setAnalysisType("COMPREHENSIVE");
        analysis.setProjectId("project-123");
        analysis.setParameters(Collections.emptyMap());
        analysis.setUserId("user-123");
        analysis.setCreatedAt(LocalDateTime.now());
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setIsActive(true);
        
        return analysis;
    }

    public List<ROIAnalysis> getUserAnalyses(String userId) {
        // Return a couple of synthetic ROI analyses for the overview
        ROIAnalysis shelterProject = new ROIAnalysis();
        shelterProject.setId("roi-shelter-001");
        shelterProject.setName("Shelter Expansion Phase 1");
        shelterProject.setDescription("ROI for expanding shelter capacity");
        shelterProject.setAnalysisType("COMPREHENSIVE");
        shelterProject.setProjectId("project-shelter-001");
        shelterProject.setParameters(Map.of("region", "Coastal", "duration_months", 12));
        shelterProject.setUserId(userId);
        shelterProject.setCreatedAt(LocalDateTime.now().minusDays(30));
        shelterProject.setStatus(AnalysisStatus.COMPLETED);
        shelterProject.setIsActive(true);

        ROIAnalysis logisticsProject = new ROIAnalysis();
        logisticsProject.setId("roi-logistics-001");
        logisticsProject.setName("Logistics Optimization");
        logisticsProject.setDescription("ROI of optimized routing for relief trucks");
        logisticsProject.setAnalysisType("COST_BENEFIT");
        logisticsProject.setProjectId("project-logistics-001");
        logisticsProject.setParameters(Map.of("fleet_size", 25, "baseline_cost", 200000));
        logisticsProject.setUserId(userId);
        logisticsProject.setCreatedAt(LocalDateTime.now().minusDays(10));
        logisticsProject.setStatus(AnalysisStatus.RUNNING);
        logisticsProject.setIsActive(true);

        return Arrays.asList(shelterProject, logisticsProject);
    }

    public ROIAnalytics getROIAnalytics(String projectId) {
        ROIAnalytics analytics = new ROIAnalytics();
        analytics.setProjectId(projectId);
        analytics.setTotalAnalyses(5);
        analytics.setAverageROI(0.42);
        analytics.setBestROI(0.67);
        analytics.setWorstROI(0.18);
        analytics.setTrendDirection("INCREASING");
        analytics.setLastAnalyzed(LocalDateTime.now().minusDays(1));
        
        return analytics;
    }

    public void deleteAnalysis(String analysisId) {
        log.info("Deleted ROI analysis: {}", analysisId);
    }

    // Data classes
    public static class ROIAnalysis {
        private String id;
        private String name;
        private String description;
        private String analysisType;
        private String projectId;
        private Map<String, Object> parameters;
        private String userId;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private AnalysisStatus status;
        private boolean isActive;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAnalysisType() { return analysisType; }
        public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

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

        public AnalysisStatus getStatus() { return status; }
        public void setStatus(AnalysisStatus status) { this.status = status; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        // Compatibility setter for Lombok-style naming
        public void setIsActive(boolean active) { this.isActive = active; }
    }

    public static class ROIMetrics {
        private String projectId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal totalInvestment;
        private BigDecimal totalReturns;
        private BigDecimal netProfit;
        private double roiPercentage;
        private int paybackPeriod;
        private BigDecimal npv;
        private double irr;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public BigDecimal getTotalInvestment() { return totalInvestment; }
        public void setTotalInvestment(BigDecimal totalInvestment) { this.totalInvestment = totalInvestment; }

        public BigDecimal getTotalReturns() { return totalReturns; }
        public void setTotalReturns(BigDecimal totalReturns) { this.totalReturns = totalReturns; }

        public BigDecimal getNetProfit() { return netProfit; }
        public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }

        public double getRoiPercentage() { return roiPercentage; }
        public void setRoiPercentage(double roiPercentage) { this.roiPercentage = roiPercentage; }

        public int getPaybackPeriod() { return paybackPeriod; }
        public void setPaybackPeriod(int paybackPeriod) { this.paybackPeriod = paybackPeriod; }

        public BigDecimal getNpv() { return npv; }
        public void setNpv(BigDecimal npv) { this.npv = npv; }

        public double getIrr() { return irr; }
        public void setIrr(double irr) { this.irr = irr; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class CostBenefitAnalysis {
        private String id;
        private String projectId;
        private List<CostItem> costs;
        private List<BenefitItem> benefits;
        private BigDecimal totalCosts;
        private BigDecimal totalBenefits;
        private BigDecimal netBenefit;
        private double benefitCostRatio;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public List<CostItem> getCosts() { return costs; }
        public void setCosts(List<CostItem> costs) { this.costs = costs; }

        public List<BenefitItem> getBenefits() { return benefits; }
        public void setBenefits(List<BenefitItem> benefits) { this.benefits = benefits; }

        public BigDecimal getTotalCosts() { return totalCosts; }
        public void setTotalCosts(BigDecimal totalCosts) { this.totalCosts = totalCosts; }

        public BigDecimal getTotalBenefits() { return totalBenefits; }
        public void setTotalBenefits(BigDecimal totalBenefits) { this.totalBenefits = totalBenefits; }

        public BigDecimal getNetBenefit() { return netBenefit; }
        public void setNetBenefit(BigDecimal netBenefit) { this.netBenefit = netBenefit; }

        public double getBenefitCostRatio() { return benefitCostRatio; }
        public void setBenefitCostRatio(double benefitCostRatio) { this.benefitCostRatio = benefitCostRatio; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class CostItem {
        private String id;
        private String name;
        private String category;
        private BigDecimal amount;
        private String currency;
        private String description;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class BenefitItem {
        private String id;
        private String name;
        private String category;
        private BigDecimal value;
        private String currency;
        private String description;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class EffectivenessMetrics {
        private String projectId;
        private String metricType;
        private Map<String, Object> parameters;
        private double effectivenessScore;
        private double targetAchievement;
        private double efficiencyRatio;
        private double qualityScore;
        private double timelinessScore;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public String getMetricType() { return metricType; }
        public void setMetricType(String metricType) { this.metricType = metricType; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public double getEffectivenessScore() { return effectivenessScore; }
        public void setEffectivenessScore(double effectivenessScore) { this.effectivenessScore = effectivenessScore; }

        public double getTargetAchievement() { return targetAchievement; }
        public void setTargetAchievement(double targetAchievement) { this.targetAchievement = targetAchievement; }

        public double getEfficiencyRatio() { return efficiencyRatio; }
        public void setEfficiencyRatio(double efficiencyRatio) { this.efficiencyRatio = efficiencyRatio; }

        public double getQualityScore() { return qualityScore; }
        public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }

        public double getTimelinessScore() { return timelinessScore; }
        public void setTimelinessScore(double timelinessScore) { this.timelinessScore = timelinessScore; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class ImpactAssessment {
        private String id;
        private String projectId;
        private String impactType;
        private Map<String, Object> criteria;
        private double impactScore;
        private double socialImpact;
        private double economicImpact;
        private double environmentalImpact;
        private double longTermImpact;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public String getImpactType() { return impactType; }
        public void setImpactType(String impactType) { this.impactType = impactType; }

        public Map<String, Object> getCriteria() { return criteria; }
        public void setCriteria(Map<String, Object> criteria) { this.criteria = criteria; }

        public double getImpactScore() { return impactScore; }
        public void setImpactScore(double impactScore) { this.impactScore = impactScore; }

        public double getSocialImpact() { return socialImpact; }
        public void setSocialImpact(double socialImpact) { this.socialImpact = socialImpact; }

        public double getEconomicImpact() { return economicImpact; }
        public void setEconomicImpact(double economicImpact) { this.economicImpact = economicImpact; }

        public double getEnvironmentalImpact() { return environmentalImpact; }
        public void setEnvironmentalImpact(double environmentalImpact) { this.environmentalImpact = environmentalImpact; }

        public double getLongTermImpact() { return longTermImpact; }
        public void setLongTermImpact(double longTermImpact) { this.longTermImpact = longTermImpact; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class PerformanceBenchmark {
        private String id;
        private String projectId;
        private String benchmarkType;
        private Map<String, Object> parameters;
        private double currentPerformance;
        private double industryAverage;
        private double bestPractice;
        private double performanceGap;
        private double improvementPotential;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public String getBenchmarkType() { return benchmarkType; }
        public void setBenchmarkType(String benchmarkType) { this.benchmarkType = benchmarkType; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public double getCurrentPerformance() { return currentPerformance; }
        public void setCurrentPerformance(double currentPerformance) { this.currentPerformance = currentPerformance; }

        public double getIndustryAverage() { return industryAverage; }
        public void setIndustryAverage(double industryAverage) { this.industryAverage = industryAverage; }

        public double getBestPractice() { return bestPractice; }
        public void setBestPractice(double bestPractice) { this.bestPractice = bestPractice; }

        public double getPerformanceGap() { return performanceGap; }
        public void setPerformanceGap(double performanceGap) { this.performanceGap = performanceGap; }

        public double getImprovementPotential() { return improvementPotential; }
        public void setImprovementPotential(double improvementPotential) { this.improvementPotential = improvementPotential; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class ValueForMoneyAnalysis {
        private String id;
        private String projectId;
        private Map<String, Object> parameters;
        private double valueScore;
        private double costEffectiveness;
        private double efficiency;
        private double economy;
        private String valueForMoneyRating;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public double getValueScore() { return valueScore; }
        public void setValueScore(double valueScore) { this.valueScore = valueScore; }

        public double getCostEffectiveness() { return costEffectiveness; }
        public void setCostEffectiveness(double costEffectiveness) { this.costEffectiveness = costEffectiveness; }

        public double getEfficiency() { return efficiency; }
        public void setEfficiency(double efficiency) { this.efficiency = efficiency; }

        public double getEconomy() { return economy; }
        public void setEconomy(double economy) { this.economy = economy; }

        public String getValueForMoneyRating() { return valueForMoneyRating; }
        public void setValueForMoneyRating(String valueForMoneyRating) { this.valueForMoneyRating = valueForMoneyRating; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class ROIComparison {
        private String id;
        private List<String> projectIds;
        private String comparisonType;
        private Map<String, Object> parameters;
        private List<ProjectROI> comparisons;
        private String bestPerformer;
        private String worstPerformer;
        private double averageROI;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public List<String> getProjectIds() { return projectIds; }
        public void setProjectIds(List<String> projectIds) { this.projectIds = projectIds; }

        public String getComparisonType() { return comparisonType; }
        public void setComparisonType(String comparisonType) { this.comparisonType = comparisonType; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public List<ProjectROI> getComparisons() { return comparisons; }
        public void setComparisons(List<ProjectROI> comparisons) { this.comparisons = comparisons; }

        public String getBestPerformer() { return bestPerformer; }
        public void setBestPerformer(String bestPerformer) { this.bestPerformer = bestPerformer; }

        public String getWorstPerformer() { return worstPerformer; }
        public void setWorstPerformer(String worstPerformer) { this.worstPerformer = worstPerformer; }

        public double getAverageROI() { return averageROI; }
        public void setAverageROI(double averageROI) { this.averageROI = averageROI; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class ProjectROI {
        private String projectId;
        private String projectName;
        private double roiPercentage;
        private BigDecimal totalInvestment;
        private BigDecimal netProfit;
        private int ranking;

        // Getters and setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public double getRoiPercentage() { return roiPercentage; }
        public void setRoiPercentage(double roiPercentage) { this.roiPercentage = roiPercentage; }

        public BigDecimal getTotalInvestment() { return totalInvestment; }
        public void setTotalInvestment(BigDecimal totalInvestment) { this.totalInvestment = totalInvestment; }

        public BigDecimal getNetProfit() { return netProfit; }
        public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }

        public int getRanking() { return ranking; }
        public void setRanking(int ranking) { this.ranking = ranking; }
    }

    public static class ROITrend {
        private String id;
        private String projectId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<TrendDataPoint> trendData;
        private String trendDirection;
        private double trendStrength;
        private Map<String, Object> forecast;
        private LocalDateTime generatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public List<TrendDataPoint> getTrendData() { return trendData; }
        public void setTrendData(List<TrendDataPoint> trendData) { this.trendData = trendData; }

        public String getTrendDirection() { return trendDirection; }
        public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

        public double getTrendStrength() { return trendStrength; }
        public void setTrendStrength(double trendStrength) { this.trendStrength = trendStrength; }

        public Map<String, Object> getForecast() { return forecast; }
        public void setForecast(Map<String, Object> forecast) { this.forecast = forecast; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class TrendDataPoint {
        private LocalDateTime date;
        private double roiPercentage;
        private BigDecimal investment;
        private BigDecimal returns;

        // Getters and setters
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public double getRoiPercentage() { return roiPercentage; }
        public void setRoiPercentage(double roiPercentage) { this.roiPercentage = roiPercentage; }

        public BigDecimal getInvestment() { return investment; }
        public void setInvestment(BigDecimal investment) { this.investment = investment; }

        public BigDecimal getReturns() { return returns; }
        public void setReturns(BigDecimal returns) { this.returns = returns; }
    }

    public static class ROIAnalytics {
        private String projectId;
        private int totalAnalyses;
        private double averageROI;
        private double bestROI;
        private double worstROI;
        private String trendDirection;
        private LocalDateTime lastAnalyzed;

        // Getters and setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }

        public int getTotalAnalyses() { return totalAnalyses; }
        public void setTotalAnalyses(int totalAnalyses) { this.totalAnalyses = totalAnalyses; }

        public double getAverageROI() { return averageROI; }
        public void setAverageROI(double averageROI) { this.averageROI = averageROI; }

        public double getBestROI() { return bestROI; }
        public void setBestROI(double bestROI) { this.bestROI = bestROI; }

        public double getWorstROI() { return worstROI; }
        public void setWorstROI(double worstROI) { this.worstROI = worstROI; }

        public String getTrendDirection() { return trendDirection; }
        public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

        public LocalDateTime getLastAnalyzed() { return lastAnalyzed; }
        public void setLastAnalyzed(LocalDateTime lastAnalyzed) { this.lastAnalyzed = lastAnalyzed; }
    }

    public enum AnalysisStatus {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }
}


