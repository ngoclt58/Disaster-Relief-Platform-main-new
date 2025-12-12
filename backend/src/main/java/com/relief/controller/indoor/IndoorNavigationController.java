package com.relief.controller.indoor;

import com.relief.domain.indoor.*;
import com.relief.service.indoor.IndoorNavigationService;
import com.relief.service.indoor.IndoorNavigationService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for indoor navigation
 */
@RestController
@RequestMapping("/indoor")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Indoor Navigation", description = "Indoor navigation and positioning APIs")
public class IndoorNavigationController {
    
    private static final Logger log = LoggerFactory.getLogger(IndoorNavigationController.class);
    
    private final IndoorNavigationService indoorNavigationService;
    
    // Indoor Maps
    
    @PostMapping("/maps")
    @Operation(summary = "Create indoor map", description = "Create a new indoor map for GPS-denied environments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<IndoorMap> createIndoorMap(@Valid @RequestBody IndoorMapRequest request) {
        log.info("Creating indoor map: {}", request.name());
        IndoorMap indoorMap = indoorNavigationService.createIndoorMap(request);
        return ResponseEntity.ok(indoorMap);
    }
    
    @PutMapping("/maps/{mapId}")
    @Operation(summary = "Update indoor map", description = "Update an existing indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<IndoorMap> updateIndoorMap(
            @PathVariable Long mapId,
            @Valid @RequestBody IndoorMapRequest request) {
        log.info("Updating indoor map: {}", mapId);
        IndoorMap indoorMap = indoorNavigationService.updateIndoorMap(mapId, request);
        return ResponseEntity.ok(indoorMap);
    }
    
    @GetMapping("/maps/{mapId}")
    @Operation(summary = "Get indoor map", description = "Get indoor map by ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<IndoorMap> getIndoorMap(@PathVariable Long mapId) {
        log.info("Getting indoor map: {}", mapId);
        Optional<IndoorMap> indoorMap = indoorNavigationService.getIndoorMap(mapId);
        return indoorMap.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/maps")
    @Operation(summary = "Get all indoor maps", description = "Get all indoor maps with optional filtering")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<IndoorMap>> getAllIndoorMaps(
            @Parameter(description = "Filter by facility ID") @RequestParam(required = false) Long facilityId,
            @Parameter(description = "Filter by map type") @RequestParam(required = false) IndoorMapType mapType,
            @Parameter(description = "Filter by floor number") @RequestParam(required = false) Integer floorNumber,
            @Parameter(description = "Filter active maps only") @RequestParam(defaultValue = "true") boolean activeOnly) {
        log.info("Getting indoor maps - facilityId: {}, mapType: {}, floorNumber: {}, activeOnly: {}", 
                facilityId, mapType, floorNumber, activeOnly);
        
        List<IndoorMap> maps;
        if (facilityId != null) {
            maps = indoorNavigationService.getIndoorMapsByFacility(facilityId);
        } else if (mapType != null) {
            maps = indoorNavigationService.getIndoorMapsByType(mapType);
        } else if (activeOnly) {
            maps = indoorNavigationService.getActiveIndoorMaps();
        } else {
            maps = indoorNavigationService.getAllIndoorMaps();
        }
        
        return ResponseEntity.ok(maps);
    }
    
    // Indoor Nodes
    
    @PostMapping("/maps/{mapId}/nodes")
    @Operation(summary = "Create indoor node", description = "Create a new navigation node in an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<IndoorNode> createIndoorNode(
            @PathVariable Long mapId,
            @Valid @RequestBody IndoorNodeRequest request) {
        log.info("Creating indoor node: {} in map: {}", request.nodeId(), mapId);
        
        // Set the map ID from path variable
        IndoorNodeRequest updatedRequest = new IndoorNodeRequest(
            mapId,
            request.nodeId(),
            request.name(),
            request.description(),
            request.longitude(),
            request.latitude(),
            request.localX(),
            request.localY(),
            request.nodeType(),
            request.isAccessible(),
            request.accessibilityFeatures(),
            request.capacity(),
            request.currentOccupancy(),
            request.isEmergencyExit(),
            request.isElevator(),
            request.isStairs(),
            request.floorLevel(),
            request.metadata()
        );
        
        IndoorNode node = indoorNavigationService.createIndoorNode(updatedRequest);
        return ResponseEntity.ok(node);
    }
    
    @GetMapping("/maps/{mapId}/nodes")
    @Operation(summary = "Get indoor nodes", description = "Get all nodes in an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<IndoorNode>> getIndoorNodes(
            @PathVariable Long mapId,
            @Parameter(description = "Filter by node type") @RequestParam(required = false) IndoorNodeType nodeType,
            @Parameter(description = "Filter by floor level") @RequestParam(required = false) Integer floorLevel,
            @Parameter(description = "Filter accessible nodes only") @RequestParam(defaultValue = "false") boolean accessibleOnly) {
        log.info("Getting indoor nodes for map: {}", mapId);
        
        List<IndoorNode> nodes = indoorNavigationService.getIndoorNodes(mapId, nodeType, floorLevel, accessibleOnly);
        return ResponseEntity.ok(nodes);
    }
    
    // Indoor Edges
    
    @GetMapping("/maps/{mapId}/edges")
    @Operation(summary = "Get indoor edges", description = "Get edges in an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<IndoorEdge>> getIndoorEdges(
            @PathVariable Long mapId,
            @Parameter(description = "Filter by edge type") @RequestParam(required = false) IndoorEdgeType edgeType,
            @Parameter(description = "Filter accessible edges only") @RequestParam(required = false) Boolean accessibleOnly,
            @Parameter(description = "Filter bidirectional edges only") @RequestParam(required = false) Boolean bidirectionalOnly) {
        
        log.info("Getting indoor edges for map: {}", mapId);
        List<IndoorEdge> edges = indoorNavigationService.getIndoorEdges(
                mapId,
                edgeType,
                accessibleOnly != null && accessibleOnly,
                bidirectionalOnly != null && bidirectionalOnly
        );
        return ResponseEntity.ok(edges);
    }
    
    @PostMapping("/maps/{mapId}/edges")
    @Operation(summary = "Create indoor edge", description = "Create a new connection between nodes in an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<IndoorEdge> createIndoorEdge(
            @PathVariable Long mapId,
            @Valid @RequestBody IndoorEdgeRequest request) {
        log.info("Creating indoor edge: {} in map: {}", request.edgeId(), mapId);
        
        // Set the map ID from path variable
        IndoorEdgeRequest updatedRequest = new IndoorEdgeRequest(
            mapId,
            request.edgeId(),
            request.name(),
            request.description(),
            request.fromNodeId(),
            request.toNodeId(),
            request.pathCoordinates(),
            request.edgeType(),
            request.isAccessible(),
            request.isBidirectional(),
            request.distance(),
            request.width(),
            request.height(),
            request.weight(),
            request.maxSpeed(),
            request.accessibilityFeatures(),
            request.isEmergencyRoute(),
            request.isRestricted(),
            request.restrictionType(),
            request.metadata()
        );
        
        IndoorEdge edge = indoorNavigationService.createIndoorEdge(updatedRequest);
        return ResponseEntity.ok(edge);
    }
    
    // Indoor Positioning
    
    @PostMapping("/positions")
    @Operation(summary = "Record indoor position", description = "Record a position within an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<IndoorPosition> recordIndoorPosition(@Valid @RequestBody IndoorPositionRequest request) {
        log.info("Recording indoor position for entity: {} {}", request.entityType(), request.entityId());
        IndoorPosition position = indoorNavigationService.recordIndoorPosition(request);
        return ResponseEntity.ok(position);
    }
    
    @GetMapping("/positions")
    @Operation(summary = "Get indoor positions", description = "Get indoor positions with optional filtering")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<IndoorPosition>> getIndoorPositions(
            @Parameter(description = "Filter by entity type") @RequestParam(required = false) String entityType,
            @Parameter(description = "Filter by entity ID") @RequestParam(required = false) Long entityId,
            @Parameter(description = "Filter by map ID") @RequestParam(required = false) Long mapId,
            @Parameter(description = "Filter by floor level") @RequestParam(required = false) Integer floorLevel,
            @Parameter(description = "Filter by positioning method") @RequestParam(required = false) PositioningMethod positioningMethod) {
        log.info("Getting indoor positions - entityType: {}, entityId: {}, mapId: {}", entityType, entityId, mapId);
        
        List<IndoorPosition> positions;
        if (entityType != null && entityId != null) {
            positions = indoorNavigationService.getIndoorPositions(entityType, entityId);
        } else if (mapId != null) {
            // Get all positions for the specified map
            positions = indoorNavigationService.getIndoorPositionsByMap(mapId, floorLevel, positioningMethod);
        } else {
            positions = List.of();
        }
        
        return ResponseEntity.ok(positions);
    }
    
    @GetMapping("/positions/latest")
    @Operation(summary = "Get latest position", description = "Get the latest position for an entity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<IndoorPosition> getLatestPosition(
            @Parameter(description = "Entity type") @RequestParam String entityType,
            @Parameter(description = "Entity ID") @RequestParam Long entityId) {
        log.info("Getting latest position for entity: {} {}", entityType, entityId);
        Optional<IndoorPosition> position = indoorNavigationService.getLatestPosition(entityType, entityId);
        return position.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Indoor Routing
    
    @PostMapping("/maps/{mapId}/routes/calculate")
    @Operation(summary = "Calculate route", description = "Calculate a route between two nodes in an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<IndoorRoute> calculateRoute(
            @PathVariable Long mapId,
            @Parameter(description = "From node ID") @RequestParam Long fromNodeId,
            @Parameter(description = "To node ID") @RequestParam Long toNodeId,
            @Parameter(description = "Route type") @RequestParam(defaultValue = "SHORTEST_PATH") IndoorRouteType routeType,
            @Parameter(description = "Created by") @RequestParam String createdBy) {
        log.info("Calculating route from node {} to node {} in map {}", fromNodeId, toNodeId, mapId);
        
        IndoorRoute route = indoorNavigationService.calculateRoute(mapId, fromNodeId, toNodeId, routeType, createdBy);
        return ResponseEntity.ok(route);
    }
    
    @GetMapping("/maps/{mapId}/routes")
    @Operation(summary = "Get indoor routes", description = "Get routes in an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<List<IndoorRoute>> getIndoorRoutes(
            @PathVariable Long mapId,
            @Parameter(description = "Filter by route type") @RequestParam(required = false) IndoorRouteType routeType,
            @Parameter(description = "Filter accessible routes only") @RequestParam(defaultValue = "false") boolean accessibleOnly,
            @Parameter(description = "Filter emergency routes only") @RequestParam(defaultValue = "false") boolean emergencyOnly) {
        log.info("Getting indoor routes for map: {}", mapId);
        
        List<IndoorRoute> routes;
        if (routeType != null) {
            routes = indoorNavigationService.getIndoorRoutesByType(mapId, routeType);
        } else if (accessibleOnly) {
            routes = indoorNavigationService.getAccessibleRoutes(mapId);
        } else if (emergencyOnly) {
            routes = indoorNavigationService.getEmergencyRoutes(mapId);
        } else {
            routes = indoorNavigationService.getIndoorRoutes(mapId);
        }
        
        return ResponseEntity.ok(routes);
    }
    
    @GetMapping("/maps/{mapId}/nodes/nearest")
    @Operation(summary = "Find nearest node", description = "Find the nearest node to a position")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('HELPER') or hasRole('RESIDENT')")
    public ResponseEntity<IndoorNode> findNearestNode(
            @PathVariable Long mapId,
            @Parameter(description = "Longitude") @RequestParam double longitude,
            @Parameter(description = "Latitude") @RequestParam double latitude,
            @Parameter(description = "Search radius in meters") @RequestParam(defaultValue = "100.0") double radius) {
        log.info("Finding nearest node to position ({}, {}) in map {}", longitude, latitude, mapId);
        
        Optional<IndoorNode> node = indoorNavigationService.findNearestNode(mapId, longitude, latitude, radius);
        return node.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Statistics and Analytics
    
    @GetMapping("/maps/{mapId}/statistics")
    @Operation(summary = "Get map statistics", description = "Get statistics for an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getMapStatistics(@PathVariable Long mapId) {
        log.info("Getting statistics for map: {}", mapId);
        
        // This would need to be implemented in the service
        Map<String, Object> statistics = Map.of(
            "totalNodes", 0,
            "totalEdges", 0,
            "totalRoutes", 0,
            "totalPositions", 0
        );
        
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/maps/{mapId}/nodes/statistics")
    @Operation(summary = "Get node statistics", description = "Get node statistics for an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getNodeStatistics(@PathVariable Long mapId) {
        log.info("Getting node statistics for map: {}", mapId);
        
        // This would need to be implemented in the service
        Map<String, Object> statistics = Map.of(
            "totalNodes", 0,
            "rooms", 0,
            "corridors", 0,
            "stairs", 0,
            "elevators", 0,
            "accessibleNodes", 0,
            "emergencyExits", 0
        );
        
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/maps/{mapId}/edges/statistics")
    @Operation(summary = "Get edge statistics", description = "Get edge statistics for an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getEdgeStatistics(@PathVariable Long mapId) {
        log.info("Getting edge statistics for map: {}", mapId);
        
        // This would need to be implemented in the service
        Map<String, Object> statistics = Map.of(
            "totalEdges", 0,
            "corridors", 0,
            "doorways", 0,
            "stairs", 0,
            "elevators", 0,
            "accessibleEdges", 0,
            "emergencyRoutes", 0
        );
        
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/maps/{mapId}/positions/statistics")
    @Operation(summary = "Get position statistics", description = "Get position statistics for an indoor map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Map<String, Object>> getPositionStatistics(
            @PathVariable Long mapId,
            @Parameter(description = "Start date") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam(required = false) LocalDateTime endDate) {
        log.info("Getting position statistics for map: {}", mapId);
        
        // This would need to be implemented in the service
        Map<String, Object> statistics = Map.of(
            "totalPositions", 0,
            "validPositions", 0,
            "wifiPositions", 0,
            "bluetoothPositions", 0,
            "uwbPositions", 0,
            "avgAccuracy", 0.0,
            "avgSpeed", 0.0
        );
        
        return ResponseEntity.ok(statistics);
    }
}



