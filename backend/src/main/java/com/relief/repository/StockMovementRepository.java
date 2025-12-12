package com.relief.repository;

import com.relief.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    @Query("SELECT m FROM StockMovement m ORDER BY m.createdAt DESC")
    List<StockMovement> findAllOrderByCreatedAtDesc();

    @Query("SELECT m FROM StockMovement m WHERE m.hub.id = :hubId ORDER BY m.createdAt DESC")
    List<StockMovement> findByHubIdOrderByCreatedAtDesc(@Param("hubId") UUID hubId);

    @Query("SELECT m FROM StockMovement m WHERE m.item.id = :itemId ORDER BY m.createdAt DESC")
    List<StockMovement> findByItemIdOrderByCreatedAtDesc(@Param("itemId") UUID itemId);

    @Query("SELECT m FROM StockMovement m WHERE m.hub.id = :hubId AND m.item.id = :itemId ORDER BY m.createdAt DESC")
    List<StockMovement> findByHubIdAndItemIdOrderByCreatedAtDesc(@Param("hubId") UUID hubId, @Param("itemId") UUID itemId);
}

