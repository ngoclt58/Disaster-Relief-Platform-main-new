package com.relief.repository;

import com.relief.entity.Task;
import com.relief.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :assigneeId")
    Page<Task> findByAssigneeId(@Param("assigneeId") UUID assigneeId, Pageable pageable);

    Page<Task> findByStatus(String status, Pageable pageable);
    
    // Admin-specific queries
    long countByStatus(String status);
    
    long countByStatusIn(String... statuses);
    
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> _countByStatusGrouped();
    
    default java.util.Map<String, Long> countByStatus() {
        return _countByStatusGrouped().stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));
    }
    
    // Analytics queries
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByStatusAndUpdatedAtBetween(String status, java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByStatusInAndUpdatedAtBetween(java.util.List<String> statuses, java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.updatedAt BETWEEN :start AND :end GROUP BY t.status")
    List<Object[]> _countByStatusGroupedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    default java.util.Map<String, Long> countByStatusAndUpdatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return _countByStatusGroupedBetween(start, end).stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));
    }
    
    // Smart automation queries
    @Query("SELECT t FROM Task t WHERE t.eta < :now AND t.status IN ('assigned', 'picked_up') ORDER BY t.eta ASC")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);
    
    default List<Task> findOverdueTasks() {
        return findOverdueTasks(LocalDateTime.now());
    }
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee = :assignee AND t.status IN :statuses")
    long countByAssigneeAndStatusIn(@Param("assignee") User assignee, @Param("statuses") List<String> statuses);

    /**
     * Find tasks for a given assignee in any of the provided statuses.
     * Used for performance history in {@link com.relief.service.task.SkillBasedMatchingService}.
     */
    List<Task> findByAssigneeAndStatusIn(User assignee, List<String> statuses);

    /**
     * Find tasks that are currently unassigned with the given status.
     * Used for auto-assignment of new tasks.
     */
    List<Task> findByAssigneeIsNullAndStatus(String status);
    
    /**
     * Find tasks created between two dates.
     * Used for performance analytics.
     */
    List<Task> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find tasks with any of the provided statuses.
     * Used for performance analytics.
     */
    List<Task> findByStatusIn(List<String> statuses);
    
    /**
     * Find tasks with a specific status (returns List instead of Page).
     * Used for performance analytics.
     */
    List<Task> findByStatus(String status);
    
    /**
     * Find tasks by request ID.
     * Used for dynamic task creation from historical patterns.
     */
    @Query("SELECT t FROM Task t WHERE t.request.id = :requestId")
    List<Task> findByRequestId(@Param("requestId") UUID requestId);
}


