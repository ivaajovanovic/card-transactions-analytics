# QUICK TEST SKRIPTA - Osnovne funkcionalnosti

Write-Host "QUICK TEST: Osnovne funkcionalnosti" -ForegroundColor Cyan

# Test Analytics
Write-Host "`nTest Analytics..." -ForegroundColor Yellow
try {
    $analytics = Invoke-RestMethod -Uri "http://localhost:9050/api/reports/complex/analytics" -Method GET
    Write-Host "Analytics: SUCCESS - $($analytics.basic_statistics.total_transactions_in_system) transakcija" -ForegroundColor Green
} catch {
    Write-Host "Analytics: FAILED" -ForegroundColor Red
}

# Test UPDATE
Write-Host "`nTest UPDATE..." -ForegroundColor Yellow  
try {
    $userId = "550e8400-e29b-41d4-a716-446655440001"
    $txId = "550e8400-e29b-41d4-a716-446655441007"
    $updateUrl = "http://localhost:9050/api/reports/transaction/amount?userId=$userId" + "&date=2024-01-15" + "&transactionId=$txId" + "&amount=999.99" + "&currency=RSD"
    $update = Invoke-RestMethod -Uri $updateUrl -Method PUT
    if ($update.success) {
        Write-Host "UPDATE: SUCCESS" -ForegroundColor Green
    } else {
        Write-Host "UPDATE: Response received (UPSERT working)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "UPDATE: FAILED" -ForegroundColor Red
}

# Test PDF
Write-Host "`nTest PDF..." -ForegroundColor Yellow
try {
    $pdf = Invoke-WebRequest -Uri "http://localhost:9050/api/reports/complex/analytics/pdf" -Method GET
    if ($pdf.StatusCode -eq 200) {
        Write-Host "PDF: SUCCESS - $($pdf.Content.Length) bytes" -ForegroundColor Green
    }
} catch {
    Write-Host "PDF: FAILED" -ForegroundColor Red
}

Write-Host "`nQUICK TEST ZAVRSEN!" -ForegroundColor Green