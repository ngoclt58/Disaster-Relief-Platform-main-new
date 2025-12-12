package com.relief.repository;

import com.relief.entity.WorkflowExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecutionEntity, UUID> {
    
    Optional<WorkflowExecutionEntity> findByExecutionId(String executionId);
    
    List<WorkflowExecutionEntity> findByRequestId(UUID requestId);
    
    List<WorkflowExecutionEntity> findByWorkflowType(String workflowType);
    
    List<WorkflowExecutionEntity> findByStatus(String status);
    
    List<WorkflowExecutionEntity> findByRequestIdOrderByStartTimeDesc(UUID requestId);
}

