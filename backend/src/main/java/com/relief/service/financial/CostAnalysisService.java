package com.relief.service.financial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for cost analysis, breakdown, and optimization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostAnalysisService {

    private final Map<String, CostCategory> costCategories = new ConcurrentHashMap<>();
    private final Map<String, List<CostItem>> costItems = new ConcurrentHashMap<>();
    private final Map<String, CostOptimization> costOptimizations = new ConcurrentHashMap<>();
    private final Map<String, CostTrend> costTrends = new ConcurrentHashMap<>();

    {
        initializeCostCategories();
    }

    /**
     * Perform cost analysis with category and filters
     */
    @Transactional(readOnly = true)
    public CostAnalysis performCostAnalysis(String category, LocalDateTime startDate, 
                                           LocalDateTime endDate, Map<String, Object> filters, 
                                           UUID userId) {
        log.info("Performing cost analysis for category {}: {} to {}", category, startDate, endDate);
        
        // Use category as budgetId if no budgetId in filters
        String budgetId = filters != null && filters.containsKey("budgetId") 
            ? (String) filters.get("budgetId") 
            : category;
        
        String analysisType = filters != null && filters.containsKey("analysisType")
            ? (String) filters.get("analysisType")
            : "CATEGORY_ANALYSIS";
        
        return analyzeCosts(budgetId, analysisType, startDate, endDate);
    }

    /**
     * Analyze costs for a specific budget or relief effort
     */
    @Transactional(readOnly = true)
    public CostAnalysis analyzeCosts(String budgetId, String analysisType, 
                                   LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Analyzing costs for budget {}: {} to {}", budgetId, startDate, endDate);
        
        CostAnalysis analysis = new CostAnalysis();
        analysis.setBudgetId(budgetId);
        analysis.setAnalysisType(analysisType);
        analysis.setStartDate(startDate);
        analysis.setEndDate(endDate);
        analysis.setAnalyzedAt(LocalDateTime.now());
        
        // Get cost items for the budget
        List<CostItem> items = costItems.getOrDefault(budgetId, new ArrayList<>());
        
        // Filter by date range
        List<CostItem> filteredItems = items.stream()
            .filter(item -> item.getDate().isAfter(startDate) && item.getDate().isBefore(endDate))
            .collect(Collectors.toList());
        
        // Calculate basic metrics
        analysis.setTotalCost(filteredItems.stream()
            .map(CostItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        analysis.setItemCount(filteredItems.size());
        
        // Calculate category breakdown
        Map<String, BigDecimal> categoryBreakdown = filteredItems.stream()
            .collect(Collectors.groupingBy(
                CostItem::getCategory,
                Collectors.reducing(BigDecimal.ZERO, CostItem::getAmount, BigDecimal::add)
            ));
        analysis.setCategoryBreakdown(categoryBreakdown);
        
        // Calculate monthly breakdown
        Map<String, BigDecimal> monthlyBreakdown = filteredItems.stream()
            .collect(Collectors.groupingBy(
                item -> item.getDate().toLocalDate().withDayOfMonth(1).toString(),
                Collectors.reducing(BigDecimal.ZERO, CostItem::getAmount, BigDecimal::add)
            ));
        analysis.setMonthlyBreakdown(monthlyBreakdown);
        
        // Calculate cost per unit metrics
        analysis.setAverageCostPerItem(calculateAverageCostPerItem(filteredItems));
        analysis.setCostPerDay(calculateCostPerDay(filteredItems, startDate, endDate));
        analysis.setCostPerBeneficiary(calculateCostPerBeneficiary(filteredItems));
        
        // Identify cost drivers
        analysis.setTopCostDrivers(identifyTopCostDrivers(filteredItems));
        
        // Calculate efficiency metrics
        analysis.setCostEfficiency(calculateCostEfficiency(filteredItems));
        analysis.setBudgetUtilization(calculateBudgetUtilization(budgetId, analysis.getTotalCost()));
        
        // Identify optimization opportunities
        analysis.setOptimizationOpportunities(identifyOptimizationOpportunities(filteredItems));
        
        // Calculate trends
        analysis.setCostTrends(calculateCostTrends(filteredItems));
        
        // Generate recommendations
        analysis.setRecommendations(generateRecommendations(analysis));
        
        log.info("Cost analysis completed for budget {}: Total cost: {}", budgetId, analysis.getTotalCost());
        return analysis;
    }

    /**
     * Add cost item
     */
    @Transactional
    public CostItem addCostItem(String budgetId, String description, BigDecimal amount,
                              String category, String subcategory, String unit,
                              int quantity, String supplier, String notes) {
        log.info("Adding cost item for budget {}: {} - {}", budgetId, description, amount);
        
        CostItem item = new CostItem();
        item.setId(UUID.randomUUID().toString());
        item.setBudgetId(budgetId);
        item.setDescription(description);
        item.setAmount(amount);
        item.setCategory(category);
        item.setSubcategory(subcategory);
        item.setUnit(unit);
        item.setQuantity(quantity);
        item.setUnitCost(amount.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP));
        item.setSupplier(supplier);
        item.setNotes(notes);
        item.setDate(LocalDateTime.now());
        item.setCreatedAt(LocalDateTime.now());
        
        costItems.computeIfAbsent(budgetId, k -> new ArrayList<>()).add(item);
        
        log.info("Cost item added: {} for budget {}", item.getId(), budgetId);
        return item;
    }

    /**
     * Get cost breakdown by category
     */
    @Transactional(readOnly = true)
    public CostBreakdown getCostBreakdown(String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Find all items matching the category and date range
        List<CostItem> categoryItems = costItems.values().stream()
            .flatMap(List::stream)
            .filter(item -> category.equals(item.getCategory()))
            .filter(item -> {
                if (startDate != null && item.getDate().isBefore(startDate)) return false;
                if (endDate != null && item.getDate().isAfter(endDate)) return false;
                return true;
            })
            .collect(Collectors.toList());
        
        CostBreakdown breakdown = new CostBreakdown();
        breakdown.setCategory(category);
        breakdown.setTotalAmount(categoryItems.stream()
            .map(CostItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.setItemCount(categoryItems.size());
        breakdown.setAverageItemCost(calculateAverageCostPerItem(categoryItems));
        breakdown.setItems(categoryItems);
        
        // Calculate subcategory breakdown
        Map<String, BigDecimal> subcategoryBreakdown = categoryItems.stream()
            .collect(Collectors.groupingBy(
                CostItem::getSubcategory,
                Collectors.reducing(BigDecimal.ZERO, CostItem::getAmount, BigDecimal::add)
            ));
        breakdown.setSubcategoryBreakdown(subcategoryBreakdown);
        
        return breakdown;
    }

    /**
     * Get cost optimization suggestions
     */
    @Transactional(readOnly = true)
    public List<CostOptimization> getCostOptimizations(String budgetId) {
        List<CostItem> items = costItems.getOrDefault(budgetId, new ArrayList<>());
        
        List<CostOptimization> optimizations = new ArrayList<>();
        
        // Identify high-cost items
        List<CostItem> highCostItems = items.stream()
            .sorted(Comparator.comparing(CostItem::getAmount).reversed())
            .limit(5)
            .collect(Collectors.toList());
        
        for (CostItem item : highCostItems) {
            CostOptimization optimization = new CostOptimization();
            optimization.setId(UUID.randomUUID().toString());
            optimization.setBudgetId(budgetId);
            optimization.setType(OptimizationType.HIGH_COST_ITEM);
            optimization.setDescription("High cost item: " + item.getDescription());
            optimization.setCurrentCost(item.getAmount());
            optimization.setPotentialSavings(item.getAmount().multiply(BigDecimal.valueOf(0.1))); // 10% savings
            optimization.setRecommendation("Consider negotiating with supplier or finding alternative");
            optimization.setPriority(Priority.HIGH);
            optimization.setCreatedAt(LocalDateTime.now());
            
            optimizations.add(optimization);
        }
        
        // Identify bulk purchase opportunities
        Map<String, List<CostItem>> itemsByDescription = items.stream()
            .collect(Collectors.groupingBy(CostItem::getDescription));
        
        for (Map.Entry<String, List<CostItem>> entry : itemsByDescription.entrySet()) {
            if (entry.getValue().size() > 1) {
                BigDecimal totalCost = entry.getValue().stream()
                    .map(CostItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                CostOptimization optimization = new CostOptimization();
                optimization.setId(UUID.randomUUID().toString());
                optimization.setBudgetId(budgetId);
                optimization.setType(OptimizationType.BULK_PURCHASE);
                optimization.setDescription("Bulk purchase opportunity: " + entry.getKey());
                optimization.setCurrentCost(totalCost);
                optimization.setPotentialSavings(totalCost.multiply(BigDecimal.valueOf(0.15))); // 15% savings
                optimization.setRecommendation("Consider bulk purchasing to reduce unit costs");
                optimization.setPriority(Priority.MEDIUM);
                optimization.setCreatedAt(LocalDateTime.now());
                
                optimizations.add(optimization);
            }
        }
        
        // Identify supplier consolidation opportunities
        Map<String, List<CostItem>> itemsBySupplier = items.stream()
            .collect(Collectors.groupingBy(CostItem::getSupplier));
        
        if (itemsBySupplier.size() > 3) {
            CostOptimization optimization = new CostOptimization();
            optimization.setId(UUID.randomUUID().toString());
            optimization.setBudgetId(budgetId);
            optimization.setType(OptimizationType.SUPPLIER_CONSOLIDATION);
            optimization.setDescription("Supplier consolidation opportunity");
            optimization.setCurrentCost(BigDecimal.ZERO);
            optimization.setPotentialSavings(BigDecimal.valueOf(1000)); // Estimated savings
            optimization.setRecommendation("Consider consolidating suppliers to reduce administrative costs");
            optimization.setPriority(Priority.MEDIUM);
            optimization.setCreatedAt(LocalDateTime.now());
            
            optimizations.add(optimization);
        }
        
        return optimizations;
    }

    /**
     * Get cost trends
     */
    @Transactional(readOnly = true)
    public List<CostTrend> getCostTrends(String category, LocalDateTime startDate, LocalDateTime endDate, int days) {
        // Use days to determine grouping if startDate/endDate are null
        final LocalDateTime finalStartDate = startDate != null ? startDate : LocalDateTime.now().minusDays(days);
        final LocalDateTime finalEndDate = endDate != null ? endDate : LocalDateTime.now();
        
        // Find all items matching the category and date range
        List<CostItem> filteredItems = costItems.values().stream()
            .flatMap(List::stream)
            .filter(item -> category == null || category.equals(item.getCategory()))
            .filter(item -> {
                if (finalStartDate != null && item.getDate().isBefore(finalStartDate)) return false;
                if (finalEndDate != null && item.getDate().isAfter(finalEndDate)) return false;
                return true;
            })
            .collect(Collectors.toList());
        
        Map<String, BigDecimal> monthlyCosts = filteredItems.stream()
            .collect(Collectors.groupingBy(
                item -> item.getDate().toLocalDate().withDayOfMonth(1).toString(),
                Collectors.reducing(BigDecimal.ZERO, CostItem::getAmount, BigDecimal::add)
            ));
        
        List<CostTrend> trends = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : monthlyCosts.entrySet()) {
            CostTrend trend = new CostTrend();
            trend.setPeriod(entry.getKey());
            trend.setAmount(entry.getValue());
            trend.setItemCount(filteredItems.stream()
                .filter(item -> item.getDate().toLocalDate().withDayOfMonth(1).toString().equals(entry.getKey()))
                .collect(Collectors.toList()).size());
            trends.add(trend);
        }
        
        return trends.stream()
            .sorted(Comparator.comparing(CostTrend::getPeriod))
            .collect(Collectors.toList());
    }

    /**
     * Get cost efficiency metrics
     */
    @Transactional(readOnly = true)
    public CostEfficiency getCostEfficiency(String budgetId) {
        List<CostItem> items = costItems.getOrDefault(budgetId, new ArrayList<>());
        
        CostEfficiency efficiency = new CostEfficiency();
        efficiency.setBudgetId(budgetId);
        efficiency.setTotalCost(items.stream()
            .map(CostItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        efficiency.setItemCount(items.size());
        efficiency.setAverageCostPerItem(calculateAverageCostPerItem(items));
        efficiency.setCostPerDay(calculateCostPerDay(items, LocalDateTime.now().minusMonths(1), LocalDateTime.now()));
        efficiency.setCostPerBeneficiary(calculateCostPerBeneficiary(items));
        efficiency.setEfficiencyScore(calculateEfficiencyScore(items));
        efficiency.setBenchmarkComparison(compareWithBenchmarks(efficiency));
        
        return efficiency;
    }

    /**
     * Calculate average cost per item
     */
    private BigDecimal calculateAverageCostPerItem(List<CostItem> items) {
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalCost = items.stream()
            .map(CostItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalCost.divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate cost per day
     */
    private BigDecimal calculateCostPerDay(List<CostItem> items, LocalDateTime startDate, LocalDateTime endDate) {
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalCost = items.stream()
            .map(CostItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long days = java.time.Duration.between(startDate, endDate).toDays();
        if (days == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalCost.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate cost per beneficiary
     */
    private BigDecimal calculateCostPerBeneficiary(List<CostItem> items) {
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalCost = items.stream()
            .map(CostItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalBeneficiaries = items.stream()
            .mapToInt(CostItem::getQuantity)
            .sum();
        
        if (totalBeneficiaries == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalCost.divide(BigDecimal.valueOf(totalBeneficiaries), 2, RoundingMode.HALF_UP);
    }

    /**
     * Identify top cost drivers
     */
    private List<CostItem> identifyTopCostDrivers(List<CostItem> items) {
        return items.stream()
            .sorted(Comparator.comparing(CostItem::getAmount).reversed())
            .limit(10)
            .collect(Collectors.toList());
    }

    /**
     * Calculate cost efficiency
     */
    private double calculateCostEfficiency(List<CostItem> items) {
        if (items.isEmpty()) {
            return 0.0;
        }
        
        // Simple efficiency calculation based on cost per item
        BigDecimal totalCost = items.stream()
            .map(CostItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageCost = totalCost.divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);
        
        // Efficiency score (0-100) based on average cost
        if (averageCost.compareTo(BigDecimal.valueOf(100)) < 0) {
            return 90.0; // High efficiency
        } else if (averageCost.compareTo(BigDecimal.valueOf(500)) < 0) {
            return 70.0; // Medium efficiency
        } else {
            return 50.0; // Low efficiency
        }
    }

    /**
     * Calculate budget utilization
     */
    private double calculateBudgetUtilization(String budgetId, BigDecimal totalCost) {
        // This would typically get the budget from the budget service
        // For now, return a mock value
        return 0.75; // 75% utilization
    }

    /**
     * Identify optimization opportunities
     */
    private List<String> identifyOptimizationOpportunities(List<CostItem> items) {
        List<String> opportunities = new ArrayList<>();
        
        // Check for high-cost items
        BigDecimal averageCost = calculateAverageCostPerItem(items);
        long highCostItems = items.stream()
            .filter(item -> item.getAmount().compareTo(averageCost.multiply(BigDecimal.valueOf(2))) > 0)
            .count();
        
        if (highCostItems > 0) {
            opportunities.add("Consider negotiating prices for " + highCostItems + " high-cost items");
        }
        
        // Check for bulk purchase opportunities
        Map<String, Long> itemCounts = items.stream()
            .collect(Collectors.groupingBy(CostItem::getDescription, Collectors.counting()));
        
        long bulkOpportunities = itemCounts.values().stream()
            .filter(count -> count > 1)
            .count();
        
        if (bulkOpportunities > 0) {
            opportunities.add("Consider bulk purchasing for " + bulkOpportunities + " frequently ordered items");
        }
        
        // Check for supplier diversity
        long uniqueSuppliers = items.stream()
            .map(CostItem::getSupplier)
            .distinct()
            .count();
        
        if (uniqueSuppliers > 5) {
            opportunities.add("Consider consolidating suppliers to reduce administrative costs");
        }
        
        return opportunities;
    }

    /**
     * Calculate cost trends
     */
    private List<CostTrend> calculateCostTrends(List<CostItem> items) {
        Map<String, BigDecimal> monthlyCosts = items.stream()
            .collect(Collectors.groupingBy(
                item -> item.getDate().toLocalDate().withDayOfMonth(1).toString(),
                Collectors.reducing(BigDecimal.ZERO, CostItem::getAmount, BigDecimal::add)
            ));
        
        return monthlyCosts.entrySet().stream()
            .map(entry -> {
                CostTrend trend = new CostTrend();
                trend.setPeriod(entry.getKey());
                trend.setAmount(entry.getValue());
                return trend;
            })
            .sorted(Comparator.comparing(CostTrend::getPeriod))
            .collect(Collectors.toList());
    }

    /**
     * Generate recommendations
     */
    private List<String> generateRecommendations(CostAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        
        if (analysis.getCostEfficiency() < 60.0) {
            recommendations.add("Improve cost efficiency by negotiating better prices with suppliers");
        }
        
        if (analysis.getBudgetUtilization() > 0.9) {
            recommendations.add("Budget utilization is high - consider requesting additional funding");
        }
        
        if (analysis.getTopCostDrivers().size() > 5) {
            recommendations.add("Focus on reducing costs for top 5 cost drivers");
        }
        
        if (analysis.getOptimizationOpportunities().size() > 0) {
            recommendations.add("Implement cost optimization opportunities to reduce overall spending");
        }
        
        return recommendations;
    }

    /**
     * Calculate efficiency score
     */
    private double calculateEfficiencyScore(List<CostItem> items) {
        return calculateCostEfficiency(items);
    }

    /**
     * Compare with benchmarks
     */
    private String compareWithBenchmarks(CostEfficiency efficiency) {
        if (efficiency.getEfficiencyScore() > 80) {
            return "Above industry average";
        } else if (efficiency.getEfficiencyScore() > 60) {
            return "At industry average";
        } else {
            return "Below industry average";
        }
    }

    /**
     * Initialize cost categories
     */
    private void initializeCostCategories() {
        String[] categories = {
            "Medical Supplies", "Food & Water", "Shelter Materials", "Transportation",
            "Personnel", "Equipment", "Communication", "Administrative", "Other"
        };
        
        for (String category : categories) {
            CostCategory costCategory = new CostCategory();
            costCategory.setId(UUID.randomUUID().toString());
            costCategory.setName(category);
            costCategory.setDescription("Cost category for " + category);
            costCategory.setCreatedAt(LocalDateTime.now());
            
            costCategories.put(category, costCategory);
        }
    }

    // Data classes
    public static class CostItem {
        private String id;
        private String budgetId;
        private String description;
        private BigDecimal amount;
        private String category;
        private String subcategory;
        private String unit;
        private int quantity;
        private BigDecimal unitCost;
        private String supplier;
        private String notes;
        private LocalDateTime date;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getBudgetId() { return budgetId; }
        public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getSubcategory() { return subcategory; }
        public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

        public String getSupplier() { return supplier; }
        public void setSupplier(String supplier) { this.supplier = supplier; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CostCategory {
        private String id;
        private String name;
        private String description;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CostAnalysis {
        private String budgetId;
        private String analysisType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime analyzedAt;
        private BigDecimal totalCost;
        private int itemCount;
        private Map<String, BigDecimal> categoryBreakdown;
        private Map<String, BigDecimal> monthlyBreakdown;
        private BigDecimal averageCostPerItem;
        private BigDecimal costPerDay;
        private BigDecimal costPerBeneficiary;
        private List<CostItem> topCostDrivers;
        private double costEfficiency;
        private double budgetUtilization;
        private List<String> optimizationOpportunities;
        private List<CostTrend> costTrends;
        private List<String> recommendations;

        // Getters and setters
        public String getBudgetId() { return budgetId; }
        public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

        public String getAnalysisType() { return analysisType; }
        public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public LocalDateTime getAnalyzedAt() { return analyzedAt; }
        public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }

        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }

        public Map<String, BigDecimal> getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(Map<String, BigDecimal> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

        public Map<String, BigDecimal> getMonthlyBreakdown() { return monthlyBreakdown; }
        public void setMonthlyBreakdown(Map<String, BigDecimal> monthlyBreakdown) { this.monthlyBreakdown = monthlyBreakdown; }

        public BigDecimal getAverageCostPerItem() { return averageCostPerItem; }
        public void setAverageCostPerItem(BigDecimal averageCostPerItem) { this.averageCostPerItem = averageCostPerItem; }

        public BigDecimal getCostPerDay() { return costPerDay; }
        public void setCostPerDay(BigDecimal costPerDay) { this.costPerDay = costPerDay; }

        public BigDecimal getCostPerBeneficiary() { return costPerBeneficiary; }
        public void setCostPerBeneficiary(BigDecimal costPerBeneficiary) { this.costPerBeneficiary = costPerBeneficiary; }

        public List<CostItem> getTopCostDrivers() { return topCostDrivers; }
        public void setTopCostDrivers(List<CostItem> topCostDrivers) { this.topCostDrivers = topCostDrivers; }

        public double getCostEfficiency() { return costEfficiency; }
        public void setCostEfficiency(double costEfficiency) { this.costEfficiency = costEfficiency; }

        public double getBudgetUtilization() { return budgetUtilization; }
        public void setBudgetUtilization(double budgetUtilization) { this.budgetUtilization = budgetUtilization; }

        public List<String> getOptimizationOpportunities() { return optimizationOpportunities; }
        public void setOptimizationOpportunities(List<String> optimizationOpportunities) { this.optimizationOpportunities = optimizationOpportunities; }

        public List<CostTrend> getCostTrends() { return costTrends; }
        public void setCostTrends(List<CostTrend> costTrends) { this.costTrends = costTrends; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    public static class CostBreakdown {
        private String category;
        private BigDecimal totalAmount;
        private int itemCount;
        private BigDecimal averageItemCost;
        private List<CostItem> items;
        private Map<String, BigDecimal> subcategoryBreakdown;

        // Getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }

        public BigDecimal getAverageItemCost() { return averageItemCost; }
        public void setAverageItemCost(BigDecimal averageItemCost) { this.averageItemCost = averageItemCost; }

        public List<CostItem> getItems() { return items; }
        public void setItems(List<CostItem> items) { this.items = items; }

        public Map<String, BigDecimal> getSubcategoryBreakdown() { return subcategoryBreakdown; }
        public void setSubcategoryBreakdown(Map<String, BigDecimal> subcategoryBreakdown) { this.subcategoryBreakdown = subcategoryBreakdown; }
    }

    public static class CostOptimization {
        private String id;
        private String budgetId;
        private OptimizationType type;
        private String description;
        private BigDecimal currentCost;
        private BigDecimal potentialSavings;
        private String recommendation;
        private Priority priority;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getBudgetId() { return budgetId; }
        public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

        public OptimizationType getType() { return type; }
        public void setType(OptimizationType type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getCurrentCost() { return currentCost; }
        public void setCurrentCost(BigDecimal currentCost) { this.currentCost = currentCost; }

        public BigDecimal getPotentialSavings() { return potentialSavings; }
        public void setPotentialSavings(BigDecimal potentialSavings) { this.potentialSavings = potentialSavings; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

        public Priority getPriority() { return priority; }
        public void setPriority(Priority priority) { this.priority = priority; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CostTrend {
        private String period;
        private BigDecimal amount;
        private int itemCount;

        // Getters and setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    }

    public static class CostEfficiency {
        private String budgetId;
        private BigDecimal totalCost;
        private int itemCount;
        private BigDecimal averageCostPerItem;
        private BigDecimal costPerDay;
        private BigDecimal costPerBeneficiary;
        private double efficiencyScore;
        private String benchmarkComparison;

        // Getters and setters
        public String getBudgetId() { return budgetId; }
        public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }

        public BigDecimal getAverageCostPerItem() { return averageCostPerItem; }
        public void setAverageCostPerItem(BigDecimal averageCostPerItem) { this.averageCostPerItem = averageCostPerItem; }

        public BigDecimal getCostPerDay() { return costPerDay; }
        public void setCostPerDay(BigDecimal costPerDay) { this.costPerDay = costPerDay; }

        public BigDecimal getCostPerBeneficiary() { return costPerBeneficiary; }
        public void setCostPerBeneficiary(BigDecimal costPerBeneficiary) { this.costPerBeneficiary = costPerBeneficiary; }

        public double getEfficiencyScore() { return efficiencyScore; }
        public void setEfficiencyScore(double efficiencyScore) { this.efficiencyScore = efficiencyScore; }

        public String getBenchmarkComparison() { return benchmarkComparison; }
        public void setBenchmarkComparison(String benchmarkComparison) { this.benchmarkComparison = benchmarkComparison; }
    }

    public enum OptimizationType {
        HIGH_COST_ITEM, BULK_PURCHASE, SUPPLIER_CONSOLIDATION, PRICE_NEGOTIATION
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public static class CostDriver {
        private String name;
        private BigDecimal impact;
        private BigDecimal percentage;
        private String description;
        private List<String> recommendations;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getImpact() { return impact; }
        public void setImpact(BigDecimal impact) { this.impact = impact; }

        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    public List<CostDriver> getCostDrivers(String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for getting cost drivers
        return Collections.emptyList();
    }

    public List<CostCategory> getCostCategories() {
        // Implementation for getting cost categories
        return Collections.emptyList();
    }

    public CostOptimization getCostOptimization(String category, BigDecimal budget, Map<String, Object> constraints) {
        // Implementation for getting cost optimization
        return new CostOptimization();
    }

    public Map<String, Object> compareCosts(String category, LocalDateTime period1Start, LocalDateTime period1End, LocalDateTime period2Start, LocalDateTime period2End) {
        // Implementation for comparing costs
        return Collections.emptyMap();
    }

    public Map<String, Object> getCostForecast(String category, int months) {
        // Implementation for getting cost forecast
        return Collections.emptyMap();
    }

    public Map<String, Object> getCostVariance(String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for getting cost variance
        return Collections.emptyMap();
    }

    public Map<String, Object> benchmarkCosts(String category, String region, String organizationSize) {
        // Implementation for benchmarking costs
        return Collections.emptyMap();
    }

    public Map<String, Object> getCostEfficiency(String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for getting cost efficiency
        return Collections.emptyMap();
    }
}
