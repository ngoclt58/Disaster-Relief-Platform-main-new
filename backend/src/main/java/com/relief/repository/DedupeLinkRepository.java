package com.relief.repository;

import com.relief.entity.DedupeLink;
import com.relief.entity.DedupeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DedupeLinkRepository extends JpaRepository<DedupeLink, UUID> {
    List<DedupeLink> findByGroup(DedupeGroup group);
}





