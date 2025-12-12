package com.relief.controller;

import com.relief.realtime.RealtimeBroadcaster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@RestController
@RequestMapping("/requests/stream")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Realtime", description = "SSE stream for realtime updates")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class RealtimeController {

    private final RealtimeBroadcaster broadcaster;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to Server-Sent Events")
    public SseEmitter stream(@RequestParam(required = false) String token, HttpServletResponse response) {
        log.info("SSE connection requested, token present: {}", token != null);
        
        // Set CORS headers explicitly for SSE
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Cache-Control");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        try {
            SseEmitter emitter = broadcaster.register(3600000L); // 1 hour timeout
            
            emitter.onCompletion(() -> log.info("SSE connection completed"));
            emitter.onTimeout(() -> log.warn("SSE connection timed out"));
            emitter.onError((ex) -> log.error("SSE connection error", ex));
            
            log.info("SSE emitter created and registered");
            return emitter;
        } catch (Exception e) {
            log.error("Failed to create SSE emitter", e);
            throw e;
        }
    }
    
    @RequestMapping(value = "", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> options(HttpServletResponse response) {
        // Handle preflight OPTIONS request for CORS
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Cache-Control, Last-Event-ID");
        response.setHeader("Access-Control-Max-Age", "3600");
        return ResponseEntity.ok().build();
    }
    
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check SSE endpoint health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "endpoint", "/requests/stream"));
    }
}





