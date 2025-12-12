package com.relief.controller.financial;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.financial.BudgetTrackingService;
import com.relief.service.financial.BudgetTrackingService.Budget;
import com.relief.service.financial.BudgetTrackingService.BudgetTransaction;
import com.relief.service.financial.BudgetTrackingService.BudgetSummary;
import com.relief.service.financial.BudgetTrackingService.BudgetAnalytics;
import com.relief.service.financial.BudgetTrackingService.TransactionType;
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
import java.util.UUID;

/**
 * Budget tracking controller
 */
@RestController
@RequestMapping("/budget-tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Budget Tracking", description = "Budget tracking and spending control APIs")
public class BudgetTrackingController {

    private final BudgetTrackingService budgetTrackingService;
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

    @PostMapping("/create")
    @Operation(summary = "Create a new budget")
    public ResponseEntity<Budget> createBudget(
            @RequestBody CreateBudgetRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        Budget budget = budgetTrackingService.createBudget(
            request.getName(),
            request.getDescription(),
            request.getTotalAmount(),
            request.getCategory(),
            userId,
            request.getStartDate(),
            request.getEndDate()
        );
        
        return ResponseEntity.ok(budget);
    }

    @PostMapping("/{budgetId}/transactions")
    @Operation(summary = "Record a budget transaction")
    public ResponseEntity<BudgetTransaction> recordTransaction(
            @PathVariable String budgetId,
            @RequestBody RecordTransactionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        UUID userId = getUserIdFromPrincipal(principal);
        
        BudgetTransaction transaction = budgetTrackingService.recordTransaction(
            budgetId,
            request.getDescription(),
            request.getAmount(),
            request.getType(),
            request.getCategory(),
            userId,
            request.getReferenceId()
        );
        
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{budgetId}")
    @Operation(summary = "Get budget details")
    public ResponseEntity<Budget> getBudget(@PathVariable String budgetId) {
        Budget budget = budgetTrackingService.getBudget(budgetId);
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/{budgetId}/transactions")
    @Operation(summary = "Get budget transactions")
    public ResponseEntity<List<BudgetTransaction>> getBudgetTransactions(
            @PathVariable String budgetId,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<BudgetTransaction> transactions = budgetTrackingService.getBudgetTransactions(budgetId, limit);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{budgetId}/alerts")
    @Operation(summary = "Get budget alerts")
    public ResponseEntity<List<BudgetTrackingService.BudgetAlert>> getBudgetAlerts(@PathVariable String budgetId) {
        List<BudgetTrackingService.BudgetAlert> alerts = budgetTrackingService.getBudgetAlerts(budgetId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/my-budgets")
    @Operation(summary = "Get user's budgets")
    public ResponseEntity<List<Budget>> getUserBudgets(@AuthenticationPrincipal UserDetails principal) {
        UUID userId = getUserIdFromPrincipal(principal);
        List<Budget> budgets = budgetTrackingService.getUserBudgets(userId);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get budgets by category")
    public ResponseEntity<List<Budget>> getBudgetsByCategory(@PathVariable String category) {
        List<Budget> budgets = budgetTrackingService.getBudgetsByCategory(category);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{budgetId}")
    @Operation(summary = "Update budget")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable String budgetId,
            @RequestBody UpdateBudgetRequest request) {
        
        Budget budget = budgetTrackingService.updateBudget(
            budgetId,
            request.getName(),
            request.getDescription(),
            request.getTotalAmount(),
            request.getEndDate()
        );
        
        return ResponseEntity.ok(budget);
    }

    @PostMapping("/{budgetId}/close")
    @Operation(summary = "Close budget")
    public ResponseEntity<Budget> closeBudget(
            @PathVariable String budgetId,
            @RequestBody CloseBudgetRequest request) {
        
        Budget budget = budgetTrackingService.closeBudget(budgetId, request.getReason());
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/{budgetId}/summary")
    @Operation(summary = "Get budget summary")
    public ResponseEntity<BudgetSummary> getBudgetSummary(@PathVariable String budgetId) {
        BudgetSummary summary = budgetTrackingService.getBudgetSummary(budgetId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{budgetId}/analytics")
    @Operation(summary = "Get budget analytics")
    public ResponseEntity<BudgetAnalytics> getBudgetAnalytics(@PathVariable String budgetId) {
        BudgetAnalytics analytics = budgetTrackingService.getBudgetAnalytics(budgetId);
        return ResponseEntity.ok(analytics);
    }

    // Request DTOs
    public static class CreateBudgetRequest {
        private String name;
        private String description;
        private BigDecimal totalAmount;
        private String category;
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    }

    public static class RecordTransactionRequest {
        private String description;
        private BigDecimal amount;
        private TransactionType type;
        private String category;
        private String referenceId;

        // Getters and setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getReferenceId() { return referenceId; }
        public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    }

    public static class UpdateBudgetRequest {
        private String name;
        private String description;
        private BigDecimal totalAmount;
        private LocalDateTime endDate;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    }

    public static class CloseBudgetRequest {
        private String reason;

        // Getters and setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
