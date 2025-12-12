# Download MinIO Binary for Windows
# This script downloads MinIO binary without requiring installation

Write-Host "Downloading MinIO binary..." -ForegroundColor Green

$minioDir = "$PSScriptRoot\minio-bin"
$minioExe = "$minioDir\minio.exe"

# Create directory if it doesn't exist
if (-not (Test-Path $minioDir)) {
    New-Item -ItemType Directory -Path $minioDir -Force | Out-Null
}

# Check if MinIO already exists
if (Test-Path $minioExe) {
    Write-Host "MinIO binary already exists at: $minioExe" -ForegroundColor Yellow
    Write-Host "To re-download, delete the file first." -ForegroundColor Yellow
    exit 0
}

# Download MinIO for Windows
$minioUrl = "https://dl.min.io/server/minio/release/windows-amd64/minio.exe"
$downloadPath = "$minioDir\minio.exe"

Write-Host "Downloading from: $minioUrl" -ForegroundColor Cyan
Write-Host "Saving to: $downloadPath" -ForegroundColor Cyan

try {
    Invoke-WebRequest -Uri $minioUrl -OutFile $downloadPath -UseBasicParsing
    Write-Host ""
    Write-Host "MinIO downloaded successfully!" -ForegroundColor Green
    Write-Host "Location: $downloadPath" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To start MinIO, run:" -ForegroundColor Yellow
    Write-Host "  .\start-minio-local.ps1" -ForegroundColor Cyan
} catch {
    Write-Host "Failed to download MinIO: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Manual download:" -ForegroundColor Yellow
    Write-Host "1. Visit: https://min.io/download" -ForegroundColor Cyan
    Write-Host "2. Download MinIO for Windows" -ForegroundColor Cyan
    Write-Host "3. Extract minio.exe to: $minioDir" -ForegroundColor Cyan
    exit 1
}


