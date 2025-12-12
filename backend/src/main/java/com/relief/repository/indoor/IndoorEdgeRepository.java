package com.relief.repository.indoor;

import com.relief.domain.indoor.IndoorEdge;
import com.relief.domain.indoor.IndoorEdgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for indoor edges
 */
@Repository
public interface IndoorEdgeRepository extends JpaRepository<IndoorEdge, Long> {
    
    /**
     * Find edges by indoor map
     */
    List<IndoorEdge> findByIndoorMapId(Long indoorMapId);
    
    /**
     * Find edges by type
     */
    List<IndoorEdge> findByEdgeType(IndoorEdgeType edgeType);
    
    /**
     * Find accessible edges
     */
    List<IndoorEdge> findByIsAccessibleTrue();
    
    /**
     * Find bidirectional edges
     */
    List<IndoorEdge> findByIsBidirectionalTrue();
    
    /**
     * Find emergency routes
     */
    List<IndoorEdge> findByIsEmergencyRouteTrue();
    
    /**
     * Find restricted edges
     */
    List<IndoorEdge> findByIsRestrictedTrue();
    
    /**
     * Find edges by map and type
     */
    List<IndoorEdge> findByIndoorMapIdAndEdgeType(Long indoorMapId, IndoorEdgeType edgeType);
    
    /**
     * Find edges by map and accessibility
     */
    List<IndoorEdge> findByIndoorMapIdAndIsAccessibleTrue(Long indoorMapId);
    
    /**
     * Find edges from a node
     */
    List<IndoorEdge> findByFromNodeId(Long fromNodeId);
    
    /**
     * Find edges to a node
     */
    List<IndoorEdge> findByToNodeId(Long toNodeId);
    
    /**
     * Find edges between two nodes
     */
    List<IndoorEdge> findByFromNodeIdAndToNodeId(Long fromNodeId, Long toNodeId);
    
    /**
     * Find edges by map and from node
     */
    List<IndoorEdge> findByIndoorMapIdAndFromNodeId(Long indoorMapId, Long fromNodeId);
    
    /**
     * Find edges by map and to node
     */
    List<IndoorEdge> findByIndoorMapIdAndToNodeId(Long indoorMapId, Long toNodeId);
    
    /**
     * Find edges by edge ID
     */
    List<IndoorEdge> findByEdgeId(String edgeId);
    
    /**
     * Find edges by map and edge ID
     */
    List<IndoorEdge> findByIndoorMapIdAndEdgeId(Long indoorMapId, String edgeId);
    
    /**
     * Find edges by name containing
     */
    List<IndoorEdge> findByNameContainingIgnoreCase(String name);
    
    /**
     * Get edge statistics for a map
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_edges,
            COUNT(CASE WHEN edge_type = 'CORRIDOR' THEN 1 END) as corridors,
            COUNT(CASE WHEN edge_type = 'DOORWAY' THEN 1 END) as doorways,
            COUNT(CASE WHEN edge_type = 'STAIRS' THEN 1 END) as stairs,
            COUNT(CASE WHEN edge_type = 'ELEVATOR' THEN 1 END) as elevators,
            COUNT(CASE WHEN is_accessible = true THEN 1 END) as accessible_edges,
            COUNT(CASE WHEN is_bidirectional = true THEN 1 END) as bidirectional_edges,
            COUNT(CASE WHEN is_emergency_route = true THEN 1 END) as emergency_routes,
            AVG(distance) as avg_distance,
            AVG(weight) as avg_weight
        FROM indoor_edges 
        WHERE indoor_map_id = :indoorMapId
        """, nativeQuery = true)
    IndoorEdgeStatistics getEdgeStatistics(@Param("indoorMapId") Long indoorMapId);
    
    /**
     * Interface for indoor edge statistics projection
     */
    interface IndoorEdgeStatistics {
        Long getTotalEdges();
        Long getCorridors();
        Long getDoorways();
        Long getStairs();
        Long getElevators();
        Long getAccessibleEdges();
        Long getBidirectionalEdges();
        Long getEmergencyRoutes();
        Double getAvgDistance();
        Double getAvgWeight();
    }
}



