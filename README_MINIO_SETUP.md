# MinIO Setup Guide

## Current Error

**Error:** `Failed to connect to localhost:9000`

**Root Cause:** MinIO server is not running on port 9000.

**This is DIFFERENT from the previous TypeScript error:**
- Previous error: TypeScript compilation error (fixed ✅)
- Current error: MinIO server connectivity error (needs MinIO server running)

## Quick Start - Start MinIO Server

### Option 1: Using Docker (Recommended)

```powershell
# Run the Docker script
.\start-minio-docker.ps1
```

Or manually:
```powershell
docker run -d -p 9000:9000 -p 9001:9001 --name minio1 `
  -e "MINIO_ROOT_USER=minioadmin" `
  -e "MINIO_ROOT_PASSWORD=minioadmin123" `
  -v "${PWD}\minio-data:/data" `
  quay.io/minio/minio server /data --console-address ":9001"
```

### Option 2: Using MinIO Binary

1. Download MinIO from https://min.io/download
2. Extract and add to PATH
3. Run:
```powershell
.\start-minio.ps1
```

Or manually:
```powershell
minio server .\minio-data --console-address ":9001"
```

## Verify MinIO is Running

1. **Check port 9000:**
   ```powershell
   Test-NetConnection -ComputerName localhost -Port 9000
   ```
   Should return `TcpTestSucceeded : True`

2. **Access MinIO Console:**
   - URL: http://localhost:9001
   - Username: `minioadmin`
   - Password: `minioadmin123`

3. **Test API endpoint:**
   ```powershell
   curl http://localhost:9000/minio/health/live
   ```

## Configuration

Your `application.yml` should have:
```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin123
  bucket-name: relief-media
```

## After Starting MinIO

1. **Restart your Spring Boot backend** to ensure it connects to MinIO
2. **Refresh your frontend** and try creating a need request again

## Troubleshooting

### Port 9000 already in use
```powershell
# Find process using port 9000
netstat -ano | findstr :9000

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### MinIO container won't start
```powershell
# Check Docker logs
docker logs minio1

# Remove and recreate container
docker rm -f minio1
.\start-minio-docker.ps1
```

### Connection still fails after starting MinIO
1. Verify MinIO is running: `Test-NetConnection localhost -Port 9000`
2. Check backend logs for MinIO connection errors
3. Verify credentials match in `application.yml`
4. Restart backend application


