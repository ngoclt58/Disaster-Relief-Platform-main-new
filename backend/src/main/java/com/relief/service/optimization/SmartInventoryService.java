package com.relief.service.optimization;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for ML algorithms for automatic reordering and stock optimization
 */
@Service
@RequiredArgsConstructor
public class SmartInventoryService {

    private static final Logger log = LoggerFactory.getLogger(SmartInventoryService.class);

    private final Map<String, InventoryOptimization> optimizations = new ConcurrentHashMap<>();
    private final Map<String, ReorderRule> reorderRules = new ConcurrentHashMap<>();

    /**
     * Analyze inventory and generate reorder recommendations
     */
    public InventoryOptimization analyzeInventory(
            String itemId,
            int currentStock,
            int minThreshold,
            Map<String, Object> historicalData) {

        InventoryOptimization optimization = new InventoryOptimization();
        optimization.setItemId(itemId);
        optimization.setAnalyzedAt(LocalDateTime.now());
        optimization.setCurrentStock(currentStock);
        optimization.setMinThreshold(minThreshold);

        // ML-based prediction
        double predictedDemand = predictDemand(historicalData);
        optimization.setPredictedDemand(predictedDemand);
        
        int recommendedOrder = calculateOptimalReorder(currentStock, minThreshold, predictedDemand, historicalData);
        optimization.setRecommendedOrder(recommendedOrder);
        optimization.setEconomicOrderQuantity(calculateEOQ(predictedDemand, historicalData));
        optimization.setReorderPoint(calculateReorderPoint(minThreshold, predictedDemand));
        optimization.setConfidence(calculateConfidence(historicalData));
        optimization.setEstimatedCost(recommendedOrder * 10.0); // Example cost

        optimizations.put(itemId, optimization);
        
        log.info("Analyzed inventory for item: {} - Recommended order: {}", itemId, recommendedOrder);
        return optimization;
    }

    /**
     * Create automatic reorder rule based on ML predictions
     */
    public ReorderRule createReorderRule(
            String itemId,
            String ruleType,
            Map<String, Object> parameters) {

        ReorderRule rule = new ReorderRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setItemId(itemId);
        rule.setRuleType(ruleType);
        rule.setParameters(parameters);
        rule.setActive(true);
        rule.setCreatedAt(LocalDateTime.now());

        reorderRules.put(rule.getId(), rule);

        log.info("Created reorder rule: {} for item: {}", rule.getId(), itemId);
        return rule;
    }

    /**
     * Apply stock optimization for multiple items
     */
    public StockOptimization optimizeStock(Map<String, Integer> currentStock, Map<String, Map<String, Object>> itemData) {
        StockOptimization optimization = new StockOptimization();
        optimization.setOptimizedAt(LocalDateTime.now());
        List<ReorderRecommendation> recommendations = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : currentStock.entrySet()) {
            String itemId = entry.getKey();
            int stock = entry.getValue();
            Map<String, Object> historicalData = itemData.get(itemId);

            InventoryOptimization invOpt = analyzeInventory(itemId, stock, 50, historicalData != null ? historicalData : new HashMap<>());
            
            if (invOpt.getRecommendedOrder() > 0) {
                ReorderRecommendation rec = new ReorderRecommendation();
                rec.setItemId(itemId);
                rec.setCurrentStock(stock);
                rec.setRecommendedQuantity(invOpt.getRecommendedOrder());
                rec.setPriority(invOpt.getCurrentStock() < invOpt.getMinThreshold() ? "HIGH" : "MEDIUM");
                rec.setEstimatedCost(invOpt.getEstimatedCost());
                recommendations.add(rec);
            }
        }

        optimization.setRecommendations(recommendations);
        optimization.setTotalEstimatedCost(
                recommendations.stream().mapToDouble(ReorderRecommendation::getEstimatedCost).sum()
        );

        return optimization;
    }

    private double predictDemand(Map<String, Object> historicalData) {
        if (historicalData == null || !historicalData.containsKey("avgDailyUsage")) {
            return 10.0; // Default prediction
        }

        double avgUsage = (Double) historicalData.get("avgDailyUsage");
        double trend = historicalData.containsKey("trend") ? (Double) historicalData.get("trend") : 1.0;
        double seasonality = historicalData.containsKey("seasonality") ? (Double) historicalData.get("seasonality") : 1.0;

        return avgUsage * trend * seasonality;
    }

    private int calculateOptimalReorder(int currentStock, int minThreshold, double predictedDemand, Map<String, Object> historicalData) {
        // If below threshold, order enough to reach optimal level
        if (currentStock < minThreshold) {
            int daysOfSupply = historicalData.containsKey("leadTime") ? 
                    (Integer) historicalData.get("leadTime") : 7;
            return (int) Math.ceil(predictedDemand * daysOfSupply * 1.5);
        }

        // If above threshold but below reorder point, consider ordering
        int reorderPoint = calculateReorderPoint(minThreshold, predictedDemand);
        if (currentStock < reorderPoint) {
            return (int) Math.ceil(predictedDemand * 7); // 1 week supply
        }

        return 0; // No reorder needed
    }

    private int calculateReorderPoint(int minThreshold, double predictedDemand) {
        return minThreshold + (int) Math.ceil(predictedDemand * 3); // 3 days of demand
    }

    private int calculateEOQ(double predictedDemand, Map<String, Object> historicalData) {
        // Economic Order Quantity formula
        double orderingCost = historicalData.containsKey("orderingCost") ? 
                (Double) historicalData.get("orderingCost") : 50.0;
        double holdingCost = historicalData.containsKey("holdingCost") ? 
                (Double) historicalData.get("holdingCost") : 2.0;
        
        int demand = (int) Math.ceil(predictedDemand * 30); // Monthly demand
        
        int eoq = (int) Math.sqrt((2 * orderingCost * demand) / holdingCost);
        return Math.max(eoq, 10); // Minimum order quantity
    }

    private double calculateConfidence(Map<String, Object> historicalData) {
        if (historicalData == null || historicalData.isEmpty()) {
            return 0.5; // Low confidence with no data
        }

        // More historical data = higher confidence
        boolean hasData = historicalData.containsKey("dataPoints");
        int dataPoints = hasData ? (Integer) historicalData.get("dataPoints") : 0;

        if (dataPoints > 100) return 0.95;
        if (dataPoints > 50) return 0.85;
        if (dataPoints > 20) return 0.75;
        return 0.60;
    }

    public List<ReorderRule> getReorderRules() {
        return new ArrayList<>(reorderRules.values());
    }

    public InventoryOptimization getOptimization(String itemId) {
        return optimizations.get(itemId);
    }

    // Inner classes
    @lombok.Data
    public static class InventoryOptimization {
        private String itemId;
        private LocalDateTime analyzedAt;
        private int currentStock;
        private int minThreshold;
        private double predictedDemand;
        private int recommendedOrder;
        private int economicOrderQuantity;
        private int reorderPoint;
        private double confidence;
        private double estimatedCost;
    }

    @lombok.Data
    public static class ReorderRule {
        private String id;
        private String itemId;
        private String ruleType;
        private Map<String, Object> parameters;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @lombok.Data
    public static class StockOptimization {
        private LocalDateTime optimizedAt;
        private List<ReorderRecommendation> recommendations;
        private double totalEstimatedCost;
    }

    @lombok.Data
    public static class ReorderRecommendation {
        private String itemId;
        private int currentStock;
        private int recommendedQuantity;
        private String priority;
        private double estimatedCost;
    }
}

