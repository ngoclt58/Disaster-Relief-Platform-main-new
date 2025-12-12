package com.relief.controller.optimization;

import com.relief.service.optimization.LoadBalancingService;
import com.relief.service.optimization.LoadBalancingService.WorkloadAssignment;
import com.relief.service.optimization.LoadBalancingService.Worker;
import com.relief.service.optimization.LoadBalancingService.TaskItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/optimization/load-balancing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Load Balancing", description = "AI-driven task assignment optimization")
public class LoadBalancingController {

    private final LoadBalancingService loadBalancingService;

    @PostMapping("/balance")
    @Operation(summary = "Balance workload across workers")
    public ResponseEntity<WorkloadAssignment> balanceWorkload(
            @RequestBody List<Worker> availableWorkers,
            @RequestBody List<TaskItem> pendingTasks) {
        
        WorkloadAssignment assignment = loadBalancingService.balanceWorkload(availableWorkers, pendingTasks);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/assignments/{assignmentId}")
    @Operation(summary = "Get workload assignment")
    public ResponseEntity<WorkloadAssignment> getAssignment(@PathVariable String assignmentId) {
        WorkloadAssignment assignment = loadBalancingService.getAssignment(assignmentId);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/assignments")
    @Operation(summary = "Get all assignments")
    public ResponseEntity<List<WorkloadAssignment>> getAssignments() {
        List<WorkloadAssignment> assignments = loadBalancingService.getAssignments();
        return ResponseEntity.ok(assignments);
    }
}



