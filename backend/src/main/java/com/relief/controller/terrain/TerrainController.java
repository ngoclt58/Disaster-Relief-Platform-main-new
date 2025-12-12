package com.relief.controller.terrain;

import com.relief.service.terrain.ElevationService;
import com.relief.service.terrain.TerrainAnalysisService;
import com.relief.service.terrain.TerrainRoutingService;
import com.relief.domain.terrain.TerrainAnalysisType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for terrain analysis and elevation services
 */
@RestController
@RequestMapping("/terrain")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Terrain Analysis", description = "Terrain analysis and elevation services")
public class TerrainController {
    
    private final ElevationService elevationService;
    private final TerrainAnalysisService terrainAnalysisService;
    private final TerrainRoutingService terrainRoutingService;
    private final GeometryFactory geometryFactory;
    
    /**
     * Get elevation at a specific point
     */
    @GetMapping("/elevation")
    @Operation(summary = "Get elevation at point", description = "Get elevation data at a specific coordinate")
    public ResponseEntity<ElevationResponse> getElevation(
            @Parameter(description = "Longitude") @RequestParam double longitude,
            @Parameter(description = "Latitude") @RequestParam double latitude) {
        
        Optional<Double> elevation = elevationService.getElevationAtPoint(longitude, latitude);
        
        if (elevation.isPresent()) {
            return ResponseEntity.ok(new ElevationResponse(longitude, latitude, elevation.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get elevation points within a bounding box
     */
    @GetMapping("/elevation/bounds")
    @Operation(summary = "Get elevation points in bounds", description = "Get elevation points within a bounding box")
    public ResponseEntity<List<ElevationPointResponse>> getElevationPointsInBounds(
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var elevationPoints = elevationService.getElevationPointsInBounds(minLon, minLat, maxLon, maxLat);
        
        List<ElevationPointResponse> responses = elevationPoints.stream()
            .map(point -> new ElevationPointResponse(
                point.getLocation().getX(),
                point.getLocation().getY(),
                point.getElevation(),
                point.getSource().name(),
                point.getAccuracy(),
                point.getResolution()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get elevation statistics for an area
     */
    @GetMapping("/elevation/statistics")
    @Operation(summary = "Get elevation statistics", description = "Get elevation statistics for a bounding box")
    public ResponseEntity<ElevationStatisticsResponse> getElevationStatistics(
            @Parameter(description = "Minimum longitude") @RequestParam double minLon,
            @Parameter(description = "Minimum latitude") @RequestParam double minLat,
            @Parameter(description = "Maximum longitude") @RequestParam double maxLon,
            @Parameter(description = "Maximum latitude") @RequestParam double maxLat) {
        
        var statistics = elevationService.getElevationStatistics(minLon, minLat, maxLon, maxLat);
        
        ElevationStatisticsResponse response = new ElevationStatisticsResponse(
            statistics.minElevation(),
            statistics.maxElevation(),
            statistics.avgElevation(),
            statistics.elevationStddev(),
            statistics.pointCount()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Perform terrain analysis for an area
     */
    @PostMapping("/analysis")
    @Operation(summary = "Perform terrain analysis", description = "Perform terrain analysis for a specified area")
    public ResponseEntity<TerrainAnalysisResponse> performTerrainAnalysis(
            @Valid @RequestBody TerrainAnalysisRequest request) {
        
        // Create polygon from coordinates
        Coordinate[] coordinates = request.coordinates().stream()
            .map(coord -> new Coordinate(coord.longitude(), coord.latitude()))
            .toArray(Coordinate[]::new);
        
        // Close the polygon if not already closed
        if (coordinates.length > 0 && !coordinates[0].equals(coordinates[coordinates.length - 1])) {
            Coordinate[] closedCoordinates = new Coordinate[coordinates.length + 1];
            System.arraycopy(coordinates, 0, closedCoordinates, 0, coordinates.length);
            closedCoordinates[coordinates.length] = coordinates[0];
            coordinates = closedCoordinates;
        }
        
        Polygon area = geometryFactory.createPolygon(coordinates);
        TerrainAnalysisType analysisType = TerrainAnalysisType.valueOf(request.analysisType());
        
        var analysis = terrainAnalysisService.performTerrainAnalysis(area, analysisType);
        
        TerrainAnalysisResponse response = new TerrainAnalysisResponse(
            analysis.getId(),
            analysis.getAnalysisType().name(),
            analysis.getMinElevation(),
            analysis.getMaxElevation(),
            analysis.getAvgElevation(),
            analysis.getElevationVariance(),
            analysis.getSlopeAverage(),
            analysis.getSlopeMaximum(),
            analysis.getAspectAverage(),
            analysis.getRoughnessIndex(),
            analysis.getAccessibilityScore(),
            analysis.getFloodRiskScore(),
            analysis.getAnalysisData()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get terrain analysis for a point
     */
    @GetMapping("/analysis/point")
    @Operation(summary = "Get terrain analysis for point", description = "Get terrain analysis for a specific point")
    public ResponseEntity<TerrainAnalysisResponse> getTerrainAnalysisForPoint(
            @Parameter(description = "Longitude") @RequestParam double longitude,
            @Parameter(description = "Latitude") @RequestParam double latitude) {
        
        Optional<com.relief.domain.terrain.TerrainAnalysis> analysis = 
            terrainAnalysisService.getTerrainAnalysisForPoint(longitude, latitude);
        
        if (analysis.isPresent()) {
            var analysisData = analysis.get();
            TerrainAnalysisResponse response = new TerrainAnalysisResponse(
                analysisData.getId(),
                analysisData.getAnalysisType().name(),
                analysisData.getMinElevation(),
                analysisData.getMaxElevation(),
                analysisData.getAvgElevation(),
                analysisData.getElevationVariance(),
                analysisData.getSlopeAverage(),
                analysisData.getSlopeMaximum(),
                analysisData.getAspectAverage(),
                analysisData.getRoughnessIndex(),
                analysisData.getAccessibilityScore(),
                analysisData.getFloodRiskScore(),
                analysisData.getAnalysisData()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Find accessible areas
     */
    @GetMapping("/analysis/accessible")
    @Operation(summary = "Find accessible areas", description = "Find areas with good accessibility scores")
    public ResponseEntity<List<TerrainAnalysisResponse>> findAccessibleAreas(
            @Parameter(description = "Minimum accessibility score") @RequestParam(defaultValue = "0.7") double minAccessibilityScore,
            @Parameter(description = "Maximum slope in degrees") @RequestParam(defaultValue = "15") double maxSlope) {
        
        var analyses = terrainAnalysisService.findAccessibleAreas(minAccessibilityScore, maxSlope);
        
        List<TerrainAnalysisResponse> responses = analyses.stream()
            .map(analysis -> new TerrainAnalysisResponse(
                analysis.getId(),
                analysis.getAnalysisType().name(),
                analysis.getMinElevation(),
                analysis.getMaxElevation(),
                analysis.getAvgElevation(),
                analysis.getElevationVariance(),
                analysis.getSlopeAverage(),
                analysis.getSlopeMaximum(),
                analysis.getAspectAverage(),
                analysis.getRoughnessIndex(),
                analysis.getAccessibilityScore(),
                analysis.getFloodRiskScore(),
                analysis.getAnalysisData()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Calculate terrain-aware route
     */
    @PostMapping("/routing")
    @Operation(summary = "Calculate terrain route", description = "Calculate terrain-aware route between two points")
    public ResponseEntity<TerrainRouteResponse> calculateTerrainRoute(
            @Valid @RequestBody TerrainRouteRequest request) {
        
        var options = new TerrainRoutingService.TerrainRoutingOptions(
            request.searchRadius(),
            request.maxSlope(),
            request.minAccessibilityScore(),
            request.waypointOffsetDistance(),
            request.maxAlternativeRoutes()
        );
        
        var route = terrainRoutingService.calculateTerrainRoute(
            request.startLongitude(), request.startLatitude(),
            request.endLongitude(), request.endLatitude(),
            options
        );
        
        TerrainRouteResponse response = new TerrainRouteResponse(
            new CoordinateResponse(route.startPoint().x, route.startPoint().y),
            new CoordinateResponse(route.endPoint().x, route.endPoint().y),
            route.segments().stream()
                .map(segment -> new RouteSegmentResponse(
                    new CoordinateResponse(segment.startPoint().x, segment.startPoint().y),
                    new CoordinateResponse(segment.endPoint().x, segment.endPoint().y),
                    segment.distance(),
                    segment.slope(),
                    segment.elevationGain(),
                    segment.elevationLoss()
                ))
                .toList(),
            route.totalDistance(),
            route.totalElevationGain(),
            route.totalElevationLoss(),
            route.maxSlope(),
            route.avgSlope(),
            route.difficultyScore(),
            route.accessibilityScore(),
            route.isAccessible()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Find alternative routes
     */
    @PostMapping("/routing/alternatives")
    @Operation(summary = "Find alternative routes", description = "Find alternative terrain-aware routes")
    public ResponseEntity<List<TerrainRouteResponse>> findAlternativeRoutes(
            @Valid @RequestBody TerrainRouteRequest request) {
        
        var options = new TerrainRoutingService.TerrainRoutingOptions(
            request.searchRadius(),
            request.maxSlope(),
            request.minAccessibilityScore(),
            request.waypointOffsetDistance(),
            request.maxAlternativeRoutes()
        );
        
        var routes = terrainRoutingService.findAlternativeRoutes(
            request.startLongitude(), request.startLatitude(),
            request.endLongitude(), request.endLatitude(),
            options
        );
        
        List<TerrainRouteResponse> responses = routes.stream()
            .map(route -> new TerrainRouteResponse(
                new CoordinateResponse(route.startPoint().x, route.startPoint().y),
                new CoordinateResponse(route.endPoint().x, route.endPoint().y),
                route.segments().stream()
                    .map(segment -> new RouteSegmentResponse(
                        new CoordinateResponse(segment.startPoint().x, segment.startPoint().y),
                        new CoordinateResponse(segment.endPoint().x, segment.endPoint().y),
                        segment.distance(),
                        segment.slope(),
                        segment.elevationGain(),
                        segment.elevationLoss()
                    ))
                    .toList(),
                route.totalDistance(),
                route.totalElevationGain(),
                route.totalElevationLoss(),
                route.maxSlope(),
                route.avgSlope(),
                route.difficultyScore(),
                route.accessibilityScore(),
                route.isAccessible()
            ))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    // Request/Response DTOs
    
    public record ElevationResponse(double longitude, double latitude, double elevation) {}
    
    public record ElevationPointResponse(
        double longitude, double latitude, double elevation, 
        String source, Double accuracy, Double resolution) {}
    
    public record ElevationStatisticsResponse(
        Double minElevation, Double maxElevation, Double avgElevation, 
        Double elevationStddev, Long pointCount) {}
    
    public record TerrainAnalysisRequest(
        List<CoordinateRequest> coordinates,
        String analysisType
    ) {}
    
    public record CoordinateRequest(double longitude, double latitude) {}
    
    public record TerrainAnalysisResponse(
        Long id, String analysisType, Double minElevation, Double maxElevation,
        Double avgElevation, Double elevationVariance, Double slopeAverage,
        Double slopeMaximum, Double aspectAverage, Double roughnessIndex,
        Double accessibilityScore, Double floodRiskScore, String analysisData
    ) {}
    
    public record TerrainRouteRequest(
        double startLongitude, double startLatitude,
        double endLongitude, double endLatitude,
        double searchRadius, double maxSlope, double minAccessibilityScore,
        double waypointOffsetDistance, int maxAlternativeRoutes
    ) {}
    
    public record TerrainRouteResponse(
        CoordinateResponse startPoint, CoordinateResponse endPoint,
        List<RouteSegmentResponse> segments, double totalDistance,
        double totalElevationGain, double totalElevationLoss,
        double maxSlope, double avgSlope, double difficultyScore,
        double accessibilityScore, boolean isAccessible
    ) {}
    
    public record CoordinateResponse(double longitude, double latitude) {}
    
    public record RouteSegmentResponse(
        CoordinateResponse startPoint, CoordinateResponse endPoint,
        double distance, double slope, double elevationGain, double elevationLoss
    ) {}
}



