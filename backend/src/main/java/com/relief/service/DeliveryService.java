package com.relief.service;

import com.relief.entity.Delivery;
import com.relief.entity.Task;
import com.relief.repository.DeliveryRepository;
import com.relief.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import com.relief.exception.BadRequestException;
import com.relief.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final TaskRepository taskRepository;

    public Delivery createDelivery(UUID taskId, String recipientName, String recipientPhone, 
                                 String notes, UUID proofMediaId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!"picked_up".equals(task.getStatus())) {
            throw new BadRequestException("Task must be in 'picked_up' status to create delivery");
        }

        Delivery delivery = Delivery.builder()
                .task(task)
                .deliveredAt(LocalDateTime.now())
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .notes(notes)
                .proofMediaId(proofMediaId)
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Update task status to delivered
        task.setStatus("delivered");
        taskRepository.save(task);

        return savedDelivery;
    }

    public Delivery getDeliveryByTaskId(UUID taskId) {
        return deliveryRepository.findByTaskId(taskId);
    }
}


