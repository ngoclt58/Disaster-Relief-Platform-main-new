package com.relief.service;

import com.relief.entity.Task;
import com.relief.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import com.relief.exception.BadRequestException;
import com.relief.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskStateService {

    private final TaskRepository taskRepository;

    private static final List<String> VALID_STATUSES = Arrays.asList(
            "new", "assigned", "picked_up", "delivered", "could_not_deliver", "cancelled"
    );

    private static final List<String> VALID_TRANSITIONS = Arrays.asList(
            "new->assigned",
            "assigned->picked_up",
            "picked_up->delivered",
            "picked_up->could_not_deliver",
            "assigned->cancelled",
            "picked_up->cancelled"
    );

    public Task updateTaskStatus(UUID taskId, String newStatus, LocalDateTime eta) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!VALID_STATUSES.contains(newStatus)) {
            throw new BadRequestException("Invalid status: " + newStatus);
        }

        String transition = task.getStatus() + "->" + newStatus;
        if (!VALID_TRANSITIONS.contains(transition)) {
            throw new BadRequestException("Invalid transition: " + transition);
        }

        task.setStatus(newStatus);
        if (eta != null) {
            task.setEta(eta);
        }
        return taskRepository.save(task);
    }

    public boolean canTransition(String fromStatus, String toStatus) {
        String transition = fromStatus + "->" + toStatus;
        return VALID_TRANSITIONS.contains(transition);
    }
}


