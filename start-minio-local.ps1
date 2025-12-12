# Start MinIO Server using local binary (no Docker required)
# This script starts MinIO server using the downloaded binary

Write-Host "Starting MinIO Server (Local Binary)..." -ForegroundColor Green

$minioDir = "$PSScriptRoot\minio-bin"
$minioExe = "$minioDir\minio.exe"

# Check if MinIO binary exists
if (-not (Test-Path $minioExe)) {
    Write-Host "MinIO binary not found at: $minioExe" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please run first:" -ForegroundColor Yellow
    Write-Host "  .\download-minio.ps1" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Or download manually from: https://min.io/download" -ForegroundColor Yellow
    exit 1
}

# Check if MinIO is already running
$minioProcess = Get-Process -Name "minio" -ErrorAction SilentlyContinue
if ($minioProcess) {
    Write-Host "MinIO is already running (PID: $($minioProcess.Id))" -ForegroundColor Yellow
    Write-Host "Access MinIO Console at: http://localhost:9001" -ForegroundColor Cyan
    Write-Host "Default credentials: minioadmin / minioadmin123" -ForegroundColor Cyan
    exit 0
}

# Create data directory if it doesn't exist
$dataDir = "$PSScriptRoot\minio-data"
if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
    Write-Host "Created MinIO data directory: $dataDir" -ForegroundColor Green
}

# Start MinIO server
Write-Host "Starting MinIO server..." -ForegroundColor Green
Write-Host "API Endpoint: http://localhost:9000" -ForegroundColor Cyan
Write-Host "Console: http://localhost:9001" -ForegroundColor Cyan
Write-Host "Credentials: minioadmin / minioadmin123" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop MinIO server" -ForegroundColor Yellow
Write-Host ""

# Start MinIO in a new window so it doesn't block
Start-Process -FilePath $minioExe -ArgumentList "server", $dataDir, "--console-address", ":9001" -WindowStyle Normal

Start-Sleep -Seconds 3

# Verify MinIO is running
$minioProcess = Get-Process -Name "minio" -ErrorAction SilentlyContinue
if ($minioProcess) {
    Write-Host "MinIO server started successfully!" -ForegroundColor Green
    Write-Host "Process ID: $($minioProcess.Id)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Access MinIO Console: http://localhost:9001" -ForegroundColor Yellow
    Write-Host "API Endpoint: http://localhost:9000" -ForegroundColor Yellow
    Write-Host "Credentials: minioadmin / minioadmin123" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To stop MinIO, close the window or run:" -ForegroundColor Cyan
    Write-Host "  Stop-Process -Name minio -Force" -ForegroundColor Cyan
} else {
    Write-Host "Failed to start MinIO server. Please check the error messages above." -ForegroundColor Red
    exit 1
}


