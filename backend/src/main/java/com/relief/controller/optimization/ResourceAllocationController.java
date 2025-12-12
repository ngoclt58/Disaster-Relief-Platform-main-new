package com.relief.controller.optimization;

import com.relief.service.optimization.ResourceAllocationService;
import com.relief.service.optimization.ResourceAllocationService.ResourceAllocation;
import com.relief.service.optimization.ResourceAllocationService.ResourceNeed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/optimization/allocation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resource Allocation", description = "Intelligent distribution of resources")
public class ResourceAllocationController {

    private final ResourceAllocationService allocationService;

    @PostMapping("/allocate")
    @Operation(summary = "Allocate resources intelligently")
    public ResponseEntity<ResourceAllocation> allocateResources(
            @RequestBody List<ResourceNeed> needs,
            @RequestBody Map<String, Integer> availableResources) {
        
        ResourceAllocation allocation = allocationService.allocateResources(needs, availableResources);
        return ResponseEntity.ok(allocation);
    }

    @GetMapping("/allocations/{allocationId}")
    @Operation(summary = "Get allocation")
    public ResponseEntity<ResourceAllocation> getAllocation(@PathVariable String allocationId) {
        ResourceAllocation allocation = allocationService.getAllocation(allocationId);
        return ResponseEntity.ok(allocation);
    }

    @GetMapping("/allocations")
    @Operation(summary = "Get all allocations")
    public ResponseEntity<List<ResourceAllocation>> getAllocations() {
        List<ResourceAllocation> allocations = allocationService.getAllocations();
        return ResponseEntity.ok(allocations);
    }
}



