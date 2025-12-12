package com.relief.repository;

import com.relief.entity.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, UUID> {

    @Query("SELECT s FROM InventoryStock s JOIN FETCH s.hub JOIN FETCH s.item WHERE s.hub.id = :hubId")
    List<InventoryStock> findByHubId(@Param("hubId") UUID hubId);

    @Query("SELECT s FROM InventoryStock s JOIN FETCH s.hub JOIN FETCH s.item WHERE s.item.id = :itemId")
    List<InventoryStock> findByItemId(@Param("itemId") UUID itemId);

    @Query("SELECT s FROM InventoryStock s JOIN FETCH s.hub JOIN FETCH s.item WHERE s.hub.id = :hubId AND s.item.id = :itemId")
    InventoryStock findByHubIdAndItemId(@Param("hubId") UUID hubId, @Param("itemId") UUID itemId);
    
    @Query("SELECT s FROM InventoryStock s JOIN FETCH s.hub JOIN FETCH s.item")
    @Override
    List<InventoryStock> findAll();
    
    // Admin-specific queries
    long countByQtyAvailableLessThan(Integer threshold);
}
