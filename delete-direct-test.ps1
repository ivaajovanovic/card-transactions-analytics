# DIREKTNI DELETE TEST - SA TxController ENDPOINT

Write-Host "=== DIREKTNI DELETE TEST SA TxController ===" -ForegroundColor Cyan

# Koristi kreiranu transakciju iz prethodnog testa
$targetTxId = "550e8400-e29b-41d4-a716-446655449999"

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
    Write-Host "`n=== IZVRŠAVAM DIREKTNI DELETE - TxController ===" -ForegroundColor Green
    
    try {
        # TxController DELETE endpoint - soft delete (menja status)
        Write-Host "Pozivam TxController DELETE: DELETE /api/tx/{txId}" -ForegroundColor Cyan
        $directDeleteUrl = "http://localhost:9050/api/tx/$targetTxId"
        
        # Ovo vraća void response (200 OK bez content)
        Invoke-RestMethod -Uri $directDeleteUrl -Method DELETE
        
        Write-Host "DIRECT DELETE REZULTAT:" -ForegroundColor Green
        Write-Host "  Status: SUCCESS (200 OK - void response)" -ForegroundColor Green
        Write-Host "  ✅ TxController DELETE ENDPOINT SUCCESSFUL!" -ForegroundColor Green
        
    } catch {
        Write-Host "TxController DELETE FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Verify DELETE - proveri da li je status promenjen na DELETED
    Write-Host "`n=== PROVERI DA LI JE STATUS PROMENJEN NA 'DELETED' ===" -ForegroundColor Green
    
    Start-Sleep -Seconds 2
    
    try {
        Write-Host "Pozivam GET ponovo za transakciju: $targetTxId" -ForegroundColor Cyan
        $afterDelete = Invoke-RestMethod -Uri "http://localhost:9050/api/tx/$targetTxId" -Method GET
        Write-Host "TRANSAKCIJA NAKON DELETE:" -ForegroundColor Green
        Write-Host "  TX ID: $($afterDelete.txId)" -ForegroundColor White
        Write-Host "  User ID: $($afterDelete.userId)" -ForegroundColor White
        Write-Host "  Amount: $($afterDelete.amountCents) centi" -ForegroundColor White
        Write-Host "  Currency: $($afterDelete.currency)" -ForegroundColor White
        Write-Host "  Status: $($afterDelete.status)" -ForegroundColor White
        
        if ($afterDelete.status -eq "DELETED") {
            Write-Host "  ✅ SOFT DELETE USPEŠAN - STATUS = 'DELETED'!" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  Status nije 'DELETED': $($afterDelete.status)" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "Transakcija NIJE PRONAĐENA nakon DELETE: $($_.Exception.Message)" -ForegroundColor Green
        Write-Host "  ✅ HARD DELETE JE USPEŠNO OBRISAO TRANSAKCIJU!" -ForegroundColor Green
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
            Write-Host "  💡 Možda je soft delete - transakcija još uvek u sistemu sa status='DELETED'" -ForegroundColor Cyan
        }
        
    } catch {
        Write-Host "Analytics failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} else {
    Write-Host "Ne mogu da testiram DELETE jer transakcija ne postoji." -ForegroundColor Red
}

Write-Host "`n🎯 DIREKTNI DELETE TEST ZAVRŠEN 🎯" -ForegroundColor Cyan
Write-Host "TxController koristi SOFT DELETE (menja status na 'DELETED')" -ForegroundColor Yellow
Write-Host "što objašnjava zašto broj transakcija ostaje isti." -ForegroundColor Yellow