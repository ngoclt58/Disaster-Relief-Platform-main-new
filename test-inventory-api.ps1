#!/usr/bin/env pwsh

Write-Host "🔍 Testing Inventory API..." -ForegroundColor Green

# Test if backend is running
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/debug/inventory/summary" -Method GET -TimeoutSec 5
    Write-Host "✅ Backend is running!" -ForegroundColor Green
    Write-Host "📊 Inventory Summary:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Backend not running or error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "🚀 Starting backend..." -ForegroundColor Yellow
    
    # Change to backend directory and start
    Set-Location backend
    
    # Start backend in background
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Minimized
    
    Write-Host "⏳ Waiting for backend to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    # Test again
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/debug/inventory/summary" -Method GET -TimeoutSec 10
        Write-Host "✅ Backend started successfully!" -ForegroundColor Green
        Write-Host "📊 Inventory Summary:" -ForegroundColor Yellow
        $response | ConvertTo-Json -Depth 3
    } catch {
        Write-Host "❌ Backend still not responding: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n🧪 Testing specific APIs..." -ForegroundColor Green

# Test hubs API
try {
    $hubs = Invoke-RestMethod -Uri "http://localhost:8080/inventory/hubs" -Method GET
    Write-Host "✅ Hubs API: Found $($hubs.Count) hubs" -ForegroundColor Green
    $hubs | ForEach-Object { Write-Host "  - $($_.name)" -ForegroundColor Cyan }
} catch {
    Write-Host "❌ Hubs API error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test items API
try {
    $items = Invoke-RestMethod -Uri "http://localhost:8080/inventory/items" -Method GET
    Write-Host "✅ Items API: Found $($items.Count) items" -ForegroundColor Green
    $items | Select-Object -First 3 | ForEach-Object { Write-Host "  - $($_.name) ($($_.code))" -ForegroundColor Cyan }
} catch {
    Write-Host "❌ Items API error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test stock API
try {
    $stock = Invoke-RestMethod -Uri "http://localhost:8080/inventory/stock" -Method GET
    Write-Host "✅ Stock API: Found $($stock.Count) stock entries" -ForegroundColor Green
    
    # Analyze stock
    $withHub = ($stock | Where-Object { $_.hub -ne $null }).Count
    $withItem = ($stock | Where-Object { $_.item -ne $null }).Count
    $withBoth = ($stock | Where-Object { $_.hub -ne $null -and $_.item -ne $null }).Count
    
    Write-Host "  📈 Stock Analysis:" -ForegroundColor Yellow
    Write-Host "    - Total entries: $($stock.Count)" -ForegroundColor White
    Write-Host "    - With hub: $withHub" -ForegroundColor White
    Write-Host "    - With item: $withItem" -ForegroundColor White
    Write-Host "    - With both: $withBoth" -ForegroundColor White
    
    if ($stock.Count -gt 0) {
        Write-Host "  📋 Sample stock entry:" -ForegroundColor Yellow
        $sample = $stock[0]
        Write-Host "    - ID: $($sample.id)" -ForegroundColor White
        Write-Host "    - Hub: $($sample.hub.name)" -ForegroundColor White
        Write-Host "    - Item: $($sample.item.name)" -ForegroundColor White
        Write-Host "    - Available: $($sample.qtyAvailable)" -ForegroundColor White
        Write-Host "    - Reserved: $($sample.qtyReserved)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Stock API error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎯 Test completed!" -ForegroundColor Green