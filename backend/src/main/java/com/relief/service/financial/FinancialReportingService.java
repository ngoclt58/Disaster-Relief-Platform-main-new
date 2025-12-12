package com.relief.service.financial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for comprehensive financial reporting and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReportingService {

    private static final Logger log = LoggerFactory.getLogger(FinancialReportingService.class);

    private final Map<String, FinancialReport> reports = new ConcurrentHashMap<>();
    private final Map<String, List<FinancialMetric>> metrics = new ConcurrentHashMap<>();
    private final Map<String, List<FinancialAlert>> alerts = new ConcurrentHashMap<>();

    /**
     * Generate comprehensive financial report
     */
    @Transactional(readOnly = true)
    public FinancialReport generateFinancialReport(String reportType, String period,
                                                 LocalDateTime startDate, LocalDateTime endDate,
                                                 String[] budgetIds) {
        log.info("Generating financial report: {} for period: {} to {}", reportType, startDate, endDate);
        
        FinancialReport report = new FinancialReport();
        report.setId(UUID.randomUUID().toString());
        report.setReportType(reportType);
        report.setPeriod(period);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());
        report.setStatus(ReportStatus.GENERATED);
        
        // Generate report sections
        report.setExecutiveSummary(generateExecutiveSummary(budgetIds, startDate, endDate));
        report.setBudgetSummary(generateBudgetSummary(budgetIds, startDate, endDate));
        report.setDonationSummary(generateDonationSummary(budgetIds, startDate, endDate));
        report.setCostAnalysis(generateCostAnalysis(budgetIds, startDate, endDate));
        report.setFinancialMetrics(generateFinancialMetrics(budgetIds, startDate, endDate));
        report.setTrends(generateTrends(budgetIds, startDate, endDate));
        report.setAlerts(generateAlerts(budgetIds, startDate, endDate));
        report.setRecommendations(generateRecommendations(budgetIds, startDate, endDate));
        
        reports.put(report.getId(), report);
        
        log.info("Financial report generated: {} with ID: {}", reportType, report.getId());
        return report;
    }

    /**
     * Generate executive summary
     */
    private ExecutiveSummary generateExecutiveSummary(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        ExecutiveSummary summary = new ExecutiveSummary();
        summary.setPeriod(startDate + " to " + endDate);
        summary.setTotalBudgets(budgetIds.length);
        summary.setTotalAllocatedAmount(calculateTotalAllocatedAmount(budgetIds));
        summary.setTotalSpentAmount(calculateTotalSpentAmount(budgetIds, startDate, endDate));
        summary.setTotalDonations(calculateTotalDonations(budgetIds, startDate, endDate));
        summary.setBudgetUtilization(calculateOverallBudgetUtilization(budgetIds));
        summary.setKeyHighlights(generateKeyHighlights(budgetIds, startDate, endDate));
        summary.setCriticalIssues(generateCriticalIssues(budgetIds, startDate, endDate));
        
        return summary;
    }

    /**
     * Generate budget summary
     */
    private BudgetSummary generateBudgetSummary(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        BudgetSummary summary = new BudgetSummary();
        summary.setTotalBudgets(budgetIds.length);
        summary.setActiveBudgets(calculateActiveBudgets(budgetIds));
        summary.setClosedBudgets(calculateClosedBudgets(budgetIds));
        summary.setTotalAllocatedAmount(calculateTotalAllocatedAmount(budgetIds));
        summary.setTotalSpentAmount(calculateTotalSpentAmount(budgetIds, startDate, endDate));
        summary.setTotalRemainingAmount(calculateTotalRemainingAmount(budgetIds));
        summary.setAverageBudgetSize(calculateAverageBudgetSize(budgetIds));
        summary.setBudgetUtilizationRate(calculateOverallBudgetUtilization(budgetIds));
        summary.setTopSpendingCategories(calculateTopSpendingCategories(budgetIds, startDate, endDate));
        summary.setBudgetAlerts(calculateBudgetAlerts(budgetIds));
        
        return summary;
    }

    /**
     * Generate donation summary
     */
    private DonationSummary generateDonationSummary(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        DonationSummary summary = new DonationSummary();
        summary.setTotalDonations(calculateTotalDonationCount(budgetIds, startDate, endDate));
        summary.setTotalDonationAmount(calculateTotalDonations(budgetIds, startDate, endDate));
        summary.setAverageDonationAmount(calculateAverageDonationAmount(budgetIds, startDate, endDate));
        summary.setConfirmedDonations(calculateConfirmedDonations(budgetIds, startDate, endDate));
        summary.setPendingDonations(calculatePendingDonations(budgetIds, startDate, endDate));
        summary.setTopDonors(calculateTopDonors(budgetIds, startDate, endDate));
        summary.setDonationTrends(calculateDonationTrends(budgetIds, startDate, endDate));
        summary.setCampaignPerformance(calculateCampaignPerformance(budgetIds, startDate, endDate));
        
        return summary;
    }

    /**
     * Generate cost analysis
     */
    private CostAnalysisSummary generateCostAnalysis(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        CostAnalysisSummary summary = new CostAnalysisSummary();
        summary.setTotalCosts(calculateTotalCosts(budgetIds, startDate, endDate));
        summary.setCostPerBeneficiary(calculateCostPerBeneficiary(budgetIds, startDate, endDate));
        summary.setCostEfficiency(calculateCostEfficiency(budgetIds, startDate, endDate));
        summary.setTopCostDrivers(calculateTopCostDrivers(budgetIds, startDate, endDate));
        summary.setCategoryBreakdown(calculateCategoryBreakdown(budgetIds, startDate, endDate));
        summary.setMonthlySpending(calculateMonthlySpending(budgetIds, startDate, endDate));
        summary.setOptimizationOpportunities(calculateOptimizationOpportunities(budgetIds, startDate, endDate));
        summary.setSupplierAnalysis(calculateSupplierAnalysis(budgetIds, startDate, endDate));
        
        return summary;
    }

    /**
     * Generate financial metrics
     */
    private List<FinancialMetric> generateFinancialMetrics(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        List<FinancialMetric> metrics = new ArrayList<>();
        
        // Budget utilization metric
        FinancialMetric budgetUtilization = new FinancialMetric();
        budgetUtilization.setId(UUID.randomUUID().toString());
        budgetUtilization.setName("Budget Utilization");
        budgetUtilization.setValue(calculateOverallBudgetUtilization(budgetIds));
        budgetUtilization.setUnit("Percentage");
        budgetUtilization.setTrend(calculateBudgetUtilizationTrend(budgetIds, startDate, endDate));
        budgetUtilization.setTarget(80.0);
        budgetUtilization.setStatus(calculateMetricStatus(budgetUtilization.getValue(), budgetUtilization.getTarget()));
        metrics.add(budgetUtilization);
        
        // Cost efficiency metric
        FinancialMetric costEfficiency = new FinancialMetric();
        costEfficiency.setId(UUID.randomUUID().toString());
        costEfficiency.setName("Cost Efficiency");
        costEfficiency.setValue(calculateCostEfficiency(budgetIds, startDate, endDate));
        costEfficiency.setUnit("Score");
        costEfficiency.setTrend(calculateCostEfficiencyTrend(budgetIds, startDate, endDate));
        costEfficiency.setTarget(75.0);
        costEfficiency.setStatus(calculateMetricStatus(costEfficiency.getValue(), costEfficiency.getTarget()));
        metrics.add(costEfficiency);
        
        // Donation growth metric
        FinancialMetric donationGrowth = new FinancialMetric();
        donationGrowth.setId(UUID.randomUUID().toString());
        donationGrowth.setName("Donation Growth");
        donationGrowth.setValue(calculateDonationGrowth(budgetIds, startDate, endDate).doubleValue());
        donationGrowth.setUnit("Percentage");
        donationGrowth.setTrend(calculateDonationGrowthTrend(budgetIds, startDate, endDate));
        donationGrowth.setTarget(10.0);
        donationGrowth.setStatus(calculateMetricStatus(donationGrowth.getValue(), donationGrowth.getTarget()));
        metrics.add(donationGrowth);
        
        // Cost per beneficiary metric
        FinancialMetric costPerBeneficiary = new FinancialMetric();
        costPerBeneficiary.setId(UUID.randomUUID().toString());
        costPerBeneficiary.setName("Cost per Beneficiary");
        costPerBeneficiary.setValue(calculateCostPerBeneficiary(budgetIds, startDate, endDate).doubleValue());
        costPerBeneficiary.setUnit("Currency");
        costPerBeneficiary.setTrend(calculateCostPerBeneficiaryTrend(budgetIds, startDate, endDate));
        costPerBeneficiary.setTarget(100.0);
        costPerBeneficiary.setStatus(calculateMetricStatus(costPerBeneficiary.getValue(), costPerBeneficiary.getTarget()));
        metrics.add(costPerBeneficiary);
        
        return metrics;
    }

    /**
     * Generate trends
     */
    private List<FinancialTrend> generateTrends(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        List<FinancialTrend> trends = new ArrayList<>();
        
        // Budget utilization trend
        FinancialTrend budgetTrend = new FinancialTrend();
        budgetTrend.setId(UUID.randomUUID().toString());
        budgetTrend.setName("Budget Utilization Trend");
        budgetTrend.setType(TrendType.BUDGET_UTILIZATION);
        budgetTrend.setDataPoints(calculateBudgetUtilizationTrendPoints(budgetIds, startDate, endDate));
        budgetTrend.setDirection(calculateTrendDirection(budgetTrend.getDataPoints()));
        trends.add(budgetTrend);
        
        // Donation trend
        FinancialTrend donationTrend = new FinancialTrend();
        donationTrend.setId(UUID.randomUUID().toString());
        donationTrend.setName("Donation Trend");
        donationTrend.setType(TrendType.DONATIONS);
        donationTrend.setDataPoints(calculateDonationTrendPoints(budgetIds, startDate, endDate));
        donationTrend.setDirection(calculateTrendDirection(donationTrend.getDataPoints()));
        trends.add(donationTrend);
        
        // Cost trend
        FinancialTrend costTrend = new FinancialTrend();
        costTrend.setId(UUID.randomUUID().toString());
        costTrend.setName("Cost Trend");
        costTrend.setType(TrendType.COSTS);
        costTrend.setDataPoints(calculateCostTrendPoints(budgetIds, startDate, endDate));
        costTrend.setDirection(calculateTrendDirection(costTrend.getDataPoints()));
        trends.add(costTrend);
        
        return trends;
    }

    /**
     * Generate alerts
     */
    private List<FinancialAlert> generateAlerts(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        List<FinancialAlert> alerts = new ArrayList<>();
        
        // Budget overrun alerts
        for (String budgetId : budgetIds) {
            double utilization = calculateBudgetUtilization(budgetId);
            if (utilization > 90.0) {
                FinancialAlert alert = new FinancialAlert();
                alert.setId(UUID.randomUUID().toString());
                alert.setType(AlertType.BUDGET_OVERRUN);
                alert.setSeverity(AlertSeverity.HIGH);
                alert.setMessage("Budget " + budgetId + " is " + String.format("%.1f", utilization) + "% utilized");
                alert.setBudgetId(budgetId);
                alert.setCreatedAt(LocalDateTime.now());
                alerts.add(alert);
            }
        }
        
        // Low donation alerts
        BigDecimal totalDonations = calculateTotalDonations(budgetIds, startDate, endDate);
        BigDecimal totalCosts = calculateTotalCosts(budgetIds, startDate, endDate);
        if (totalDonations.compareTo(totalCosts.multiply(BigDecimal.valueOf(0.5))) < 0) {
            FinancialAlert alert = new FinancialAlert();
            alert.setId(UUID.randomUUID().toString());
            alert.setType(AlertType.LOW_DONATIONS);
            alert.setSeverity(AlertSeverity.MEDIUM);
            alert.setMessage("Donations are significantly lower than costs");
            alert.setCreatedAt(LocalDateTime.now());
            alerts.add(alert);
        }
        
        // High cost alerts
        double costEfficiency = calculateCostEfficiency(budgetIds, startDate, endDate);
        if (costEfficiency < 50.0) {
            FinancialAlert alert = new FinancialAlert();
            alert.setId(UUID.randomUUID().toString());
            alert.setType(AlertType.HIGH_COSTS);
            alert.setSeverity(AlertSeverity.MEDIUM);
            alert.setMessage("Cost efficiency is below target");
            alert.setCreatedAt(LocalDateTime.now());
            alerts.add(alert);
        }
        
        return alerts;
    }

    /**
     * Generate recommendations
     */
    private List<FinancialRecommendation> generateRecommendations(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        List<FinancialRecommendation> recommendations = new ArrayList<>();
        
        // Budget utilization recommendations
        double overallUtilization = calculateOverallBudgetUtilization(budgetIds);
        if (overallUtilization > 85.0) {
            FinancialRecommendation rec = new FinancialRecommendation();
            rec.setId(UUID.randomUUID().toString());
            rec.setType(RecommendationType.BUDGET_MANAGEMENT);
            rec.setPriority(Priority.HIGH);
            rec.setTitle("High Budget Utilization");
            rec.setDescription("Overall budget utilization is " + String.format("%.1f", overallUtilization) + "%. Consider requesting additional funding or optimizing costs.");
            rec.setActionItems(Arrays.asList("Review high-cost items", "Negotiate with suppliers", "Request additional funding"));
            recommendations.add(rec);
        }
        
        // Cost optimization recommendations
        double costEfficiency = calculateCostEfficiency(budgetIds, startDate, endDate);
        if (costEfficiency < 70.0) {
            FinancialRecommendation rec = new FinancialRecommendation();
            rec.setId(UUID.randomUUID().toString());
            rec.setType(RecommendationType.COST_OPTIMIZATION);
            rec.setPriority(Priority.MEDIUM);
            rec.setTitle("Improve Cost Efficiency");
            rec.setDescription("Cost efficiency is " + String.format("%.1f", costEfficiency) + "%. Focus on optimizing spending.");
            rec.setActionItems(Arrays.asList("Review supplier contracts", "Implement bulk purchasing", "Negotiate better prices"));
            recommendations.add(rec);
        }
        
        // Donation growth recommendations
        BigDecimal donationGrowth = calculateDonationGrowth(budgetIds, startDate, endDate);
        if (donationGrowth.compareTo(BigDecimal.valueOf(5.0)) < 0) {
            FinancialRecommendation rec = new FinancialRecommendation();
            rec.setId(UUID.randomUUID().toString());
            rec.setType(RecommendationType.DONATION_GROWTH);
            rec.setPriority(Priority.MEDIUM);
            rec.setTitle("Increase Donation Growth");
            rec.setDescription("Donation growth is " + donationGrowth + "%. Focus on donor engagement and campaign effectiveness.");
            rec.setActionItems(Arrays.asList("Launch new campaigns", "Improve donor communication", "Expand donor base"));
            recommendations.add(rec);
        }
        
        return recommendations;
    }

    // Helper methods for calculations
    private BigDecimal calculateTotalAllocatedAmount(String[] budgetIds) {
        // Mock implementation - would typically query budget service
        return BigDecimal.valueOf(1000000);
    }

    private BigDecimal calculateTotalSpentAmount(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        // Mock implementation - would typically query budget service
        return BigDecimal.valueOf(750000);
    }

    private BigDecimal calculateTotalDonations(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        // Mock implementation - would typically query donation service
        return BigDecimal.valueOf(800000);
    }

    private double calculateOverallBudgetUtilization(String[] budgetIds) {
        // Mock implementation - would typically query budget service
        return 75.0;
    }

    private double calculateBudgetUtilization(String budgetId) {
        // Mock implementation - would typically query budget service
        return 85.0;
    }

    private List<String> generateKeyHighlights(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return Arrays.asList(
            "Total budget utilization: 75%",
            "Donations exceeded costs by 6.7%",
            "Cost efficiency improved by 12%",
            "3 new major donors acquired"
        );
    }

    private List<String> generateCriticalIssues(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return Arrays.asList(
            "Budget A is 95% utilized",
            "Donation growth rate below target",
            "High administrative costs in Budget B"
        );
    }

    private int calculateActiveBudgets(String[] budgetIds) {
        return budgetIds.length;
    }

    private int calculateClosedBudgets(String[] budgetIds) {
        return 0;
    }

    private BigDecimal calculateTotalRemainingAmount(String[] budgetIds) {
        return BigDecimal.valueOf(250000);
    }

    private BigDecimal calculateAverageBudgetSize(String[] budgetIds) {
        return BigDecimal.valueOf(200000);
    }

    private Map<String, BigDecimal> calculateTopSpendingCategories(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> categories = new HashMap<>();
        categories.put("Medical Supplies", BigDecimal.valueOf(300000));
        categories.put("Food & Water", BigDecimal.valueOf(200000));
        categories.put("Shelter Materials", BigDecimal.valueOf(150000));
        categories.put("Transportation", BigDecimal.valueOf(100000));
        return categories;
    }

    private int calculateBudgetAlerts(String[] budgetIds) {
        return 2;
    }

    private int calculateTotalDonationCount(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return 150;
    }

    private BigDecimal calculateAverageDonationAmount(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return BigDecimal.valueOf(5333);
    }

    private BigDecimal calculateConfirmedDonations(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return BigDecimal.valueOf(750000);
    }

    private BigDecimal calculatePendingDonations(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return BigDecimal.valueOf(50000);
    }

    private List<String> calculateTopDonors(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return Arrays.asList("ABC Foundation", "XYZ Corporation", "Individual Donor A");
    }

    private Map<String, BigDecimal> calculateDonationTrends(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> trends = new HashMap<>();
        trends.put("2024-01", BigDecimal.valueOf(100000));
        trends.put("2024-02", BigDecimal.valueOf(120000));
        trends.put("2024-03", BigDecimal.valueOf(150000));
        return trends;
    }

    private Map<String, BigDecimal> calculateCampaignPerformance(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> performance = new HashMap<>();
        performance.put("Emergency Relief", BigDecimal.valueOf(300000));
        performance.put("Medical Aid", BigDecimal.valueOf(250000));
        performance.put("Food Security", BigDecimal.valueOf(200000));
        return performance;
    }

    private BigDecimal calculateTotalCosts(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return BigDecimal.valueOf(750000);
    }

    private BigDecimal calculateCostPerBeneficiary(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return BigDecimal.valueOf(150);
    }

    private double calculateCostEfficiency(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return 78.5;
    }

    private List<String> calculateTopCostDrivers(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return Arrays.asList("Medical Equipment", "Emergency Transportation", "Temporary Shelter");
    }

    private Map<String, BigDecimal> calculateCategoryBreakdown(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> breakdown = new HashMap<>();
        breakdown.put("Medical", BigDecimal.valueOf(300000));
        breakdown.put("Food", BigDecimal.valueOf(200000));
        breakdown.put("Shelter", BigDecimal.valueOf(150000));
        breakdown.put("Transport", BigDecimal.valueOf(100000));
        return breakdown;
    }

    private Map<String, BigDecimal> calculateMonthlySpending(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> spending = new HashMap<>();
        spending.put("2024-01", BigDecimal.valueOf(200000));
        spending.put("2024-02", BigDecimal.valueOf(250000));
        spending.put("2024-03", BigDecimal.valueOf(300000));
        return spending;
    }

    private List<String> calculateOptimizationOpportunities(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return Arrays.asList("Bulk purchasing for medical supplies", "Negotiate transportation contracts", "Consolidate supplier base");
    }

    private Map<String, BigDecimal> calculateSupplierAnalysis(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> suppliers = new HashMap<>();
        suppliers.put("MedSupply Co", BigDecimal.valueOf(150000));
        suppliers.put("FoodCorp", BigDecimal.valueOf(100000));
        suppliers.put("ShelterPro", BigDecimal.valueOf(80000));
        return suppliers;
    }

    private String calculateBudgetUtilizationTrend(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return "INCREASING";
    }

    private String calculateCostEfficiencyTrend(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return "IMPROVING";
    }

    private BigDecimal calculateDonationGrowth(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return BigDecimal.valueOf(15.5);
    }

    private String calculateDonationGrowthTrend(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return "INCREASING";
    }

    private String calculateCostPerBeneficiaryTrend(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        return "STABLE";
    }

    private String calculateMetricStatus(double value, double target) {
        if (value >= target) {
            return "GOOD";
        } else if (value >= target * 0.8) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    private List<DataPoint> calculateBudgetUtilizationTrendPoints(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        List<DataPoint> points = new ArrayList<>();
        points.add(new DataPoint("2024-01", 70.0));
        points.add(new DataPoint("2024-02", 72.0));
        points.add(new DataPoint("2024-03", 75.0));
        return points;
    }

    private List<DataPoint> calculateDonationTrendPoints(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        List<DataPoint> points = new ArrayList<>();
        points.add(new DataPoint("2024-01", 100000.0));
        points.add(new DataPoint("2024-02", 120000.0));
        points.add(new DataPoint("2024-03", 150000.0));
        return points;
    }

    private List<DataPoint> calculateCostTrendPoints(String[] budgetIds, LocalDateTime startDate, LocalDateTime endDate) {
        List<DataPoint> points = new ArrayList<>();
        points.add(new DataPoint("2024-01", 200000.0));
        points.add(new DataPoint("2024-02", 225000.0));
        points.add(new DataPoint("2024-03", 250000.0));
        return points;
    }

    private String calculateTrendDirection(List<DataPoint> dataPoints) {
        if (dataPoints.size() < 2) {
            return "STABLE";
        }
        
        double first = dataPoints.get(0).getValue();
        double last = dataPoints.get(dataPoints.size() - 1).getValue();
        
        if (last > first * 1.05) {
            return "INCREASING";
        } else if (last < first * 0.95) {
            return "DECREASING";
        } else {
            return "STABLE";
        }
    }

    // Data classes
    public static class FinancialReport {
        private String id;
        private String reportType;
        private String period;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime generatedAt;
        private ReportStatus status;
        private ReportFormat format;
        private UUID generatedBy;
        private ExecutiveSummary executiveSummary;
        private BudgetSummary budgetSummary;
        private DonationSummary donationSummary;
        private CostAnalysisSummary costAnalysis;
        private List<FinancialMetric> financialMetrics;
        private List<FinancialTrend> trends;
        private List<FinancialAlert> alerts;
        private List<FinancialRecommendation> recommendations;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

        public ReportStatus getStatus() { return status; }
        public void setStatus(ReportStatus status) { this.status = status; }

        public ExecutiveSummary getExecutiveSummary() { return executiveSummary; }
        public void setExecutiveSummary(ExecutiveSummary executiveSummary) { this.executiveSummary = executiveSummary; }

        public BudgetSummary getBudgetSummary() { return budgetSummary; }
        public void setBudgetSummary(BudgetSummary budgetSummary) { this.budgetSummary = budgetSummary; }

        public DonationSummary getDonationSummary() { return donationSummary; }
        public void setDonationSummary(DonationSummary donationSummary) { this.donationSummary = donationSummary; }

        public CostAnalysisSummary getCostAnalysis() { return costAnalysis; }
        public void setCostAnalysis(CostAnalysisSummary costAnalysis) { this.costAnalysis = costAnalysis; }

        public List<FinancialMetric> getFinancialMetrics() { return financialMetrics; }
        public void setFinancialMetrics(List<FinancialMetric> financialMetrics) { this.financialMetrics = financialMetrics; }

        public List<FinancialTrend> getTrends() { return trends; }
        public void setTrends(List<FinancialTrend> trends) { this.trends = trends; }

        public List<FinancialAlert> getAlerts() { return alerts; }
        public void setAlerts(List<FinancialAlert> alerts) { this.alerts = alerts; }

        public List<FinancialRecommendation> getRecommendations() { return recommendations; }
        public void setRecommendations(List<FinancialRecommendation> recommendations) { this.recommendations = recommendations; }

        public ReportFormat getFormat() { return format; }
        public void setFormat(ReportFormat format) { this.format = format; }

        public UUID getGeneratedBy() { return generatedBy; }
        public void setGeneratedBy(UUID generatedBy) { this.generatedBy = generatedBy; }
    }

    public static class ExecutiveSummary {
        private String period;
        private int totalBudgets;
        private BigDecimal totalAllocatedAmount;
        private BigDecimal totalSpentAmount;
        private BigDecimal totalDonations;
        private double budgetUtilization;
        private List<String> keyHighlights;
        private List<String> criticalIssues;

        // Getters and setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public int getTotalBudgets() { return totalBudgets; }
        public void setTotalBudgets(int totalBudgets) { this.totalBudgets = totalBudgets; }

        public BigDecimal getTotalAllocatedAmount() { return totalAllocatedAmount; }
        public void setTotalAllocatedAmount(BigDecimal totalAllocatedAmount) { this.totalAllocatedAmount = totalAllocatedAmount; }

        public BigDecimal getTotalSpentAmount() { return totalSpentAmount; }
        public void setTotalSpentAmount(BigDecimal totalSpentAmount) { this.totalSpentAmount = totalSpentAmount; }

        public BigDecimal getTotalDonations() { return totalDonations; }
        public void setTotalDonations(BigDecimal totalDonations) { this.totalDonations = totalDonations; }

        public double getBudgetUtilization() { return budgetUtilization; }
        public void setBudgetUtilization(double budgetUtilization) { this.budgetUtilization = budgetUtilization; }

        public List<String> getKeyHighlights() { return keyHighlights; }
        public void setKeyHighlights(List<String> keyHighlights) { this.keyHighlights = keyHighlights; }

        public List<String> getCriticalIssues() { return criticalIssues; }
        public void setCriticalIssues(List<String> criticalIssues) { this.criticalIssues = criticalIssues; }
    }

    public static class BudgetSummary {
        private int totalBudgets;
        private int activeBudgets;
        private int closedBudgets;
        private BigDecimal totalAllocatedAmount;
        private BigDecimal totalSpentAmount;
        private BigDecimal totalRemainingAmount;
        private BigDecimal averageBudgetSize;
        private double budgetUtilizationRate;
        private Map<String, BigDecimal> topSpendingCategories;
        private int budgetAlerts;

        // Getters and setters
        public int getTotalBudgets() { return totalBudgets; }
        public void setTotalBudgets(int totalBudgets) { this.totalBudgets = totalBudgets; }

        public int getActiveBudgets() { return activeBudgets; }
        public void setActiveBudgets(int activeBudgets) { this.activeBudgets = activeBudgets; }

        public int getClosedBudgets() { return closedBudgets; }
        public void setClosedBudgets(int closedBudgets) { this.closedBudgets = closedBudgets; }

        public BigDecimal getTotalAllocatedAmount() { return totalAllocatedAmount; }
        public void setTotalAllocatedAmount(BigDecimal totalAllocatedAmount) { this.totalAllocatedAmount = totalAllocatedAmount; }

        public BigDecimal getTotalSpentAmount() { return totalSpentAmount; }
        public void setTotalSpentAmount(BigDecimal totalSpentAmount) { this.totalSpentAmount = totalSpentAmount; }

        public BigDecimal getTotalRemainingAmount() { return totalRemainingAmount; }
        public void setTotalRemainingAmount(BigDecimal totalRemainingAmount) { this.totalRemainingAmount = totalRemainingAmount; }

        public BigDecimal getAverageBudgetSize() { return averageBudgetSize; }
        public void setAverageBudgetSize(BigDecimal averageBudgetSize) { this.averageBudgetSize = averageBudgetSize; }

        public double getBudgetUtilizationRate() { return budgetUtilizationRate; }
        public void setBudgetUtilizationRate(double budgetUtilizationRate) { this.budgetUtilizationRate = budgetUtilizationRate; }

        public Map<String, BigDecimal> getTopSpendingCategories() { return topSpendingCategories; }
        public void setTopSpendingCategories(Map<String, BigDecimal> topSpendingCategories) { this.topSpendingCategories = topSpendingCategories; }

        public int getBudgetAlerts() { return budgetAlerts; }
        public void setBudgetAlerts(int budgetAlerts) { this.budgetAlerts = budgetAlerts; }
    }

    public static class DonationSummary {
        private int totalDonations;
        private BigDecimal totalDonationAmount;
        private BigDecimal averageDonationAmount;
        private BigDecimal confirmedDonations;
        private BigDecimal pendingDonations;
        private List<String> topDonors;
        private Map<String, BigDecimal> donationTrends;
        private Map<String, BigDecimal> campaignPerformance;

        // Getters and setters
        public int getTotalDonations() { return totalDonations; }
        public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

        public BigDecimal getTotalDonationAmount() { return totalDonationAmount; }
        public void setTotalDonationAmount(BigDecimal totalDonationAmount) { this.totalDonationAmount = totalDonationAmount; }

        public BigDecimal getAverageDonationAmount() { return averageDonationAmount; }
        public void setAverageDonationAmount(BigDecimal averageDonationAmount) { this.averageDonationAmount = averageDonationAmount; }

        public BigDecimal getConfirmedDonations() { return confirmedDonations; }
        public void setConfirmedDonations(BigDecimal confirmedDonations) { this.confirmedDonations = confirmedDonations; }

        public BigDecimal getPendingDonations() { return pendingDonations; }
        public void setPendingDonations(BigDecimal pendingDonations) { this.pendingDonations = pendingDonations; }

        public List<String> getTopDonors() { return topDonors; }
        public void setTopDonors(List<String> topDonors) { this.topDonors = topDonors; }

        public Map<String, BigDecimal> getDonationTrends() { return donationTrends; }
        public void setDonationTrends(Map<String, BigDecimal> donationTrends) { this.donationTrends = donationTrends; }

        public Map<String, BigDecimal> getCampaignPerformance() { return campaignPerformance; }
        public void setCampaignPerformance(Map<String, BigDecimal> campaignPerformance) { this.campaignPerformance = campaignPerformance; }
    }

    public static class CostAnalysisSummary {
        private BigDecimal totalCosts;
        private BigDecimal costPerBeneficiary;
        private double costEfficiency;
        private List<String> topCostDrivers;
        private Map<String, BigDecimal> categoryBreakdown;
        private Map<String, BigDecimal> monthlySpending;
        private List<String> optimizationOpportunities;
        private Map<String, BigDecimal> supplierAnalysis;

        // Getters and setters
        public BigDecimal getTotalCosts() { return totalCosts; }
        public void setTotalCosts(BigDecimal totalCosts) { this.totalCosts = totalCosts; }

        public BigDecimal getCostPerBeneficiary() { return costPerBeneficiary; }
        public void setCostPerBeneficiary(BigDecimal costPerBeneficiary) { this.costPerBeneficiary = costPerBeneficiary; }

        public double getCostEfficiency() { return costEfficiency; }
        public void setCostEfficiency(double costEfficiency) { this.costEfficiency = costEfficiency; }

        public List<String> getTopCostDrivers() { return topCostDrivers; }
        public void setTopCostDrivers(List<String> topCostDrivers) { this.topCostDrivers = topCostDrivers; }

        public Map<String, BigDecimal> getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(Map<String, BigDecimal> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

        public Map<String, BigDecimal> getMonthlySpending() { return monthlySpending; }
        public void setMonthlySpending(Map<String, BigDecimal> monthlySpending) { this.monthlySpending = monthlySpending; }

        public List<String> getOptimizationOpportunities() { return optimizationOpportunities; }
        public void setOptimizationOpportunities(List<String> optimizationOpportunities) { this.optimizationOpportunities = optimizationOpportunities; }

        public Map<String, BigDecimal> getSupplierAnalysis() { return supplierAnalysis; }
        public void setSupplierAnalysis(Map<String, BigDecimal> supplierAnalysis) { this.supplierAnalysis = supplierAnalysis; }
    }

    public static class FinancialMetric {
        private String id;
        private String name;
        private double value;
        private String unit;
        private String trend;
        private double target;
        private String status;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }

        public double getTarget() { return target; }
        public void setTarget(double target) { this.target = target; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class FinancialTrend {
        private String id;
        private String name;
        private TrendType type;
        private List<DataPoint> dataPoints;
        private String direction;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public TrendType getType() { return type; }
        public void setType(TrendType type) { this.type = type; }

        public List<DataPoint> getDataPoints() { return dataPoints; }
        public void setDataPoints(List<DataPoint> dataPoints) { this.dataPoints = dataPoints; }

        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
    }

    public static class DataPoint {
        private String period;
        private double value;

        public DataPoint(String period, double value) {
            this.period = period;
            this.value = value;
        }

        // Getters and setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
    }

    public static class FinancialAlert {
        private String id;
        private AlertType type;
        private AlertSeverity severity;
        private String message;
        private String budgetId;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public AlertType getType() { return type; }
        public void setType(AlertType type) { this.type = type; }

        public AlertSeverity getSeverity() { return severity; }
        public void setSeverity(AlertSeverity severity) { this.severity = severity; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getBudgetId() { return budgetId; }
        public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class FinancialRecommendation {
        private String id;
        private RecommendationType type;
        private Priority priority;
        private String title;
        private String description;
        private List<String> actionItems;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public RecommendationType getType() { return type; }
        public void setType(RecommendationType type) { this.type = type; }

        public Priority getPriority() { return priority; }
        public void setPriority(Priority priority) { this.priority = priority; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getActionItems() { return actionItems; }
        public void setActionItems(List<String> actionItems) { this.actionItems = actionItems; }
    }

    public enum ReportStatus {
        GENERATING, GENERATED, FAILED
    }

    public enum TrendType {
        BUDGET_UTILIZATION, DONATIONS, COSTS, EFFICIENCY
    }

    public enum AlertType {
        BUDGET_OVERRUN, LOW_DONATIONS, HIGH_COSTS, EFFICIENCY_LOW
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum RecommendationType {
        BUDGET_MANAGEMENT, COST_OPTIMIZATION, DONATION_GROWTH, EFFICIENCY_IMPROVEMENT
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ReportType {
        INCOME_STATEMENT, BALANCE_SHEET, CASH_FLOW, BUDGET_VS_ACTUAL, DONATION_SUMMARY, EXPENSE_ANALYSIS, COST_BREAKDOWN, FINANCIAL_DASHBOARD, CUSTOM
    }

    public enum ReportFormat {
        PDF, EXCEL, CSV, JSON
    }

    public enum ReportSchedule {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }

    public FinancialReport getReport(String reportId) {
        // Implementation for getting report
        return new FinancialReport();
    }

    public Map<String, Object> scheduleReport(String reportId, ReportSchedule schedule, List<String> recipients, boolean enabled) {
        // Implementation for scheduling report
        return Collections.emptyMap();
    }

    public Map<String, Object> cancelScheduledReport(String reportId) {
        // Implementation for canceling scheduled report
        return Collections.emptyMap();
    }

    public Map<String, Object> shareReport(String reportId, List<String> recipients, LocalDateTime expiryDate, Map<String, Object> permissions) {
        // Implementation for sharing report
        return Collections.emptyMap();
    }

    public Map<String, Object> getReportAnalytics(String reportId) {
        // Implementation for getting report analytics
        return Collections.emptyMap();
    }

    public List<Map<String, Object>> getReportTemplates() {
        // Implementation for getting report templates
        return Collections.emptyList();
    }

    public Map<String, Object> createReportTemplate(String name, String description, ReportType type, Map<String, Object> configuration, UUID userId) {
        // Implementation for creating report template
        return Collections.emptyMap();
    }

    public Map<String, Object> updateReportTemplate(String templateId, String name, String description, Map<String, Object> configuration) {
        // Implementation for updating report template
        return Collections.emptyMap();
    }

    public Map<String, Object> deleteReportTemplate(String templateId) {
        // Implementation for deleting report template
        return Collections.emptyMap();
    }

    public Map<String, Object> getFinancialDashboard(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for getting financial dashboard
        return Collections.emptyMap();
    }

    public Map<String, Object> getFinancialKPIs(LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for getting financial KPIs
        return Collections.emptyMap();
    }

    /**
     * Generate report with filters
     */
    public FinancialReport generateReport(ReportType type, ReportFormat format,
                                         LocalDateTime startDate, LocalDateTime endDate,
                                         Map<String, Object> filters, UUID userId) {
        // Convert filters to budget IDs if needed
        String[] budgetIds = extractBudgetIds(filters);
        String reportType = type != null ? type.name() : "CUSTOM";
        String period = calculatePeriod(startDate, endDate);
        
        FinancialReport report = generateFinancialReport(reportType, period, startDate, endDate, budgetIds);
        
        // Store format and user info
        report.setFormat(format);
        report.setGeneratedBy(userId);
        
        return report;
    }

    /**
     * Get reports with filters
     */
    public List<FinancialReport> getReports(ReportType type, LocalDateTime startDate, 
                                          LocalDateTime endDate, int limit) {
        return reports.values().stream()
            .filter(r -> type == null || r.getReportType().equals(type.name()))
            .filter(r -> startDate == null || !r.getStartDate().isBefore(startDate))
            .filter(r -> endDate == null || !r.getEndDate().isAfter(endDate))
            .sorted((a, b) -> b.getGeneratedAt().compareTo(a.getGeneratedAt()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Download report in specified format
     */
    public byte[] downloadReport(String reportId, ReportFormat format) {
        FinancialReport report = reports.get(reportId);
        if (report == null) {
            throw new IllegalArgumentException("Report not found");
        }
        
        // Mock implementation - would generate actual file
        return new byte[0];
    }

    // Helper methods
    private String[] extractBudgetIds(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return new String[]{"default"};
        }
        Object budgetIds = filters.get("budgetIds");
        if (budgetIds instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) budgetIds;
            return ids.toArray(new String[0]);
        }
        return new String[]{"default"};
    }

    private String calculatePeriod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return "CUSTOM";
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 7) return "WEEKLY";
        if (days <= 31) return "MONTHLY";
        if (days <= 93) return "QUARTERLY";
        if (days <= 365) return "YEARLY";
        return "CUSTOM";
    }
}
