package com.relief.service.optimization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for intelligent distribution of resources based on need severity and availability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceAllocationService {

    private final Map<String, ResourceAllocation> allocations = new ConcurrentHashMap<>();

    /**
     * Allocate resources intelligently
     */
    public ResourceAllocation allocateResources(
            List<ResourceNeed> needs,
            Map<String, Integer> availableResources) {

        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setId(UUID.randomUUID().toString());
        allocation.setAllocatedAt(LocalDateTime.now());

        // Sort needs by severity and urgency
        List<ResourceNeed> sortedNeeds = needs.stream()
                .sorted(Comparator
                        .comparing(ResourceNeed::getSeverity).reversed()
                        .thenComparing(ResourceNeed::getUrgency).reversed())
                .collect(Collectors.toList());

        List<AllocationPlan> plans = new ArrayList<>();
        
        for (ResourceNeed need : sortedNeeds) {
            AllocationPlan plan = allocateForNeed(need, availableResources);
            if (plan != null && plan.getQuantity() > 0) {
                plans.add(plan);
                // Deduct allocated resources
                String resourceType = plan.getResourceType();
                availableResources.put(resourceType,
                        availableResources.get(resourceType) - plan.getQuantity());
            }
        }

        allocation.setPlans(plans);
        allocation.setTotalAllocated(calculateTotalAllocated(plans));
        allocation.setAllocationEfficiency(calculateEfficiency(needs, plans));
        allocation.setUnmetNeeds(getUnmetNeeds(needs, plans));

        allocations.put(allocation.getId(), allocation);
        
        log.info("Allocated resources for {} needs - Efficiency: {}%", 
                needs.size(), allocation.getAllocationEfficiency());
        
        return allocation;
    }

    private AllocationPlan allocateForNeed(ResourceNeed need, Map<String, Integer> availableResources) {
        String resourceType = need.getResourceType();
        int available = availableResources.getOrDefault(resourceType, 0);

        if (available == 0) {
            return null;
        }

        AllocationPlan plan = new AllocationPlan();
        plan.setNeedId(need.getId());
        plan.setResourceType(resourceType);
        plan.setLocation(need.getLocation());
        plan.setSeverity(need.getSeverity());
        
        // Allocate based on severity and availability
        int quantityToAllocate = Math.min(need.getQuantity(), available);
        
        // Adjust allocation based on severity
        if (need.getSeverity() >= 8) {
            // High severity - try to allocate more
            quantityToAllocate = Math.min(need.getQuantity(), available);
        } else if (need.getSeverity() >= 5) {
            // Medium severity - allocate proportionally
            quantityToAllocate = (int) (Math.min(need.getQuantity(), available) * 0.8);
        } else {
            // Low severity - allocate minimum
            quantityToAllocate = Math.min((int) (need.getQuantity() * 0.5), available);
        }

        plan.setQuantity(quantityToAllocate);
        plan.setPercentageMet(quantityToAllocate / (double) need.getQuantity() * 100);
        plan.setPriority(calculatePriority(need));

        return plan;
    }

    private int calculateTotalAllocated(List<AllocationPlan> plans) {
        return plans.stream().mapToInt(AllocationPlan::getQuantity).sum();
    }

    private double calculateEfficiency(List<ResourceNeed> needs, List<AllocationPlan> plans) {
        Map<String, Integer> needTotals = new HashMap<>();
        Map<String, Integer> allocatedTotals = new HashMap<>();

        for (ResourceNeed need : needs) {
            needTotals.put(need.getResourceType(),
                    needTotals.getOrDefault(need.getResourceType(), 0) + need.getQuantity());
        }

        for (AllocationPlan plan : plans) {
            allocatedTotals.put(plan.getResourceType(),
                    allocatedTotals.getOrDefault(plan.getResourceType(), 0) + plan.getQuantity());
        }

        double totalEfficiency = 0.0;
        int count = 0;

        for (String resourceType : needTotals.keySet()) {
            int needTotal = needTotals.get(resourceType);
            int allocatedTotal = allocatedTotals.getOrDefault(resourceType, 0);
            double efficiency = (allocatedTotal / (double) needTotal) * 100;
            totalEfficiency += efficiency;
            count++;
        }

        return count > 0 ? totalEfficiency / count : 0.0;
    }

    private List<String> getUnmetNeeds(List<ResourceNeed> needs, List<AllocationPlan> plans) {
        Map<String, AllocationPlan> planMap = plans.stream()
                .collect(Collectors.toMap(AllocationPlan::getNeedId, p -> p));

        List<String> unmet = new ArrayList<>();
        for (ResourceNeed need : needs) {
            AllocationPlan plan = planMap.get(need.getId());
            if (plan == null || plan.getPercentageMet() < 80) {
                unmet.add(need.getId());
            }
        }

        return unmet;
    }

    private String calculatePriority(ResourceNeed need) {
        if (need.getSeverity() >= 8 && need.getUrgency() >= 8) return "CRITICAL";
        if (need.getSeverity() >= 6 || need.getUrgency() >= 6) return "HIGH";
        if (need.getSeverity() >= 4 || need.getUrgency() >= 4) return "MEDIUM";
        return "LOW";
    }

    public ResourceAllocation getAllocation(String allocationId) {
        return allocations.get(allocationId);
    }

    public List<ResourceAllocation> getAllocations() {
        return new ArrayList<>(allocations.values());
    }

    // Inner classes
    @lombok.Data
    public static class ResourceAllocation {
        private String id;
        private LocalDateTime allocatedAt;
        private List<AllocationPlan> plans;
        private int totalAllocated;
        private double allocationEfficiency;
        private List<String> unmetNeeds;
    }

    @lombok.Data
    public static class AllocationPlan {
        private String needId;
        private String resourceType;
        private String location;
        private int quantity;
        private double percentageMet;
        private int severity;
        private String priority;
    }

    @lombok.Data
    public static class ResourceNeed {
        private String id;
        private String resourceType;
        private int quantity;
        private int severity;
        private int urgency;
        private String location;

        public ResourceNeed(String id, String resourceType, int quantity, int severity, int urgency, String location) {
            this.id = id;
            this.resourceType = resourceType;
            this.quantity = quantity;
            this.severity = severity;
            this.urgency = urgency;
            this.location = location;
        }
    }
}

