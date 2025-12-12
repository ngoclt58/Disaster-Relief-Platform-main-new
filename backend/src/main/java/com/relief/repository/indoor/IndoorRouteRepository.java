package com.relief.repository.indoor;

import com.relief.domain.indoor.IndoorRoute;
import com.relief.domain.indoor.IndoorRouteType;
import com.relief.domain.indoor.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for indoor routes
 */
@Repository
public interface IndoorRouteRepository extends JpaRepository<IndoorRoute, Long> {
    
    /**
     * Find routes by indoor map
     */
    List<IndoorRoute> findByIndoorMapId(Long indoorMapId);
    
    /**
     * Find routes by type
     */
    List<IndoorRoute> findByRouteType(IndoorRouteType routeType);
    
    /**
     * Find accessible routes
     */
    List<IndoorRoute> findByIsAccessibleTrue();
    
    /**
     * Find emergency routes
     */
    List<IndoorRoute> findByIsEmergencyRouteTrue();
    
    /**
     * Find restricted routes
     */
    List<IndoorRoute> findByIsRestrictedTrue();
    
    /**
     * Find routes by difficulty level
     */
    List<IndoorRoute> findByDifficultyLevel(DifficultyLevel difficultyLevel);
    
    /**
     * Find routes by map and type
     */
    List<IndoorRoute> findByIndoorMapIdAndRouteType(Long indoorMapId, IndoorRouteType routeType);
    
    /**
     * Find routes by map and accessibility
     */
    List<IndoorRoute> findByIndoorMapIdAndIsAccessibleTrue(Long indoorMapId);

    /**
     * Find emergency routes by map
     */
    List<IndoorRoute> findByIndoorMapIdAndIsEmergencyRouteTrue(Long indoorMapId);
    
    /**
     * Find routes from a node
     */
    List<IndoorRoute> findByFromNodeId(Long fromNodeId);
    
    /**
     * Find routes to a node
     */
    List<IndoorRoute> findByToNodeId(Long toNodeId);
    
    /**
     * Find routes between two nodes
     */
    List<IndoorRoute> findByFromNodeIdAndToNodeId(Long fromNodeId, Long toNodeId);
    
    /**
     * Find routes by map and from node
     */
    List<IndoorRoute> findByIndoorMapIdAndFromNodeId(Long indoorMapId, Long fromNodeId);
    
    /**
     * Find routes by map and to node
     */
    List<IndoorRoute> findByIndoorMapIdAndToNodeId(Long indoorMapId, Long toNodeId);
    
    /**
     * Find routes by route ID
     */
    List<IndoorRoute> findByRouteId(String routeId);
    
    /**
     * Find routes by map and route ID
     */
    List<IndoorRoute> findByIndoorMapIdAndRouteId(Long indoorMapId, String routeId);
    
    /**
     * Find routes by name containing
     */
    List<IndoorRoute> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find routes by creator
     */
    List<IndoorRoute> findByCreatedBy(String createdBy);
    
    /**
     * Find routes by map and creator
     */
    List<IndoorRoute> findByIndoorMapIdAndCreatedBy(Long indoorMapId, String createdBy);
    
    /**
     * Find routes by date range
     */
    @Query("SELECT r FROM IndoorRoute r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<IndoorRoute> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find routes by map and date range
     */
    @Query("SELECT r FROM IndoorRoute r WHERE r.indoorMap.id = :indoorMapId AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<IndoorRoute> findByIndoorMapIdAndCreatedAtBetween(
        @Param("indoorMapId") Long indoorMapId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Get route statistics for a map
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_routes,
            COUNT(CASE WHEN route_type = 'SHORTEST_PATH' THEN 1 END) as shortest_routes,
            COUNT(CASE WHEN route_type = 'FASTEST_PATH' THEN 1 END) as fastest_routes,
            COUNT(CASE WHEN route_type = 'ACCESSIBLE_PATH' THEN 1 END) as accessible_routes,
            COUNT(CASE WHEN route_type = 'EMERGENCY_EVACUATION' THEN 1 END) as emergency_routes,
            COUNT(CASE WHEN is_accessible = true THEN 1 END) as accessible_routes_count,
            COUNT(CASE WHEN is_emergency_route = true THEN 1 END) as emergency_routes_count,
            AVG(total_distance) as avg_distance,
            AVG(estimated_time) as avg_estimated_time
        FROM indoor_routes 
        WHERE indoor_map_id = :indoorMapId
        """, nativeQuery = true)
    IndoorRouteStatistics getRouteStatistics(@Param("indoorMapId") Long indoorMapId);
    
    /**
     * Interface for indoor route statistics projection
     */
    interface IndoorRouteStatistics {
        Long getTotalRoutes();
        Long getShortestRoutes();
        Long getFastestRoutes();
        Long getAccessibleRoutes();
        Long getEmergencyRoutes();
        Long getAccessibleRoutesCount();
        Long getEmergencyRoutesCount();
        Double getAvgDistance();
        Double getAvgEstimatedTime();
    }
}



