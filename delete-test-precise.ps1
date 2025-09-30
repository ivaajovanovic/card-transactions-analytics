# DELETE TEST - PRECIZNO SA KREIRANOM TRANSAKCIJOM

Write-Host "=== DELETE TEST SA PRECIZNIM PODACIMA ===" -ForegroundColor Cyan

# Koristi kreiranu transakciju iz prethodnog testa
$targetTxId = "550e8400-e29b-41d4-a716-446655449999"
$targetUserId = "550e8400-e29b-41d4-a716-446655440001"

Write-Host "`n=== PROVERI DA LI POSTOJI TRANSAKCIJA PRE DELETE ===" -ForegroundColor Green

try {
    Write-Host "Pozivam GET za transakciju: $targetTxId" -ForegroundColor Cyan
    $beforeDelete = Invoke-RestMethod -Uri "http://localhost:9050/api/tx/$targetTxId" -Method GET
    Write-Host "TRANSAKCIJA POSTOJALA PRE DELETE:" -ForegroundColor Green
    Write-Host "  TX ID: $($beforeDelete.txId)" -ForegroundColor White
    Write-Host "  User ID: $($beforeDelete.userId)" -ForegroundColor White
    Write-Host "  Amount: $($beforeDelete.amountCents) centi" -ForegroundColor White
    Write-Host "  Currency: $($beforeDelete.currency)" -ForegroundColor White
    Write-Host "  Status: $($beforeDelete.status)" -ForegroundColor White
    
    $transactionExists = $true
} catch {
    Write-Host "Transakcija ne postoji pre DELETE: $($_.Exception.Message)" -ForegroundColor Red
    $transactionExists = $false
}

if ($transactionExists) {
    Write-Host "`n=== IZVRŠAVAM DELETE OPERACIJU ===" -ForegroundColor Green
    
    try {
        # Pokušaj sa ReportController endpoint
        Write-Host "Pozivam DELETE endpoint: DELETE /api/reports/user/{userId}/transaction" -ForegroundColor Cyan
        $deleteUrl = "http://localhost:9050/api/reports/user/$targetUserId/transaction?transactionId=$targetTxId&date=2024-09-30"
        $deleteResult = Invoke-RestMethod -Uri $deleteUrl -Method DELETE
        
        Write-Host "DELETE REZULTAT:" -ForegroundColor Green
        Write-Host "  Success: $($deleteResult.success)" -ForegroundColor White
        Write-Host "  Operation: $($deleteResult.operation)" -ForegroundColor White
        Write-Host "  Transaction ID: $($deleteResult.transaction_id)" -ForegroundColor White
        
        if ($deleteResult.success -eq $true) {
            Write-Host "  ✅ DELETE OPERATION SUCCESS = TRUE!" -ForegroundColor Green
        } else {
            Write-Host "  ❌ DELETE OPERATION SUCCESS = FALSE" -ForegroundColor Red
        }
        
    } catch {
        Write-Host "DELETE FAILED: $($_.Exception.Message)" -ForegroundColor Red
        
        # Pokušaj alternativan endpoint
        Write-Host "`nPokušavam sa TxController DELETE endpoint..." -ForegroundColor Yellow
        
        try {
            # Možda postoji direktan DELETE endpoint u TxController
            Write-Host "Pozivam TxController DELETE: DELETE /api/tx/{txId}" -ForegroundColor Cyan
            $directDeleteUrl = "http://localhost:9050/api/tx/$targetTxId"
            $directDeleteResult = Invoke-RestMethod -Uri $directDeleteUrl -Method DELETE
            
            Write-Host "DIRECT DELETE REZULTAT:" -ForegroundColor Green
            Write-Host "  Status: SUCCESS (Void response)" -ForegroundColor Green
            Write-Host "  ✅ DIRECT DELETE SUCCESSFUL!" -ForegroundColor Green
            
        } catch {
            Write-Host "DIRECT DELETE ALSO FAILED: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
    # Verify DELETE - proveri da li je transakcija obrisana
    Write-Host "`n=== PROVERI DA LI JE TRANSAKCIJA OBRISANA ===" -ForegroundColor Green
    
    Start-Sleep -Seconds 2
    
    try {
        Write-Host "Pozivam GET ponovo za transakciju: $targetTxId" -ForegroundColor Cyan
        $afterDelete = Invoke-RestMethod -Uri "http://localhost:9050/api/tx/$targetTxId" -Method GET
        Write-Host "TRANSAKCIJA JOŠ UVEK POSTOJI:" -ForegroundColor Yellow
        Write-Host "  TX ID: $($afterDelete.txId)" -ForegroundColor White
        Write-Host "  ❌ DELETE MOŽDA NIJE USPEŠAN" -ForegroundColor Red
        
    } catch {
        Write-Host "Transakcija NIJE PRONAĐENA nakon DELETE: $($_.Exception.Message)" -ForegroundColor Green
        Write-Host "  ✅ DELETE JE USPEŠNO OBRISAO TRANSAKCIJU!" -ForegroundColor Green
    }
    
    # Proveri totals analytics
    Write-Host "`n=== PROVERI TOTALS NAKON DELETE ===" -ForegroundColor Green
    
    try {
        Write-Host "Pozivam analytics..." -ForegroundColor Cyan
        $analytics = Invoke-RestMethod -Uri "http://localhost:9050/api/reports/complex/analytics" -Method GET
        Write-Host "ANALYTICS NAKON DELETE:" -ForegroundColor Green
        Write-Host "  Total transakcija: $($analytics.basic_statistics.total_transactions_in_system)" -ForegroundColor White
        Write-Host "  Ukupan iznos: $($analytics.basic_statistics.total_amount_in_system_rsd) RSD" -ForegroundColor White
        
        if ($analytics.basic_statistics.total_transactions_in_system -eq 550) {
            Write-Host "  ✅ BROJ TRANSAKCIJA SE SMANJIO - DELETE USPEŠAN!" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  Broj transakcija: $($analytics.basic_statistics.total_transactions_in_system) (očekivano 550)" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "Analytics failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} else {
    Write-Host "Ne mogu da testiram DELETE jer transakcija ne postoji." -ForegroundColor Red
}

Write-Host "`n🎯 DELETE TEST ZAVRŠEN 🎯" -ForegroundColor Cyan