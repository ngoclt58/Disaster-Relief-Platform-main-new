package com.relief.controller;

import com.relief.entity.InventoryHub;
import com.relief.entity.InventoryStock;
import com.relief.entity.ItemCatalog;
import com.relief.entity.StockMovement;
import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management endpoints")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    
    private UUID getUserIdFromPrincipal(UserDetails principal) {
        if (principal == null) return null;
        String username = principal.getUsername();
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            User user = userRepository.findByEmail(username)
                    .orElseGet(() -> userRepository.findByPhone(username)
                            .orElse(null));
            return user != null ? user.getId() : null;
        }
    }

    @GetMapping("/hubs")
    @Operation(summary = "List all inventory hubs")
    public ResponseEntity<List<InventoryHub>> getHubs() {
        return ResponseEntity.ok(inventoryService.getAllHubs());
    }

    @GetMapping("/items")
    @Operation(summary = "List all items in catalog")
    public ResponseEntity<List<ItemCatalog>> getItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/stock")
    @Operation(summary = "Get stock by hub or item, or all stock if no parameters")
    public ResponseEntity<List<InventoryStock>> getStock(
            @RequestParam(required = false) UUID hub,
            @RequestParam(required = false) UUID item
    ) {
        if (hub != null) {
            return ResponseEntity.ok(inventoryService.getStockByHub(hub));
        } else if (item != null) {
            return ResponseEntity.ok(inventoryService.getStockByItem(item));
        } else {
            // Return all stock when no parameters provided
            return ResponseEntity.ok(inventoryService.getAllStock());
        }
    }

    @PutMapping("/stock")
    @Operation(summary = "Update stock quantities")
    public ResponseEntity<InventoryStock> updateStock(
            @RequestBody UpdateStockRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        UUID userId = getUserIdFromPrincipal(principal);
        return ResponseEntity.ok(inventoryService.updateStock(
                request.getHubId(), 
                request.getItemId(), 
                request.getQtyAvailable(), 
                request.getQtyReserved(),
                userId,
                request.getReason()
        ));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for a task")
    public ResponseEntity<InventoryStock> reserveStock(
            @RequestBody ReserveStockRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        UUID userId = getUserIdFromPrincipal(principal);
        return ResponseEntity.ok(inventoryService.reserveStock(
                request.getHubId(), 
                request.getItemId(), 
                request.getQuantity(),
                userId
        ));
    }

    @PostMapping("/release")
    @Operation(summary = "Release reserved stock")
    public ResponseEntity<InventoryStock> releaseStock(
            @RequestBody ReserveStockRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        UUID userId = getUserIdFromPrincipal(principal);
        return ResponseEntity.ok(inventoryService.releaseReservation(
                request.getHubId(), 
                request.getItemId(), 
                request.getQuantity(),
                userId
        ));
    }
    
    @GetMapping("/movements")
    @Operation(summary = "Get stock movements")
    public ResponseEntity<List<StockMovement>> getStockMovements(
            @RequestParam(required = false) UUID hub,
            @RequestParam(required = false) UUID item) {
        return ResponseEntity.ok(inventoryService.getStockMovements(hub, item));
    }

    @Data
    public static class UpdateStockRequest {
        private UUID hubId;
        private UUID itemId;
        private Integer qtyAvailable;
        private Integer qtyReserved;

        // Explicit getters and setters for Lombok compatibility
        public UUID getHubId() { return hubId; }
        public void setHubId(UUID hubId) { this.hubId = hubId; }

        public UUID getItemId() { return itemId; }
        public void setItemId(UUID itemId) { this.itemId = itemId; }

        public Integer getQtyAvailable() { return qtyAvailable; }
        public void setQtyAvailable(Integer qtyAvailable) { this.qtyAvailable = qtyAvailable; }

        public Integer getQtyReserved() { return qtyReserved; }
        public void setQtyReserved(Integer qtyReserved) { this.qtyReserved = qtyReserved; }
        
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    @Data
    public static class ReserveStockRequest {
        private UUID hubId;
        private UUID itemId;
        private Integer quantity;

        // Explicit getters and setters for Lombok compatibility
        public UUID getHubId() { return hubId; }
        public void setHubId(UUID hubId) { this.hubId = hubId; }

        public UUID getItemId() { return itemId; }
        public void setItemId(UUID itemId) { this.itemId = itemId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}



