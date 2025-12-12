package com.relief.repository;

import com.relief.entity.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, UUID> {
    @Query("SELECT n FROM InAppNotification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    Page<InAppNotification> findByUserId(@Param("userId") UUID userId, Pageable pageable);
}



