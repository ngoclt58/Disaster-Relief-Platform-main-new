package com.relief.repository;

import com.relief.entity.WorkflowTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplateEntity, UUID> {
    
    Optional<WorkflowTemplateEntity> findByName(String name);
    
    List<WorkflowTemplateEntity> findByIsActiveTrue();
    
    boolean existsByName(String name);
}

