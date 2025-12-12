package com.relief.controller.security;

import com.relief.service.security.EncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Map;

@RestController
@RequestMapping("/security/crypto")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Advanced Encryption", description = "End-to-end encryption utilities")
public class EncryptionController {

    private final EncryptionService encryptionService;

    @PostMapping("/encrypt")
    @Operation(summary = "Encrypt plaintext with generated key")
    public ResponseEntity<Map<String, String>> encrypt(@RequestBody String plaintext) throws Exception {
        SecretKey key = encryptionService.generateKey(256);
        String cipher = encryptionService.encrypt(plaintext, key);
        return ResponseEntity.ok(Map.of(
                "cipher", cipher,
                "keyAlgorithm", key.getAlgorithm()
        ));
    }

    @PostMapping("/decrypt")
    @Operation(summary = "Decrypt ciphertext using provided key not supported in demo")
    public ResponseEntity<String> decrypt() {
        return ResponseEntity.badRequest().body("Provide server-managed key. Demo supports encrypt only.");
    }
}




