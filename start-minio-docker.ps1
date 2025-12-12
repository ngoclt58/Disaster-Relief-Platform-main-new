# Start MinIO Server using Docker
# This script starts MinIO server in a Docker container

Write-Host "Starting MinIO Server using Docker..." -ForegroundColor Green

# Check if Docker is available
try {
    docker --version | Out-Null
} catch {
    Write-Host "Docker is not installed or not in PATH." -ForegroundColor Red
    Write-Host "Please install Docker Desktop from https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Check if MinIO container already exists
$existingContainer = docker ps -a --filter "name=minio1" --format "{{.Names}}" 2>$null
if ($existingContainer -eq "minio1") {
    $running = docker ps --filter "name=minio1" --format "{{.Names}}" 2>$null
    if ($running -eq "minio1") {
        Write-Host "MinIO container is already running!" -ForegroundColor Yellow
        Write-Host "Access MinIO Console at: http://localhost:9001" -ForegroundColor Cyan
        Write-Host "Default credentials: minioadmin / minioadmin123" -ForegroundColor Cyan
        exit 0
    } else {
        Write-Host "Starting existing MinIO container..." -ForegroundColor Yellow
        docker start minio1
        Start-Sleep -Seconds 3
        Write-Host "MinIO container started!" -ForegroundColor Green
        Write-Host "Access MinIO Console at: http://localhost:9001" -ForegroundColor Cyan
        exit 0
    }
}

# Create data directory
$dataDir = "$PSScriptRoot\minio-data"
if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
    Write-Host "Created MinIO data directory: $dataDir" -ForegroundColor Green
}

# Start MinIO container
Write-Host "Starting MinIO Docker container..." -ForegroundColor Green
docker run -d `
    -p 9000:9000 `
    -p 9001:9001 `
    --name minio1 `
    -e "MINIO_ROOT_USER=minioadmin" `
    -e "MINIO_ROOT_PASSWORD=minioadmin123" `
    -v "${dataDir}:/data" `
    quay.io/minio/minio server /data --console-address ":9001"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "MinIO server started successfully in Docker!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Access MinIO Console: http://localhost:9001" -ForegroundColor Yellow
    Write-Host "API Endpoint: http://localhost:9000" -ForegroundColor Yellow
    Write-Host "Credentials: minioadmin / minioadmin123" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To stop MinIO: docker stop minio1" -ForegroundColor Cyan
    Write-Host "To remove MinIO: docker rm -f minio1" -ForegroundColor Cyan
} else {
    Write-Host "Failed to start MinIO container. Please check Docker is running." -ForegroundColor Red
    exit 1
}


