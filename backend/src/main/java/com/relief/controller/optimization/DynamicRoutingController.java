package com.relief.controller.optimization;

import com.relief.service.optimization.DynamicRoutingService;
import com.relief.service.optimization.DynamicRoutingService.Route;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/optimization/routing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dynamic Routing", description = "AI-powered optimal delivery routes")
public class DynamicRoutingController {

    private final DynamicRoutingService routingService;

    @PostMapping("/routes")
    @Operation(summary = "Create optimized route")
    public ResponseEntity<Route> createRoute(
            @RequestParam String originLat,
            @RequestParam String originLon,
            @RequestBody List<String> destinations,
            @RequestBody(required = false) Map<String, Object> constraints) {
        
        Route route = routingService.createOptimizedRoute(originLat, originLon, destinations, constraints != null ? constraints : Map.of());
        return ResponseEntity.ok(route);
    }

    @PostMapping("/routes/{routeId}/reoptimize")
    @Operation(summary = "Re-optimize route with new conditions")
    public ResponseEntity<Route> reoptimizeRoute(
            @PathVariable String routeId,
            @RequestBody Map<String, Object> newConditions) {
        
        Route route = routingService.reoptimizeRoute(routeId, newConditions);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/routes/{routeId}")
    @Operation(summary = "Get route")
    public ResponseEntity<Route> getRoute(@PathVariable String routeId) {
        Route route = routingService.getRoute(routeId);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/routes")
    @Operation(summary = "Get all routes")
    public ResponseEntity<List<Route>> getRoutes() {
        List<Route> routes = routingService.getRoutes();
        return ResponseEntity.ok(routes);
    }
}



