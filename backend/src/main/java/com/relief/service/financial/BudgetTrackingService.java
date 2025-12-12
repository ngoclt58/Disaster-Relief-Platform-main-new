package com.relief.service.financial;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for budget tracking and spending control across relief efforts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetTrackingService {

    private final UserRepository userRepository;
    private final Map<String, Budget> budgets = new ConcurrentHashMap<>();
    private final Map<String, List<BudgetTransaction>> budgetTransactions = new ConcurrentHashMap<>();
    private final Map<String, List<BudgetAlert>> budgetAlerts = new ConcurrentHashMap<>();

    /**
     * Create a new budget for a relief effort
     */
    @Transactional
    public Budget createBudget(String name, String description, BigDecimal totalAmount, 
                              String category, UUID createdBy, LocalDateTime startDate, 
                              LocalDateTime endDate) {
        log.info("Creating budget: {} for category: {}", name, category);
        
        User creator = userRepository.findById(createdBy)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        
        Budget budget = new Budget();
        budget.setId(UUID.randomUUID().toString());
        budget.setName(name);
        budget.setDescription(description);
        budget.setTotalAmount(totalAmount);
        budget.setRemainingAmount(totalAmount);
        budget.setSpentAmount(BigDecimal.ZERO);
        budget.setCategory(category);
        budget.setCreatedBy(createdBy);
        budget.setCreatedByName(creator.getFullName());
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setStatus(BudgetStatus.ACTIVE);
        budget.setCreatedAt(LocalDateTime.now());
        budget.setUpdatedAt(LocalDateTime.now());
        
        // Set budget alerts
        budget.setAlertThresholds(Arrays.asList(0.8, 0.9, 0.95)); // 80%, 90%, 95%
        
        budgets.put(budget.getId(), budget);
        budgetTransactions.put(budget.getId(), new ArrayList<>());
        budgetAlerts.put(budget.getId(), new ArrayList<>());
        
        log.info("Budget created: {} with ID: {}", budget.getName(), budget.getId());
        return budget;
    }

    /**
     * Record a budget transaction (expense or income)
     */
    @Transactional
    public BudgetTransaction recordTransaction(String budgetId, String description, 
                                            BigDecimal amount, TransactionType type, 
                                            String category, UUID recordedBy, 
                                            String referenceId) {
        log.info("Recording transaction for budget {}: {} {}", budgetId, amount, type);
        
        Budget budget = budgets.get(budgetId);
        if (budget == null) {
            throw new IllegalArgumentException("Budget not found");
        }
        
        User recorder = userRepository.findById(recordedBy)
            .orElseThrow(() -> new IllegalArgumentException("Recorder not found"));
        
        BudgetTransaction transaction = new BudgetTransaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setBudgetId(budgetId);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setRecordedBy(recordedBy);
        transaction.setRecordedByName(recorder.getFullName());
        transaction.setReferenceId(referenceId);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setCreatedAt(LocalDateTime.now());
        
        // Update budget amounts
        if (type == TransactionType.EXPENSE) {
            budget.setSpentAmount(budget.getSpentAmount().add(amount));
            budget.setRemainingAmount(budget.getTotalAmount().subtract(budget.getSpentAmount()));
        } else if (type == TransactionType.INCOME) {
            budget.setTotalAmount(budget.getTotalAmount().add(amount));
            budget.setRemainingAmount(budget.getRemainingAmount().add(amount));
        }
        
        budget.setUpdatedAt(LocalDateTime.now());
        
        // Store transaction
        budgetTransactions.get(budgetId).add(transaction);
        
        // Check for budget alerts
        checkBudgetAlerts(budget);
        
        log.info("Transaction recorded: {} for budget {}", transaction.getId(), budgetId);
        return transaction;
    }

    /**
     * Get budget details
     */
    @Transactional(readOnly = true)
    public Budget getBudget(String budgetId) {
        Budget budget = budgets.get(budgetId);
        if (budget == null) {
            throw new IllegalArgumentException("Budget not found");
        }
        return budget;
    }

    /**
     * Get budget transactions
     */
    @Transactional(readOnly = true)
    public List<BudgetTransaction> getBudgetTransactions(String budgetId, int limit) {
        return budgetTransactions.getOrDefault(budgetId, new ArrayList<>())
            .stream()
            .sorted(Comparator.comparing(BudgetTransaction::getTransactionDate).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get budget alerts
     */
    @Transactional(readOnly = true)
    public List<BudgetAlert> getBudgetAlerts(String budgetId) {
        return budgetAlerts.getOrDefault(budgetId, new ArrayList<>());
    }

    /**
     * Get all budgets for a user
     */
    @Transactional(readOnly = true)
    public List<Budget> getUserBudgets(UUID userId) {
        return budgets.values().stream()
            .filter(budget -> budget.getCreatedBy().equals(userId))
            .sorted(Comparator.comparing(Budget::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get budgets by category
     */
    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByCategory(String category) {
        return budgets.values().stream()
            .filter(budget -> budget.getCategory().equals(category))
            .sorted(Comparator.comparing(Budget::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Update budget
     */
    @Transactional
    public Budget updateBudget(String budgetId, String name, String description, 
                              BigDecimal totalAmount, LocalDateTime endDate) {
        Budget budget = budgets.get(budgetId);
        if (budget == null) {
            throw new IllegalArgumentException("Budget not found");
        }
        
        budget.setName(name);
        budget.setDescription(description);
        
        // Recalculate if total amount changed
        if (totalAmount != null && !totalAmount.equals(budget.getTotalAmount())) {
            budget.setTotalAmount(totalAmount);
            budget.setRemainingAmount(totalAmount.subtract(budget.getSpentAmount()));
        }
        
        if (endDate != null) {
            budget.setEndDate(endDate);
        }
        
        budget.setUpdatedAt(LocalDateTime.now());
        
        log.info("Budget updated: {}", budgetId);
        return budget;
    }

    /**
     * Close budget
     */
    @Transactional
    public Budget closeBudget(String budgetId, String reason) {
        Budget budget = budgets.get(budgetId);
        if (budget == null) {
            throw new IllegalArgumentException("Budget not found");
        }
        
        budget.setStatus(BudgetStatus.CLOSED);
        budget.setClosedAt(LocalDateTime.now());
        budget.setCloseReason(reason);
        budget.setUpdatedAt(LocalDateTime.now());
        
        log.info("Budget closed: {} - {}", budgetId, reason);
        return budget;
    }

    /**
     * Get budget summary
     */
    @Transactional(readOnly = true)
    public BudgetSummary getBudgetSummary(String budgetId) {
        Budget budget = budgets.get(budgetId);
        if (budget == null) {
            throw new IllegalArgumentException("Budget not found");
        }
        
        List<BudgetTransaction> transactions = budgetTransactions.getOrDefault(budgetId, new ArrayList<>());
        
        BudgetSummary summary = new BudgetSummary();
        summary.setBudget(budget);
        summary.setTotalTransactions(transactions.size());
        summary.setTotalExpenses(transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(BudgetTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalIncome(transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(BudgetTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setAverageTransactionAmount(transactions.isEmpty() ? BigDecimal.ZERO :
            transactions.stream()
                .map(BudgetTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(transactions.size()), 2, BigDecimal.ROUND_HALF_UP));
        summary.setSpendingRate(calculateSpendingRate(budget));
        summary.setDaysRemaining(calculateDaysRemaining(budget));
        summary.setUtilizationPercentage(calculateUtilizationPercentage(budget));
        
        return summary;
    }

    /**
     * Get budget analytics
     */
    @Transactional(readOnly = true)
    public BudgetAnalytics getBudgetAnalytics(String budgetId) {
        Budget budget = budgets.get(budgetId);
        if (budget == null) {
            throw new IllegalArgumentException("Budget not found");
        }
        
        List<BudgetTransaction> transactions = budgetTransactions.getOrDefault(budgetId, new ArrayList<>());
        
        BudgetAnalytics analytics = new BudgetAnalytics();
        analytics.setBudgetId(budgetId);
        analytics.setTotalAmount(budget.getTotalAmount());
        analytics.setSpentAmount(budget.getSpentAmount());
        analytics.setRemainingAmount(budget.getRemainingAmount());
        analytics.setUtilizationPercentage(calculateUtilizationPercentage(budget));
        analytics.setSpendingRate(calculateSpendingRate(budget));
        analytics.setProjectedEndDate(calculateProjectedEndDate(budget));
        analytics.setCategoryBreakdown(calculateCategoryBreakdown(transactions));
        analytics.setMonthlySpending(calculateMonthlySpending(transactions));
        analytics.setTopExpenses(calculateTopExpenses(transactions));
        analytics.setAlertsCount(budgetAlerts.getOrDefault(budgetId, new ArrayList<>()).size());
        
        return analytics;
    }

    /**
     * Check budget alerts
     */
    private void checkBudgetAlerts(Budget budget) {
        double utilizationPercentage = calculateUtilizationPercentage(budget);
        
        for (Double threshold : budget.getAlertThresholds()) {
            if (utilizationPercentage >= threshold && !hasAlertForThreshold(budget.getId(), threshold)) {
                BudgetAlert alert = new BudgetAlert();
                alert.setId(UUID.randomUUID().toString());
                alert.setBudgetId(budget.getId());
                alert.setType(AlertType.UTILIZATION_THRESHOLD);
                alert.setMessage(String.format("Budget utilization reached %.1f%%", threshold * 100));
                alert.setThreshold(threshold);
                alert.setCurrentValue(utilizationPercentage);
                alert.setCreatedAt(LocalDateTime.now());
                alert.setStatus(AlertStatus.ACTIVE);
                
                budgetAlerts.get(budget.getId()).add(alert);
                log.warn("Budget alert created: {} - {}", budget.getId(), alert.getMessage());
            }
        }
    }

    /**
     * Check if alert exists for threshold
     */
    private boolean hasAlertForThreshold(String budgetId, Double threshold) {
        return budgetAlerts.getOrDefault(budgetId, new ArrayList<>())
            .stream()
            .anyMatch(alert -> Math.abs(alert.getThreshold() - threshold) < 0.01);
    }

    /**
     * Calculate utilization percentage
     */
    private double calculateUtilizationPercentage(Budget budget) {
        if (budget.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return budget.getSpentAmount().divide(budget.getTotalAmount(), 4, BigDecimal.ROUND_HALF_UP)
            .doubleValue();
    }

    /**
     * Calculate spending rate (per day)
     */
    private BigDecimal calculateSpendingRate(Budget budget) {
        if (budget.getCreatedAt() == null) {
            return BigDecimal.ZERO;
        }
        
        long days = java.time.Duration.between(budget.getCreatedAt(), LocalDateTime.now()).toDays();
        if (days == 0) {
            return BigDecimal.ZERO;
        }
        
        return budget.getSpentAmount().divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate days remaining
     */
    private long calculateDaysRemaining(Budget budget) {
        if (budget.getEndDate() == null) {
            return -1; // No end date
        }
        
        return java.time.Duration.between(LocalDateTime.now(), budget.getEndDate()).toDays();
    }

    /**
     * Calculate projected end date
     */
    private LocalDateTime calculateProjectedEndDate(Budget budget) {
        BigDecimal spendingRate = calculateSpendingRate(budget);
        if (spendingRate.compareTo(BigDecimal.ZERO) == 0) {
            return null; // No spending rate
        }
        
        BigDecimal remainingDays = budget.getRemainingAmount().divide(spendingRate, 0, BigDecimal.ROUND_UP);
        return LocalDateTime.now().plusDays(remainingDays.longValue());
    }

    /**
     * Calculate category breakdown
     */
    private Map<String, BigDecimal> calculateCategoryBreakdown(List<BudgetTransaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .collect(Collectors.groupingBy(
                BudgetTransaction::getCategory,
                Collectors.reducing(BigDecimal.ZERO, BudgetTransaction::getAmount, BigDecimal::add)
            ));
    }

    /**
     * Calculate monthly spending
     */
    private Map<String, BigDecimal> calculateMonthlySpending(List<BudgetTransaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .collect(Collectors.groupingBy(
                t -> t.getTransactionDate().toLocalDate().withDayOfMonth(1).toString(),
                Collectors.reducing(BigDecimal.ZERO, BudgetTransaction::getAmount, BigDecimal::add)
            ));
    }

    /**
     * Calculate top expenses
     */
    private List<BudgetTransaction> calculateTopExpenses(List<BudgetTransaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .sorted(Comparator.comparing(BudgetTransaction::getAmount).reversed())
            .limit(10)
            .collect(Collectors.toList());
    }

    // Data classes
    public static class Budget {
        private String id;
        private String name;
        private String description;
        private BigDecimal totalAmount;
        private BigDecimal remainingAmount;
        private BigDecimal spentAmount;
        private String category;
        private UUID createdBy;
        private String createdByName;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BudgetStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime closedAt;
        private String closeReason;
        private List<Double> alertThresholds;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getRemainingAmount() { return remainingAmount; }
        public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

        public BigDecimal getSpentAmount() { return spentAmount; }
        public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public UUID getCreatedBy() { return createdBy; }
        public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public BudgetStatus getStatus() { return status; }
        public void setStatus(BudgetStatus status) { this.status = status; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public LocalDateTime getClosedAt() { return closedAt; }
        public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

        public String getCloseReason() { return closeReason; }
        public void setCloseReason(String closeReason) { this.closeReason = closeReason; }

        public List<Double> getAlertThresholds() { return alertThresholds; }
        public void setAlertThresholds(List<Double> alertThresholds) { this.alertThresholds = alertThresholds; }
    }

    public static class BudgetTransaction {
        private String id;
        private String budgetId;
        private String description;
        private BigDecimal amount;
        private TransactionType type;
        private String category;
        private UUID recordedBy;
        private String recordedByName;
        private String referenceId;
        private LocalDateTime transactionDate;
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

        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public UUID getRecordedBy() { return recordedBy; }
        public void setRecordedBy(UUID recordedBy) { this.recordedBy = recordedBy; }

        public String getRecordedByName() { return recordedByName; }
        public void setRecordedByName(String recordedByName) { this.recordedByName = recordedByName; }

        public String getReferenceId() { return referenceId; }
        public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

        public LocalDateTime getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class BudgetAlert {
        private String id;
        private String budgetId;
        private AlertType type;
        private String message;
        private Double threshold;
        private Double currentValue;
        private LocalDateTime createdAt;
        private AlertStatus status;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getBudgetId() { return budgetId; }
        public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

        public AlertType getType() { return type; }
        public void setType(AlertType type) { this.type = type; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Double getThreshold() { return threshold; }
        public void setThreshold(Double threshold) { this.threshold = threshold; }

        public Double getCurrentValue() { return currentValue; }
        public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public AlertStatus getStatus() { return status; }
        public void setStatus(AlertStatus status) { this.status = status; }
    }

    public static class BudgetSummary {
        private Budget budget;
        private int totalTransactions;
        private BigDecimal totalExpenses;
        private BigDecimal totalIncome;
        private BigDecimal averageTransactionAmount;
        private BigDecimal spendingRate;
        private long daysRemaining;
        private double utilizationPercentage;

        // Getters and setters
        public Budget getBudget() { return budget; }
        public void setBudget(Budget budget) { this.budget = budget; }

        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

        public BigDecimal getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

        public BigDecimal getAverageTransactionAmount() { return averageTransactionAmount; }
        public void setAverageTransactionAmount(BigDecimal averageTransactionAmount) { this.averageTransactionAmount = averageTransactionAmount; }

        public BigDecimal getSpendingRate() { return spendingRate; }
        public void setSpendingRate(BigDecimal spendingRate) { this.spendingRate = spendingRate; }

        public long getDaysRemaining() { return daysRemaining; }
        public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }

        public double getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }
    }

    public static class BudgetAnalytics {
        private String budgetId;
        private BigDecimal totalAmount;
        private BigDecimal spentAmount;
        private BigDecimal remainingAmount;
        private double utilizationPercentage;
        private BigDecimal spendingRate;
        private LocalDateTime projectedEndDate;
        private Map<String, BigDecimal> categoryBreakdown;
        private Map<String, BigDecimal> monthlySpending;
        private List<BudgetTransaction> topExpenses;
        private int alertsCount;

        // Getters and setters
        public String getBudgetId() { return budgetId; }
        public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getSpentAmount() { return spentAmount; }
        public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

        public BigDecimal getRemainingAmount() { return remainingAmount; }
        public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

        public double getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

        public BigDecimal getSpendingRate() { return spendingRate; }
        public void setSpendingRate(BigDecimal spendingRate) { this.spendingRate = spendingRate; }

        public LocalDateTime getProjectedEndDate() { return projectedEndDate; }
        public void setProjectedEndDate(LocalDateTime projectedEndDate) { this.projectedEndDate = projectedEndDate; }

        public Map<String, BigDecimal> getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(Map<String, BigDecimal> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

        public Map<String, BigDecimal> getMonthlySpending() { return monthlySpending; }
        public void setMonthlySpending(Map<String, BigDecimal> monthlySpending) { this.monthlySpending = monthlySpending; }

        public List<BudgetTransaction> getTopExpenses() { return topExpenses; }
        public void setTopExpenses(List<BudgetTransaction> topExpenses) { this.topExpenses = topExpenses; }

        public int getAlertsCount() { return alertsCount; }
        public void setAlertsCount(int alertsCount) { this.alertsCount = alertsCount; }
    }

    public enum BudgetStatus {
        ACTIVE, CLOSED, SUSPENDED
    }

    public enum TransactionType {
        INCOME, EXPENSE, TRANSFER
    }

    public enum AlertType {
        UTILIZATION_THRESHOLD, BUDGET_EXCEEDED, LOW_FUNDS
    }

    public enum AlertStatus {
        ACTIVE, ACKNOWLEDGED, RESOLVED
    }
}


