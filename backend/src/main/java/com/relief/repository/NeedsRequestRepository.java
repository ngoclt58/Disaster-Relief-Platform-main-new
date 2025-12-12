package com.relief.repository;

import com.relief.entity.NeedsRequest;
import org.locationtech.jts.geom.Point;
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
public interface NeedsRequestRepository extends JpaRepository<NeedsRequest, UUID> {

    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.household.residentUser.id = :userId")
    Page<NeedsRequest> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.createdBy.id = :userId ORDER BY nr.createdAt DESC")
    List<NeedsRequest> findByCreatedByIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
    
    // Smart automation queries
    @Query("""
           SELECT nr FROM NeedsRequest nr
           WHERE nr.geomPoint IS NOT NULL
             AND function('ST_DWithin', nr.geomPoint, :point, :radius) = true
             AND nr.createdAt >= :since
           ORDER BY nr.createdAt DESC
           """)
    List<NeedsRequest> findRecentRequestsInArea(@Param("point") Point point, @Param("radius") double radius, @Param("since") LocalDateTime since);
    
    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.severity >= 4 AND nr.status IN ('OPEN', 'IN_PROGRESS') ORDER BY nr.createdAt ASC")
    List<NeedsRequest> findHighPriorityRequests();
    
    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.status IN ('OPEN', 'IN_PROGRESS') AND nr.createdAt < :cutoff ORDER BY nr.createdAt ASC")
    List<NeedsRequest> findUnresolvedRequests(@Param("cutoff") LocalDateTime cutoff);
    
    default List<NeedsRequest> findUnresolvedRequests() {
        return findUnresolvedRequests(LocalDateTime.now().minusHours(2));
    }

    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.status = :status")
    Page<NeedsRequest> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.type = :type")
    Page<NeedsRequest> findByType(@Param("type") String type, Pageable pageable);

    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.severity >= :minSeverity")
    Page<NeedsRequest> findByMinSeverity(@Param("minSeverity") Integer minSeverity, Pageable pageable);

    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.createdAt >= :fromDate AND nr.createdAt <= :toDate")
    Page<NeedsRequest> findByDateRange(@Param("fromDate") LocalDateTime fromDate, 
                                       @Param("toDate") LocalDateTime toDate, 
                                       Pageable pageable);

    @Query(value = "SELECT * FROM needs_requests WHERE ST_DWithin(geom_point, ST_GeomFromText(:point, 4326), :radius)", 
           nativeQuery = true)
    List<NeedsRequest> findNearbyRequests(@Param("point") String point, @Param("radius") double radius);

    @Query(value = "SELECT * FROM needs_requests WHERE ST_Within(geom_point, ST_GeomFromText(:bbox, 4326))", 
           nativeQuery = true)
    Page<NeedsRequest> findWithinBoundingBox(@Param("bbox") String bbox, Pageable pageable);

    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.currentAssignee.id = :assigneeId")
    Page<NeedsRequest> findByAssignee(@Param("assigneeId") UUID assigneeId, Pageable pageable);

    @Query("SELECT COUNT(nr) FROM NeedsRequest nr WHERE nr.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(nr) FROM NeedsRequest nr WHERE nr.type = :type AND nr.status = :status")
    long countByTypeAndStatus(@Param("type") String type, @Param("status") String status);
    
    /**
     * Find requests by type, severity, and status not equal to specified value.
     * Used for dynamic task creation from historical patterns.
     */
    @Query("SELECT nr FROM NeedsRequest nr WHERE nr.type = :type AND nr.severity = :severity AND nr.status != :status")
    List<NeedsRequest> findByTypeAndSeverityAndStatusNot(@Param("type") String type, @Param("severity") Integer severity, @Param("status") String status);
    
    // Admin-specific queries
    @Query("SELECT nr.type, COUNT(nr) FROM NeedsRequest nr GROUP BY nr.type")
    List<Object[]> _countByTypeGrouped();
    
    default java.util.Map<String, Long> countByCategory() {
        return _countByTypeGrouped().stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));
    }
    
    // Analytics queries
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByStatusAndCreatedAtBetween(String status, java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    @Query("SELECT nr.type, COUNT(nr) FROM NeedsRequest nr WHERE nr.createdAt BETWEEN :start AND :end GROUP BY nr.type")
    List<Object[]> _countByTypeGroupedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    default java.util.Map<String, Long> countByCategoryAndCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return _countByTypeGroupedBetween(start, end).stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));
    }
    
    @Query("SELECT nr.severity, COUNT(nr) FROM NeedsRequest nr WHERE nr.createdAt BETWEEN :start AND :end GROUP BY nr.severity")
    List<Object[]> _countBySeverityGroupedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    default java.util.Map<Integer, Long> countBySeverityAndCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return _countBySeverityGroupedBetween(start, end).stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> ((Number) row[0]).intValue(),
                row -> ((Number) row[1]).longValue()
            ));
    }
    
    @Query("""
           SELECT COALESCE(w.code, 'UNKNOWN'), COUNT(nr)
           FROM NeedsRequest nr
           LEFT JOIN nr.household h
           LEFT JOIN h.ward w
           WHERE nr.createdAt BETWEEN :start AND :end
           GROUP BY COALESCE(w.code, 'UNKNOWN')
           """)
    List<Object[]> _countByRegionGroupedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    default java.util.Map<String, Long> countByRegionAndCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return _countByRegionGroupedBetween(start, end).stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));
    }
}
