package com.relief.controller.security;

import com.relief.service.security.ZeroTrustService;
import com.relief.service.security.ZeroTrustService.VerificationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security/zero-trust")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Zero Trust", description = "Continuous verification of users and devices")
public class ZeroTrustController {

    private final ZeroTrustService zeroTrustService;

    @PostMapping("/verify")
    @Operation(summary = "Verify access under zero-trust policy")
    public ResponseEntity<VerificationResult> verify(
            @RequestParam String userId,
            @RequestParam String deviceId,
            @RequestParam String ipAddress,
            @RequestParam String resource,
            @RequestParam String action) {
        VerificationResult result = zeroTrustService.verifyAccess(userId, deviceId, ipAddress, resource, action);
        return ResponseEntity.ok(result);
    }
}




