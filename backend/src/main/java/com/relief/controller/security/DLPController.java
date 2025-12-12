package com.relief.controller.security;

import com.relief.service.security.DLPService;
import com.relief.service.security.DLPService.DLPScanResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/security/dlp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Loss Prevention", description = "Detect and prevent sensitive data leaks")
public class DLPController {

    private final DLPService dlpService;

    @PostMapping("/scan")
    @Operation(summary = "Scan content for sensitive data")
    public ResponseEntity<DLPScanResult> scan(@RequestBody String content, @RequestParam(required = false) Map<String, Object> options) {
        DLPScanResult result = dlpService.scan(content, options);
        return ResponseEntity.ok(result);
    }
}




