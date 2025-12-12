package com.relief.repository.indoor;

import com.relief.domain.indoor.IndoorNode;
import com.relief.domain.indoor.IndoorNodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for indoor nodes
 */
@Repository
public interface IndoorNodeRepository extends JpaRepository<IndoorNode, Long> {
    
    /**
     * Find nodes by indoor map
     */
    List<IndoorNode> findByIndoorMapId(Long indoorMapId);
    
    /**
     * Find nodes by type
     */
    List<IndoorNode> findByNodeType(IndoorNodeType nodeType);
    
    /**
     * Find accessible nodes
     */
    List<IndoorNode> findByIsAccessibleTrue();
    
    /**
     * Find nodes by floor level
     */
    List<IndoorNode> findByFloorLevel(Integer floorLevel);
    
    /**
     * Find nodes by map and floor
     */
    List<IndoorNode> findByIndoorMapIdAndFloorLevel(Long indoorMapId, Integer floorLevel);
    
    /**
     * Find emergency exits
     */
    List<IndoorNode> findByIsEmergencyExitTrue();
    
    /**
     * Find elevators
     */
    List<IndoorNode> findByIsElevatorTrue();
    
    /**
     * Find stairs
     */
    List<IndoorNode> findByIsStairsTrue();
    
    /**
     * Find nodes by map and type
     */
    List<IndoorNode> findByIndoorMapIdAndNodeType(Long indoorMapId, IndoorNodeType nodeType);
    
    /**
     * Find nodes by map and accessibility
     */
    List<IndoorNode> findByIndoorMapIdAndIsAccessibleTrue(Long indoorMapId);
    
    /**
     * Find nodes within bounds
     */
    @Query(value = """
        SELECT * FROM indoor_nodes 
        WHERE ST_Within(position, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        AND indoor_map_id = :indoorMapId
        ORDER BY name
        """, nativeQuery = true)
    List<IndoorNode> findWithinBounds(
        @Param("indoorMapId") Long indoorMapId,
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Find nodes near a point
     */
    @Query(value = """
        SELECT * FROM indoor_nodes 
        WHERE ST_DWithin(position, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius)
        AND indoor_map_id = :indoorMapId
        ORDER BY ST_Distance(position, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
        """, nativeQuery = true)
    List<IndoorNode> findNearPoint(
        @Param("indoorMapId") Long indoorMapId,
        @Param("longitude") double longitude,
        @Param("latitude") double latitude,
        @Param("radius") double radius
    );
    
    /**
     * Find nodes by name containing
     */
    List<IndoorNode> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find nodes by node ID
     */
    Optional<IndoorNode> findByNodeId(String nodeId);
    
    /**
     * Find nodes by map and node ID
     */
    Optional<IndoorNode> findByIndoorMapIdAndNodeId(Long indoorMapId, String nodeId);
    
    /**
     * Get node statistics for a map
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_nodes,
            COUNT(CASE WHEN node_type = 'ROOM' THEN 1 END) as rooms,
            COUNT(CASE WHEN node_type = 'CORRIDOR' THEN 1 END) as corridors,
            COUNT(CASE WHEN node_type = 'STAIRS' THEN 1 END) as stairs,
            COUNT(CASE WHEN node_type = 'ELEVATOR' THEN 1 END) as elevators,
            COUNT(CASE WHEN is_accessible = true THEN 1 END) as accessible_nodes,
            COUNT(CASE WHEN is_emergency_exit = true THEN 1 END) as emergency_exits,
            AVG(capacity) as avg_capacity,
            AVG(current_occupancy) as avg_occupancy
        FROM indoor_nodes 
        WHERE indoor_map_id = :indoorMapId
        """, nativeQuery = true)
    IndoorNodeStatistics getNodeStatistics(@Param("indoorMapId") Long indoorMapId);
    
    /**
     * Interface for indoor node statistics projection
     */
    interface IndoorNodeStatistics {
        Long getTotalNodes();
        Long getRooms();
        Long getCorridors();
        Long getStairs();
        Long getElevators();
        Long getAccessibleNodes();
        Long getEmergencyExits();
        Double getAvgCapacity();
        Double getAvgOccupancy();
    }
}



