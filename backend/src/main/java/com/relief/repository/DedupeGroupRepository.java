package com.relief.repository;

import com.relief.entity.DedupeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DedupeGroupRepository extends JpaRepository<DedupeGroup, UUID> {
}





