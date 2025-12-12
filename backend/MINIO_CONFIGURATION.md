# MinIO Configuration Guide

## Root Cause Analysis

### Error: `invalid hostname localhost:9000`

**Problem:**
- MinIO Java SDK 8.5.7 validates the endpoint using `HttpUtils.validateHostnameOrIPAddress()`
- This method rejects `"localhost:9000"` because hostnames cannot contain colons (`:`)
- The SDK expects either:
  1. A full URL format: `http://localhost:9000` or `https://minio.example.com:9000`
  2. Just the hostname (without port): `localhost` (uses default ports)

**Why `localhost:9000` fails:**
- The colon (`:`) is a port separator, not part of a valid hostname
- Hostname validation follows RFC 1123 standards where hostnames can only contain:
  - Letters (a-z, A-Z)
  - Digits (0-9)
  - Hyphens (-)
  - Dots (.) for domain names
- Port numbers must be specified separately or included in a full URL

## Solution

The `MinioConfig` class now normalizes all endpoint formats to full URLs before passing to the MinIO client.

### Supported Endpoint Formats

1. **Full URL (Recommended):**
   ```yaml
   minio:
     endpoint: http://localhost:9000
     # or
     endpoint: https://minio.example.com:9000
   ```

2. **Hostname:Port (Auto-converted to HTTP):**
   ```yaml
   minio:
     endpoint: localhost:9000
     # Automatically converted to: http://localhost:9000
   ```

3. **Just Hostname (Defaults to HTTP port 9000):**
   ```yaml
   minio:
     endpoint: localhost
     # Automatically converted to: http://localhost:9000
   ```

## Configuration Examples

### Local Development

```yaml
# application.yml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin123
  bucket-name: relief-media
  secure: false
```

### Production (Environment Variables)

```yaml
# application.yml
minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin123}
  bucket-name: ${MINIO_BUCKET_NAME:relief-media}
  secure: ${MINIO_SECURE:false}
```

**Environment Variables:**
```bash
# Production
export MINIO_ENDPOINT=https://minio.production.example.com:9000
export MINIO_ACCESS_KEY=your-access-key
export MINIO_SECRET_KEY=your-secret-key
export MINIO_BUCKET_NAME=relief-media-prod
export MINIO_SECURE=true
```

### Docker/Kubernetes

```yaml
# docker-compose.yml or k8s config
environment:
  - MINIO_ENDPOINT=http://minio:9000
  - MINIO_ACCESS_KEY=minioadmin
  - MINIO_SECRET_KEY=minioadmin123
  - MINIO_BUCKET_NAME=relief-media
  - MINIO_SECURE=false
```

## MinIO Java SDK Version

- **Current Version:** 8.5.7
- **Compatibility:** Spring Boot 3.2.0+
- **Java Version:** 17+

## Verification

After configuration, verify the MinIO client is created successfully:

1. Check application startup logs for:
   ```
   MinIO client initialized successfully
   ```

2. If errors occur, check:
   - MinIO server is running
   - Endpoint is accessible from application
   - Credentials are correct
   - Network connectivity (firewall, DNS)

## Troubleshooting

### Error: "Failed to create MinIO client"

**Possible causes:**
1. Invalid endpoint format
2. MinIO server not running
3. Network connectivity issues
4. Incorrect credentials

**Solution:**
- Verify endpoint format (should be full URL)
- Check MinIO server status: `docker ps` or service status
- Test connectivity: `curl http://localhost:9000/minio/health/live`
- Verify credentials in MinIO console

### Error: "Connection refused"

**Possible causes:**
1. MinIO server not running
2. Wrong port number
3. Firewall blocking connection

**Solution:**
- Start MinIO server
- Verify port in endpoint matches MinIO server port
- Check firewall rules

## Additional Notes

- The `secure` property is auto-detected from the endpoint URL (https = secure)
- Bucket will be created automatically if it doesn't exist
- Default ports: HTTP=9000, HTTPS=443
- For production, always use HTTPS with proper SSL certificates


