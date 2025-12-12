package com.relief.repository;

import com.relief.entity.InventoryHub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryHubRepository extends JpaRepository<InventoryHub, UUID> {
}



