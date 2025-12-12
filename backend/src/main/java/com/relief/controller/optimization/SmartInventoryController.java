package com.relief.controller.optimization;

import com.relief.service.optimization.SmartInventoryService;
import com.relief.service.optimization.SmartInventoryService.InventoryOptimization;
import com.relief.service.optimization.SmartInventoryService.ReorderRule;
import com.relief.service.optimization.SmartInventoryService.StockOptimization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/optimization/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Smart Inventory Management", description = "ML algorithms for automatic reordering and stock optimization")
public class SmartInventoryController {

    private final SmartInventoryService inventoryService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze inventory and generate recommendations")
    public ResponseEntity<InventoryOptimization> analyzeInventory(
            @RequestParam String itemId,
            @RequestParam int currentStock,
            @RequestParam int minThreshold,
            @RequestBody(required = false) Map<String, Object> historicalData) {
        
        InventoryOptimization optimization = inventoryService.analyzeInventory(
                itemId, currentStock, minThreshold, historicalData != null ? historicalData : Map.of());
        return ResponseEntity.ok(optimization);
    }

    @PostMapping("/optimize")
    @Operation(summary = "Optimize stock for multiple items")
    public ResponseEntity<StockOptimization> optimizeStock(
            @RequestBody Map<String, Integer> currentStock,
            @RequestBody(required = false) Map<String, Map<String, Object>> itemData) {
        
        StockOptimization optimization = inventoryService.optimizeStock(
                currentStock, itemData != null ? itemData : Map.of());
        return ResponseEntity.ok(optimization);
    }

    @PostMapping("/rules")
    @Operation(summary = "Create reorder rule")
    public ResponseEntity<ReorderRule> createReorderRule(
            @RequestParam String itemId,
            @RequestParam String ruleType,
            @RequestBody Map<String, Object> parameters) {
        
        ReorderRule rule = inventoryService.createReorderRule(itemId, ruleType, parameters);
        return ResponseEntity.ok(rule);
    }

    @GetMapping("/rules")
    @Operation(summary = "Get all reorder rules")
    public ResponseEntity<List<ReorderRule>> getReorderRules() {
        List<ReorderRule> rules = inventoryService.getReorderRules();
        return ResponseEntity.ok(rules);
    }
}

