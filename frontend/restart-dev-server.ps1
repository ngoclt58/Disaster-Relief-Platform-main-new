# Stop any running processes on port 3000
Write-Host "Stopping any processes on port 3000..."
Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
}

# Clear caches
Write-Host "Clearing caches..."
if (Test-Path "node_modules\.cache") {
    Remove-Item -Recurse -Force "node_modules\.cache" -ErrorAction SilentlyContinue
}
if (Test-Path ".cache") {
    Remove-Item -Recurse -Force ".cache" -ErrorAction SilentlyContinue
}

Write-Host "Cache cleared. Starting dev server..."
npm start



