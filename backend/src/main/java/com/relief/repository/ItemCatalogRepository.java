package com.relief.repository;

import com.relief.entity.ItemCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemCatalogRepository extends JpaRepository<ItemCatalog, UUID> {

    Optional<ItemCatalog> findByCode(String code);
}



