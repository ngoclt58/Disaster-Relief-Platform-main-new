package com.relief.service.indoor;

import com.relief.domain.indoor.*;
import com.relief.repository.indoor.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for indoor navigation and positioning
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IndoorNavigationService {

    private static final Logger log = LoggerFactory.getLogger(IndoorNavigationService.class);
    
    private final IndoorMapRepository mapRepository;
    private final IndoorNodeRepository nodeRepository;
    private final IndoorEdgeRepository edgeRepository;
    private final IndoorPositionRepository positionRepository;
    private final IndoorRouteRepository routeRepository;
    private final GeometryFactory geometryFactory;
    
    /**
     * Create an indoor map
     */
    @Transactional
    public IndoorMap createIndoorMap(IndoorMapRequest request) {
        log.info("Creating indoor map: {}", request.name());
        
        // Create map bounds geometry
        Geometry mapBounds = createMapBoundsGeometry(request.boundsCoordinates());
        
        IndoorMap indoorMap = IndoorMap.builder()
            .name(request.name())
            .description(request.description())
            .facilityId(request.facilityId())
            .facilityName(request.facilityName())
            .floorNumber(request.floorNumber())
            .floorName(request.floorName())
            .mapBounds(mapBounds)
            .mapType(request.mapType())
            .coordinateSystem(request.coordinateSystem())
            .scaleFactor(request.scaleFactor())
            .mapImageUrl(request.mapImageUrl())
            .mapData(request.mapData())
            .isActive(request.isActive())
            .createdBy(request.createdBy())
            .build();
            
        return mapRepository.save(indoorMap);
    }
    
    /**
     * Get indoor nodes for a map with optional filters
     */
    @Transactional(readOnly = true)
    public List<IndoorNode> getIndoorNodes(Long mapId, IndoorNodeType nodeType,
                                           Integer floorLevel, boolean accessibleOnly) {
        log.info("Fetching indoor nodes for map {} with type={}, floorLevel={}, accessibleOnly={}",
                mapId, nodeType, floorLevel, accessibleOnly);

        if (nodeType != null && floorLevel != null) {
            return nodeRepository.findByIndoorMapIdAndFloorLevel(mapId, floorLevel).stream()
                    .filter(n -> n.getNodeType() == nodeType)
                    .toList();
        }

        if (nodeType != null) {
            return nodeRepository.findByIndoorMapIdAndNodeType(mapId, nodeType);
        }

        if (floorLevel != null) {
            return nodeRepository.findByIndoorMapIdAndFloorLevel(mapId, floorLevel);
        }

        if (accessibleOnly) {
            return nodeRepository.findByIndoorMapIdAndIsAccessibleTrue(mapId);
        }

        return nodeRepository.findByIndoorMapId(mapId);
    }

    /**
     * Get indoor edges for a map with optional filters
     */
    @Transactional(readOnly = true)
    public List<IndoorEdge> getIndoorEdges(Long mapId, IndoorEdgeType edgeType,
                                           Boolean accessibleOnly, Boolean bidirectionalOnly) {
        log.info("Fetching indoor edges for map {} with type={}, accessibleOnly={}, bidirectionalOnly={}",
                mapId, edgeType, accessibleOnly, bidirectionalOnly);

        List<IndoorEdge> edges = edgeRepository.findByIndoorMapId(mapId);

        if (edgeType != null) {
            edges = edges.stream()
                    .filter(e -> e.getEdgeType() == edgeType)
                    .toList();
        }

        if (Boolean.TRUE.equals(accessibleOnly)) {
            edges = edges.stream()
                    .filter(IndoorEdge::getIsAccessible)
                    .toList();
        }

        if (Boolean.TRUE.equals(bidirectionalOnly)) {
            edges = edges.stream()
                    .filter(IndoorEdge::getIsBidirectional)
                    .toList();
        }

        return edges;
    }

    /**
     * Get indoor routes for a map with optional filters
     */
    @Transactional(readOnly = true)
    public List<IndoorRoute> getIndoorRoutes(Long mapId, IndoorRouteType routeType,
                                             boolean accessibleOnly, boolean emergencyOnly) {
        log.info("Fetching indoor routes for map {} with type={}, accessibleOnly={}, emergencyOnly={}",
                mapId, routeType, accessibleOnly, emergencyOnly);

        if (routeType != null) {
            return routeRepository.findByIndoorMapIdAndRouteType(mapId, routeType);
        }

        if (accessibleOnly) {
            return routeRepository.findByIndoorMapIdAndIsAccessibleTrue(mapId);
        }

        if (emergencyOnly) {
            return routeRepository.findByIndoorMapIdAndIsEmergencyRouteTrue(mapId);
        }

        return routeRepository.findByIndoorMapId(mapId);
    }

    /**
     * Update an indoor map
     */
    @Transactional
    public IndoorMap updateIndoorMap(Long mapId, IndoorMapRequest request) {
        log.info("Updating indoor map: {}", mapId);
        
        IndoorMap indoorMap = mapRepository.findById(mapId)
            .orElseThrow(() -> new IllegalArgumentException("Indoor map not found"));
        
        // Update fields
        indoorMap.setName(request.name());
        indoorMap.setDescription(request.description());
        indoorMap.setFacilityId(request.facilityId());
        indoorMap.setFacilityName(request.facilityName());
        indoorMap.setFloorNumber(request.floorNumber());
        indoorMap.setFloorName(request.floorName());
        indoorMap.setMapType(request.mapType());
        indoorMap.setCoordinateSystem(request.coordinateSystem());
        indoorMap.setScaleFactor(request.scaleFactor());
        indoorMap.setMapImageUrl(request.mapImageUrl());
        indoorMap.setMapData(request.mapData());
        indoorMap.setIsActive(request.isActive());
        
        // Update bounds if provided
        if (request.boundsCoordinates() != null) {
            Geometry mapBounds = createMapBoundsGeometry(request.boundsCoordinates());
            indoorMap.setMapBounds(mapBounds);
        }
        
        return mapRepository.save(indoorMap);
    }
    
    /**
     * Get indoor map by ID
     */
    public Optional<IndoorMap> getIndoorMap(Long mapId) {
        return mapRepository.findById(mapId);
    }
    
    /**
     * Get all indoor maps
     */
    public List<IndoorMap> getAllIndoorMaps() {
        return mapRepository.findAll();
    }
    
    /**
     * Get active indoor maps
     */
    public List<IndoorMap> getActiveIndoorMaps() {
        return mapRepository.findByIsActiveTrue();
    }
    
    /**
     * Get indoor maps by facility
     */
    public List<IndoorMap> getIndoorMapsByFacility(Long facilityId) {
        return mapRepository.findByFacilityId(facilityId);
    }
    
    /**
     * Get indoor maps by type
     */
    public List<IndoorMap> getIndoorMapsByType(IndoorMapType mapType) {
        return mapRepository.findByMapType(mapType);
    }
    
    /**
     * Create an indoor node
     */
    @Transactional
    public IndoorNode createIndoorNode(IndoorNodeRequest request) {
        log.info("Creating indoor node: {}", request.nodeId());
        
        IndoorMap indoorMap = mapRepository.findById(request.indoorMapId())
            .orElseThrow(() -> new IllegalArgumentException("Indoor map not found"));
        
        Point position = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        
        IndoorNode node = IndoorNode.builder()
            .indoorMap(indoorMap)
            .nodeId(request.nodeId())
            .name(request.name())
            .description(request.description())
            .position(position)
            .localX(request.localX())
            .localY(request.localY())
            .nodeType(request.nodeType())
            .isAccessible(request.isAccessible())
            .accessibilityFeatures(request.accessibilityFeatures())
            .capacity(request.capacity())
            .currentOccupancy(request.currentOccupancy())
            .isEmergencyExit(request.isEmergencyExit())
            .isElevator(request.isElevator())
            .isStairs(request.isStairs())
            .floorLevel(request.floorLevel())
            .metadata(request.metadata())
            .build();
            
        return nodeRepository.save(node);
    }
    
    /**
     * Create an indoor edge
     */
    @Transactional
    public IndoorEdge createIndoorEdge(IndoorEdgeRequest request) {
        log.info("Creating indoor edge: {}", request.edgeId());
        
        IndoorMap indoorMap = mapRepository.findById(request.indoorMapId())
            .orElseThrow(() -> new IllegalArgumentException("Indoor map not found"));
        
        IndoorNode fromNode = nodeRepository.findById(request.fromNodeId())
            .orElseThrow(() -> new IllegalArgumentException("From node not found"));
        
        IndoorNode toNode = nodeRepository.findById(request.toNodeId())
            .orElseThrow(() -> new IllegalArgumentException("To node not found"));
        
        // Create path geometry if provided
        LineString path = null;
        if (request.pathCoordinates() != null && !request.pathCoordinates().isEmpty()) {
            Coordinate[] coordinates = request.pathCoordinates().toArray(new Coordinate[0]);
            path = geometryFactory.createLineString(coordinates);
        }
        
        IndoorEdge edge = IndoorEdge.builder()
            .indoorMap(indoorMap)
            .fromNode(fromNode)
            .toNode(toNode)
            .edgeId(request.edgeId())
            .name(request.name())
            .description(request.description())
            .path(path)
            .edgeType(request.edgeType())
            .isAccessible(request.isAccessible())
            .isBidirectional(request.isBidirectional())
            .distance(request.distance())
            .width(request.width())
            .height(request.height())
            .weight(request.weight())
            .maxSpeed(request.maxSpeed())
            .accessibilityFeatures(request.accessibilityFeatures())
            .isEmergencyRoute(request.isEmergencyRoute())
            .isRestricted(request.isRestricted())
            .restrictionType(request.restrictionType())
            .metadata(request.metadata())
            .build();
            
        return edgeRepository.save(edge);
    }
    
    /**
     * Record indoor position
     */
    @Transactional
    public IndoorPosition recordIndoorPosition(IndoorPositionRequest request) {
        log.info("Recording indoor position for entity: {} {}", request.entityType(), request.entityId());
        
        IndoorMap indoorMap = mapRepository.findById(request.indoorMapId())
            .orElseThrow(() -> new IllegalArgumentException("Indoor map not found"));
        
        Point position = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        
        IndoorPosition indoorPosition = IndoorPosition.builder()
            .indoorMap(indoorMap)
            .entityType(request.entityType())
            .entityId(request.entityId())
            .entityName(request.entityName())
            .position(position)
            .localX(request.localX())
            .localY(request.localY())
            .floorLevel(request.floorLevel())
            .heading(request.heading())
            .speed(request.speed())
            .accuracy(request.accuracy())
            .positioningMethod(request.positioningMethod())
            .timestamp(request.timestamp())
            .isValid(request.isValid())
            .metadata(request.metadata())
            .build();
            
        return positionRepository.save(indoorPosition);
    }
    
    /**
     * Find nearest node to a position
     */
    public Optional<IndoorNode> findNearestNode(Long mapId, double longitude, double latitude, double radius) {
        List<IndoorNode> nodes = nodeRepository.findNearPoint(mapId, longitude, latitude, radius);
        return nodes.isEmpty() ? Optional.empty() : Optional.of(nodes.get(0));
    }
    
    /**
     * Calculate route between two nodes
     */
    public IndoorRoute calculateRoute(Long mapId, Long fromNodeId, Long toNodeId, 
                                    IndoorRouteType routeType, String createdBy) {
        log.info("Calculating route from node {} to node {} in map {}", fromNodeId, toNodeId, mapId);
        
        IndoorNode fromNode = nodeRepository.findById(fromNodeId)
            .orElseThrow(() -> new IllegalArgumentException("From node not found"));
        
        IndoorNode toNode = nodeRepository.findById(toNodeId)
            .orElseThrow(() -> new IllegalArgumentException("To node not found"));
        
        // Use Dijkstra's algorithm for pathfinding
        List<IndoorNode> path = findShortestPath(fromNode, toNode, routeType);
        
        if (path.isEmpty()) {
            throw new IllegalArgumentException("No route found between nodes");
        }
        
        // Calculate route metrics
        double totalDistance = calculatePathDistance(path);
        int estimatedTime = calculateEstimatedTime(path, totalDistance);
        DifficultyLevel difficultyLevel = calculateDifficultyLevel(path);
        
        // Create route
        String routeId = "route_" + System.currentTimeMillis();
        IndoorRoute route = IndoorRoute.builder()
            .indoorMap(fromNode.getIndoorMap())
            .fromNode(fromNode)
            .toNode(toNode)
            .routeId(routeId)
            .name("Route from " + fromNode.getName() + " to " + toNode.getName())
            .description("Generated route between nodes")
            .path(createRoutePath(path))
            .routeType(routeType)
            .totalDistance(totalDistance)
            .estimatedTime(estimatedTime)
            .difficultyLevel(difficultyLevel)
            .isAccessible(isPathAccessible(path))
            .isEmergencyRoute(routeType == IndoorRouteType.EMERGENCY_EVACUATION)
            .isRestricted(false)
            .waypoints(createWaypointsJson(path))
            .instructions(createInstructionsJson(path))
            .createdBy(createdBy)
            .build();
            
        return routeRepository.save(route);
    }
    
    /**
     * Get indoor positions by map with optional filters
     */
    public List<IndoorPosition> getIndoorPositionsByMap(Long mapId, Integer floorLevel, PositioningMethod positioningMethod) {
        if (mapId == null) {
            return List.of();
        }
        
        if (floorLevel != null && positioningMethod != null) {
            return positionRepository.findByIndoorMapIdAndFloorLevelAndPositioningMethod(mapId, floorLevel, positioningMethod);
        } else if (floorLevel != null) {
            return positionRepository.findByIndoorMapIdAndFloorLevel(mapId, floorLevel);
        } else if (positioningMethod != null) {
            return positionRepository.findByIndoorMapIdAndPositioningMethod(mapId, positioningMethod);
        } else {
            return positionRepository.findByIndoorMapId(mapId);
        }
    }

    /**
     * Get indoor positions for an entity
     */
    public List<IndoorPosition> getIndoorPositions(String entityType, Long entityId) {
        return positionRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    /**
     * Get latest position for an entity
     */
    public Optional<IndoorPosition> getLatestPosition(String entityType, Long entityId) {
        List<IndoorPosition> positions = positionRepository.findLatestPositionByEntity(entityType, entityId);
        return positions.isEmpty() ? Optional.empty() : Optional.of(positions.get(0));
    }
    
    /**
     * Get indoor routes for a map
     */
    public List<IndoorRoute> getIndoorRoutes(Long mapId) {
        return routeRepository.findByIndoorMapId(mapId);
    }
    
    /**
     * Get indoor routes by type
     */
    public List<IndoorRoute> getIndoorRoutesByType(Long mapId, IndoorRouteType routeType) {
        return routeRepository.findByIndoorMapIdAndRouteType(mapId, routeType);
    }
    
    /**
     * Get accessible routes
     */
    public List<IndoorRoute> getAccessibleRoutes(Long mapId) {
        return routeRepository.findByIndoorMapIdAndIsAccessibleTrue(mapId);
    }
    
    /**
     * Get emergency routes
     */
    public List<IndoorRoute> getEmergencyRoutes(Long mapId) {
        return routeRepository.findByIndoorMapIdAndIsEmergencyRouteTrue(mapId);
    }
    
    /**
     * Find shortest path using Dijkstra's algorithm
     */
    private List<IndoorNode> findShortestPath(IndoorNode fromNode, IndoorNode toNode, IndoorRouteType routeType) {
        Map<Long, Double> distances = new HashMap<>();
        Map<Long, IndoorNode> previous = new HashMap<>();
        PriorityQueue<IndoorNode> queue = new PriorityQueue<>(
            Comparator.comparingDouble(node -> distances.getOrDefault(node.getId(), Double.MAX_VALUE))
        );
        
        distances.put(fromNode.getId(), 0.0);
        queue.offer(fromNode);
        
        while (!queue.isEmpty()) {
            IndoorNode current = queue.poll();
            
            if (current.getId().equals(toNode.getId())) {
                break;
            }
            
            List<IndoorEdge> edges = edgeRepository.findByFromNodeId(current.getId());
            for (IndoorEdge edge : edges) {
                if (!isEdgeAccessible(edge, routeType)) {
                    continue;
                }
                
                IndoorNode neighbor = edge.getToNode();
                double newDistance = distances.get(current.getId()) + edge.getDistance() * edge.getWeight();
                
                if (newDistance < distances.getOrDefault(neighbor.getId(), Double.MAX_VALUE)) {
                    distances.put(neighbor.getId(), newDistance);
                    previous.put(neighbor.getId(), current);
                    queue.offer(neighbor);
                }
            }
        }
        
        // Reconstruct path
        List<IndoorNode> path = new ArrayList<>();
        IndoorNode current = toNode;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current.getId());
        }
        
        return path;
    }
    
    /**
     * Check if edge is accessible for route type
     */
    private boolean isEdgeAccessible(IndoorEdge edge, IndoorRouteType routeType) {
        if (!edge.getIsAccessible()) {
            return false;
        }
        
        if (edge.getIsRestricted()) {
            return false;
        }
        
        if (routeType == IndoorRouteType.ACCESSIBLE_PATH && !edge.getIsAccessible()) {
            return false;
        }
        
        if (routeType == IndoorRouteType.EMERGENCY_EVACUATION && !edge.getIsEmergencyRoute()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate total distance of path
     */
    private double calculatePathDistance(List<IndoorNode> path) {
        double totalDistance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            IndoorNode from = path.get(i);
            IndoorNode to = path.get(i + 1);
            
            List<IndoorEdge> edges = edgeRepository.findByFromNodeIdAndToNodeId(from.getId(), to.getId());
            if (!edges.isEmpty()) {
                totalDistance += edges.get(0).getDistance();
            } else {
                // Calculate Euclidean distance if no edge found
                double dx = to.getLocalX() - from.getLocalX();
                double dy = to.getLocalY() - from.getLocalY();
                totalDistance += Math.sqrt(dx * dx + dy * dy);
            }
        }
        return totalDistance;
    }
    
    /**
     * Calculate estimated time for path
     */
    private int calculateEstimatedTime(List<IndoorNode> path, double totalDistance) {
        // Assume average walking speed of 1.4 m/s
        double averageSpeed = 1.4;
        return (int) Math.round(totalDistance / averageSpeed);
    }
    
    /**
     * Calculate difficulty level for path
     */
    private DifficultyLevel calculateDifficultyLevel(List<IndoorNode> path) {
        boolean hasStairs = path.stream().anyMatch(IndoorNode::getIsStairs);
        boolean hasElevator = path.stream().anyMatch(IndoorNode::getIsElevator);
        
        if (hasStairs && hasElevator) {
            return DifficultyLevel.MODERATE;
        } else if (hasStairs) {
            return DifficultyLevel.DIFFICULT;
        } else {
            return DifficultyLevel.EASY;
        }
    }
    
    /**
     * Check if path is accessible
     */
    private boolean isPathAccessible(List<IndoorNode> path) {
        return path.stream().allMatch(IndoorNode::getIsAccessible);
    }
    
    /**
     * Create route path geometry
     */
    private LineString createRoutePath(List<IndoorNode> path) {
        Coordinate[] coordinates = path.stream()
            .map(node -> new Coordinate(node.getPosition().getX(), node.getPosition().getY()))
            .toArray(Coordinate[]::new);
        return geometryFactory.createLineString(coordinates);
    }
    
    /**
     * Create waypoints JSON
     */
    private String createWaypointsJson(List<IndoorNode> path) {
        List<Map<String, Object>> waypoints = path.stream()
            .map(node -> {
                Map<String, Object> waypoint = new java.util.HashMap<>();
                waypoint.put("nodeId", node.getNodeId());
                waypoint.put("name", node.getName() != null ? node.getName() : "");
                waypoint.put("localX", node.getLocalX());
                waypoint.put("localY", node.getLocalY());
                waypoint.put("floorLevel", node.getFloorLevel() != null ? node.getFloorLevel() : 0);
                return waypoint;
            })
            .collect(Collectors.toList());
        
        return "{\"waypoints\":" + waypoints.toString() + "}";
    }
    
    /**
     * Create instructions JSON
     */
    private String createInstructionsJson(List<IndoorNode> path) {
        List<Map<String, Object>> instructions = new ArrayList<>();
        
        for (int i = 0; i < path.size(); i++) {
            IndoorNode node = path.get(i);
            String instruction = "";
            String actionType = "CONTINUE_STRAIGHT";
            
            if (i == 0) {
                instruction = "Start at " + (node.getName() != null ? node.getName() : node.getNodeId());
                actionType = "START";
            } else if (i == path.size() - 1) {
                instruction = "Arrive at " + (node.getName() != null ? node.getName() : node.getNodeId());
                actionType = "ARRIVE";
            } else {
                // Determine action based on node type
                if (node.getIsStairs()) {
                    instruction = "Go up stairs";
                    actionType = "GO_UP_STAIRS";
                } else if (node.getIsElevator()) {
                    instruction = "Take elevator";
                    actionType = "TAKE_ELEVATOR";
                } else {
                    instruction = "Continue to " + (node.getName() != null ? node.getName() : node.getNodeId());
                    actionType = "CONTINUE_STRAIGHT";
                }
            }
            
            instructions.add(Map.of(
                "step", i + 1,
                "instruction", instruction,
                "actionType", actionType,
                "nodeId", node.getNodeId(),
                "localX", node.getLocalX(),
                "localY", node.getLocalY(),
                "floorLevel", node.getFloorLevel() != null ? node.getFloorLevel() : 0
            ));
        }
        
        return "{\"instructions\":" + instructions.toString() + "}";
    }
    
    /**
     * Create map bounds geometry
     */
    private Geometry createMapBoundsGeometry(List<Coordinate> coordinates) {
        if (coordinates.size() < 3) {
            throw new IllegalArgumentException("At least 3 coordinates required for map bounds");
        }
        
        // Close the polygon if not already closed
        if (!coordinates.get(0).equals(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(coordinates.get(0));
        }
        
        Coordinate[] coordArray = coordinates.toArray(new Coordinate[0]);
        LinearRing ring = geometryFactory.createLinearRing(coordArray);
        return geometryFactory.createPolygon(ring);
    }
    
    // Data classes
    public record IndoorMapRequest(
        String name,
        String description,
        Long facilityId,
        String facilityName,
        Integer floorNumber,
        String floorName,
        List<Coordinate> boundsCoordinates,
        IndoorMapType mapType,
        String coordinateSystem,
        Double scaleFactor,
        String mapImageUrl,
        String mapData,
        Boolean isActive,
        String createdBy
    ) {}
    
    public record IndoorNodeRequest(
        Long indoorMapId,
        String nodeId,
        String name,
        String description,
        double longitude,
        double latitude,
        Double localX,
        Double localY,
        IndoorNodeType nodeType,
        Boolean isAccessible,
        String accessibilityFeatures,
        Integer capacity,
        Integer currentOccupancy,
        Boolean isEmergencyExit,
        Boolean isElevator,
        Boolean isStairs,
        Integer floorLevel,
        String metadata
    ) {}
    
    public record IndoorEdgeRequest(
        Long indoorMapId,
        String edgeId,
        String name,
        String description,
        Long fromNodeId,
        Long toNodeId,
        List<Coordinate> pathCoordinates,
        IndoorEdgeType edgeType,
        Boolean isAccessible,
        Boolean isBidirectional,
        Double distance,
        Double width,
        Double height,
        Double weight,
        Double maxSpeed,
        String accessibilityFeatures,
        Boolean isEmergencyRoute,
        Boolean isRestricted,
        String restrictionType,
        String metadata
    ) {}
    
    public record IndoorPositionRequest(
        Long indoorMapId,
        String entityType,
        Long entityId,
        String entityName,
        double longitude,
        double latitude,
        Double localX,
        Double localY,
        Integer floorLevel,
        Double heading,
        Double speed,
        Double accuracy,
        PositioningMethod positioningMethod,
        LocalDateTime timestamp,
        Boolean isValid,
        String metadata
    ) {}
}



