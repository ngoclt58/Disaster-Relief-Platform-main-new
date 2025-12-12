package com.relief.repository;

import com.relief.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action")
    Page<AuditLog> findByAction(@Param("action") String action, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId")
    Page<AuditLog> findByUserId(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.userId = :userId")
    Page<AuditLog> findByActionAndUserId(@Param("action") String action, @Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :fromDate AND a.timestamp <= :toDate")
    Page<AuditLog> findByDateRange(@Param("fromDate") java.time.LocalDateTime fromDate, 
                                  @Param("toDate") java.time.LocalDateTime toDate, 
                                  Pageable pageable);
    
    // Analytics queries
    long countByTimestampBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countDistinctUserIdByTimestampBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
