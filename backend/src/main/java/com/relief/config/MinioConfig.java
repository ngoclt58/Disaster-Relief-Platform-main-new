package com.relief.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

/**
 * MinIO Client Configuration for MinIO Java SDK 8.5.7
 * 
 * Root Cause Analysis:
 * - MinIO Java SDK 8.x validates hostname using HttpUtils.validateHostnameOrIPAddress()
 * - This method rejects "localhost:9000" because it contains a colon, which is invalid in a hostname
 * - Hostname must be just the domain/IP (e.g., "localhost"), port must be handled separately
 * 
 * Solution:
 * - Extract hostname and port from the endpoint URL
 * - Use hostname only in .endpoint() method
 * - For non-standard ports, MinIO SDK 8.x supports URL-based endpoint format
 */
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "storage.type", 
        havingValue = "minio", 
        matchIfMissing = false
    )
    public MinioClient minioClient() {
        try {
            // Normalize endpoint to always be a full URL
            String normalizedEndpoint = normalizeEndpoint(endpoint);

            // MinIO Java SDK 8.5.7 accepts full URL format in endpoint()
            // The SDK will parse the URL internally and extract hostname/port
            return MinioClient.builder()
                    .endpoint(normalizedEndpoint)
                    .credentials(accessKey, secretKey)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to create MinIO client. Endpoint: " + endpoint + 
                    ". Please check your minio.endpoint configuration. " +
                    "Supported formats: http://hostname:port, https://hostname:port, or hostname:port. " +
                    "Error: " + e.getMessage(), e);
        }
    }

    /**
     * Normalize endpoint to full URL format
     * Handles: http://localhost:9000, https://minio.example.com:9000, localhost:9000, localhost
     */
    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("MinIO endpoint cannot be null or empty");
        }

        endpoint = endpoint.trim();

        // If already a full URL, validate and return
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            try {
                // Validate URL format
                new URL(endpoint);
                return endpoint;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid MinIO endpoint URL: " + endpoint, e);
            }
        }

        // Handle hostname:port format (e.g., localhost:9000)
        if (endpoint.contains(":")) {
            String[] parts = endpoint.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid endpoint format: " + endpoint + 
                        ". Expected: hostname:port or http://hostname:port");
            }
            String hostname = parts[0].trim();
            String portStr = parts[1].trim();
            try {
                int port = Integer.parseInt(portStr);
                // Default to HTTP for hostname:port format
                return "http://" + hostname + ":" + port;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number in endpoint: " + endpoint, e);
            }
        }

        // Just hostname (e.g., localhost) - default to HTTP on port 9000
        return "http://" + endpoint + ":9000";
    }
}





