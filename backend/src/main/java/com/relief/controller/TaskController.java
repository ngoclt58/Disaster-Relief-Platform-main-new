package com.relief.controller;

import com.relief.entity.Task;
import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.security.RequiresPermission;
import com.relief.security.Permission;
import com.relief.service.TaskService;
import com.relief.service.TaskStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task lifecycle endpoints")
public class TaskController {

    private final TaskService taskService;
    private final TaskStateService taskStateService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a task from a request")
    @RequiresPermission(Permission.TASK_WRITE)
    public ResponseEntity<Task> create(@RequestParam UUID requestId,
                                       @RequestParam(required = false) UUID assigneeId,
                                       @RequestParam(required = false) String plannedKitCode) {
        return ResponseEntity.ok(taskService.createTask(requestId, assigneeId, plannedKitCode));
    }

    @PostMapping("/{id}:claim")
    @Operation(summary = "Claim a task for the current user")
    @RequiresPermission(Permission.TASK_CLAIM)
    public ResponseEntity<Task> claim(@PathVariable("id") UUID taskId,
                                      @AuthenticationPrincipal UserDetails principal) {
        User me = userRepository.findByEmail(principal.getUsername()).orElseGet(() ->
                userRepository.findByPhone(principal.getUsername()).orElseThrow());
        return ResponseEntity.ok(taskService.claimTask(taskId, me.getId()));
    }

    @PostMapping("/{id}:assign")
    @Operation(summary = "Assign a task to a user")
    @RequiresPermission(Permission.TASK_ASSIGN)
    public ResponseEntity<Task> assign(@PathVariable("id") UUID taskId,
                                       @RequestParam UUID assigneeId) {
        return ResponseEntity.ok(taskService.assignTask(taskId, assigneeId));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update task status and optional ETA")
    @RequiresPermission(Permission.TASK_WRITE)
    public ResponseEntity<Task> update(@PathVariable("id") UUID taskId,
                                       @RequestBody UpdateTaskRequest body) {
        return ResponseEntity.ok(taskStateService.updateTaskStatus(taskId, body.getStatus(), body.getEta()));
    }

    @GetMapping("/mine")
    @Operation(summary = "List my tasks")
    @RequiresPermission(Permission.TASK_READ)
    public ResponseEntity<Page<Task>> mine(@AuthenticationPrincipal UserDetails principal,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        User me = userRepository.findByEmail(principal.getUsername()).orElseGet(() ->
                userRepository.findByPhone(principal.getUsername()).orElseThrow());
        return ResponseEntity.ok(taskService.listMyTasks(me.getId(), page, size));
    }

    @Data
    public static class UpdateTaskRequest {
        @NotBlank
        private String status; // new/assigned/picked_up/delivered/could_not_deliver
        private LocalDateTime eta;
    }
}


