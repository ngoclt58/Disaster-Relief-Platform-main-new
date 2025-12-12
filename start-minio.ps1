# Start MinIO Server Script for Windows
# This script starts MinIO server for local development

Write-Host "Starting MinIO Server..." -ForegroundColor Green

# Check if MinIO is already running
$minioProcess = Get-Process -Name "minio" -ErrorAction SilentlyContinue
if ($minioProcess) {
    Write-Host "MinIO is already running (PID: $($minioProcess.Id))" -ForegroundColor Yellow
    Write-Host "Access MinIO Console at: http://localhost:9001" -ForegroundColor Cyan
    Write-Host "Default credentials: minioadmin / minioadmin123" -ForegroundColor Cyan
    exit 0
}

# Check if MinIO executable exists
$minioExe = Get-Command minio -ErrorAction SilentlyContinue
if (-not $minioExe) {
    Write-Host "MinIO executable not found in PATH." -ForegroundColor Red
    Write-Host ""
    Write-Host "Option 1: Install MinIO using Chocolatey:" -ForegroundColor Yellow
    Write-Host "  choco install minio" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Option 2: Download from https://min.io/download" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Option 3: Use Docker (if Docker is installed):" -ForegroundColor Yellow
    Write-Host "  docker run -d -p 9000:9000 -p 9001:9001 --name minio1 `" -ForegroundColor Cyan
    Write-Host "    -e `"MINIO_ROOT_USER=minioadmin`" `" -ForegroundColor Cyan
    Write-Host "    -e `"MINIO_ROOT_PASSWORD=minioadmin123`" `" -ForegroundColor Cyan
    Write-Host "    quay.io/minio/minio server /data --console-address `":9001`"" -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

# Create data directory if it doesn't exist
$dataDir = "$PSScriptRoot\minio-data"
if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
    Write-Host "Created MinIO data directory: $dataDir" -ForegroundColor Green
}

# Start MinIO server
Write-Host "Starting MinIO server on port 9000..." -ForegroundColor Green
Write-Host "Console will be available at: http://localhost:9001" -ForegroundColor Cyan
Write-Host "Default credentials: minioadmin / minioadmin123" -ForegroundColor Cyan
Write-Host ""

Start-Process -FilePath "minio" -ArgumentList "server", $dataDir, "--console-address", ":9001" -WindowStyle Normal

Start-Sleep -Seconds 2

# Verify MinIO is running
$minioProcess = Get-Process -Name "minio" -ErrorAction SilentlyContinue
if ($minioProcess) {
    Write-Host "MinIO server started successfully!" -ForegroundColor Green
    Write-Host "Process ID: $($minioProcess.Id)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Access MinIO Console: http://localhost:9001" -ForegroundColor Yellow
    Write-Host "API Endpoint: http://localhost:9000" -ForegroundColor Yellow
    Write-Host "Credentials: minioadmin / minioadmin123" -ForegroundColor Yellow
} else {
    Write-Host "Failed to start MinIO server. Please check the error messages above." -ForegroundColor Red
    exit 1
}


