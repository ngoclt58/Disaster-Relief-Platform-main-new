package com.relief.controller;

import com.relief.entity.NeedsRequest;
import com.relief.entity.InventoryHub;
import com.relief.entity.InventoryStock;
import com.relief.entity.ItemCatalog;
import com.relief.repository.NeedsRequestRepository;
import com.relief.repository.InventoryHubRepository;
import com.relief.repository.InventoryStockRepository;
import com.relief.repository.ItemCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final NeedsRequestRepository needsRequestRepository;
    private final InventoryHubRepository hubRepository;
    private final InventoryStockRepository stockRepository;
    private final ItemCatalogRepository itemRepository;

    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests() {
        try {
            List<NeedsRequest> requests = needsRequestRepository.findAll();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @GetMapping("/requests/count")
    public ResponseEntity<?> getRequestsCount() {
        try {
            long count = needsRequestRepository.count();
            return ResponseEntity.ok("Total requests: " + count);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @GetMapping("/inventory/summary")
    public ResponseEntity<?> getInventorySummary() {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            // Count entities
            long hubCount = hubRepository.count();
            long itemCount = itemRepository.count();
            long stockCount = stockRepository.count();
            
            summary.put("hubCount", hubCount);
            summary.put("itemCount", itemCount);
            summary.put("stockCount", stockCount);
            
            // Get hub names
            List<InventoryHub> hubs = hubRepository.findAll();
            summary.put("hubNames", hubs.stream().map(InventoryHub::getName).toList());
            
            // Get item names (first 5)
            List<ItemCatalog> items = itemRepository.findAll();
            summary.put("itemNames", items.stream().limit(5).map(ItemCatalog::getName).toList());
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @GetMapping("/inventory/stock-raw")
    public ResponseEntity<?> getStockRaw() {
        try {
            List<InventoryStock> stocks = stockRepository.findAll();
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", stocks.size());
            result.put("stocksWithHub", stocks.stream().filter(s -> s.getHub() != null).count());
            result.put("stocksWithItem", stocks.stream().filter(s -> s.getItem() != null).count());
            result.put("stocksWithBoth", stocks.stream().filter(s -> s.getHub() != null && s.getItem() != null).count());
            
            // Sample first stock
            if (!stocks.isEmpty()) {
                InventoryStock first = stocks.get(0);
                Map<String, Object> sample = new HashMap<>();
                sample.put("id", first.getId());
                sample.put("qtyAvailable", first.getQtyAvailable());
                sample.put("qtyReserved", first.getQtyReserved());
                sample.put("hasHub", first.getHub() != null);
                sample.put("hasItem", first.getItem() != null);
                if (first.getHub() != null) {
                    sample.put("hubName", first.getHub().getName());
                }
                if (first.getItem() != null) {
                    sample.put("itemName", first.getItem().getName());
                }
                result.put("sampleStock", sample);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage() + " - " + e.getClass().getSimpleName());
        }
    }
}