package com.relief.service;

import com.relief.entity.NeedsRequest;
import com.relief.entity.Task;
import com.relief.entity.User;
import com.relief.repository.NeedsRequestRepository;
import com.relief.repository.TaskRepository;
import com.relief.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.relief.realtime.RealtimeBroadcaster;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final NeedsRequestRepository needsRequestRepository;
    private final UserRepository userRepository;
    private final RealtimeBroadcaster broadcaster;

    public Task createTask(UUID requestId, UUID assigneeId, String plannedKitCode) {
        NeedsRequest request = needsRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));
        Task task = new Task();
        task.setRequest(request);
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            task.setAssignee(assignee);
            task.setStatus("assigned");
        } else {
            task.setStatus("new");
        }
        task.setPlannedKitCode(plannedKitCode);
        Task saved = taskRepository.save(task);
        broadcaster.broadcast("task.created", saved.getId());
        return saved;
    }

    public Task claimTask(UUID taskId, UUID userId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        task.setAssignee(user);
        task.setStatus("assigned");
        Task saved = taskRepository.save(task);
        broadcaster.broadcast("task.assigned", saved.getId());
        return saved;
    }

    public Task assignTask(UUID taskId, UUID assigneeId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task not found"));
        User assignee = userRepository.findById(assigneeId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        task.setAssignee(assignee);
        task.setStatus("assigned");
        Task saved = taskRepository.save(task);
        broadcaster.broadcast("task.updated", saved.getId());
        return saved;
    }

    public Task updateStatus(UUID taskId, String status, LocalDateTime eta) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setStatus(status);
        task.setEta(eta);
        return taskRepository.save(task);
    }

    public Page<Task> listMyTasks(UUID userId, int page, int size) {
        return taskRepository.findByAssigneeId(userId, PageRequest.of(page, size));
    }
}


