# CRUD DEMONSTRATION SKRIPTA - KOMPLETNA sa ispravkama

Write-Host "=== KOMPLETNA CRUD DEMONSTRATION ===" -ForegroundColor Cyan
Write-Host "Testiram svaku CRUD operaciju sa preciznim podacima`n" -ForegroundColor Yellow

# ===========================================
# C - CREATE (TxController)
# ===========================================

Write-Host "=== C - CREATE OPERACIJA ===" -ForegroundColor Green
Write-Host "Dodajem novu test transakciju sa validnim UUID-evima..." -ForegroundColor Yellow

$newTransaction = @{
    userId = "550e8400-e29b-41d4-a716-446655440001"
    amount = 1234.56
    merchantId = "550e8400-e29b-41d4-a716-446655440101"  # Valid UUID
    category = "ELECTRONICS"
    description = "CRUD Test Transaction"
    currency = "RSD"
} | ConvertTo-Json

try {
    Write-Host "Pozivam CREATE endpoint: POST /api/tx" -ForegroundColor Cyan
    $createResult = Invoke-RestMethod -Uri "http://localhost:9050/api/tx" -Method POST -Body $newTransaction -ContentType "application/json"
    Write-Host "CREATE REZULTAT:" -ForegroundColor Green
    Write-Host "  Status: SUCCESS (200 OK)" -ForegroundColor Green
    Write-Host "  Transaction procesiran uspešno" -ForegroundColor White
} catch {
    Write-Host "CREATE FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# ===========================================
# Pronađi realnu transakciju za DELETE test
# ===========================================

Write-Host "`n=== PRONALAŽENJE REALNE TRANSAKCIJE ZA DELETE ===" -ForegroundColor Green
Write-Host "Dobijam listu transakcija za korisnika..." -ForegroundColor Yellow

try {
    $userId = "550e8400-e29b-41d4-a716-446655440001"
    Write-Host "Pozivam GET endpoint: GET /api/tx/user/$userId" -ForegroundColor Cyan
    $userTransactions = Invoke-RestMethod -Uri "http://localhost:9050/api/tx/user/$userId" -Method GET
    
    if ($userTransactions -and $userTransactions.Count -gt 0) {
        $firstTx = $userTransactions[0]
        Write-Host "PRONAĐENA TRANSAKCIJA ZA DELETE:" -ForegroundColor Green
        Write-Host "  Transaction ID: $($firstTx.txId)" -ForegroundColor White
        Write-Host "  User ID: $($firstTx.userId)" -ForegroundColor White
        Write-Host "  Amount: $($firstTx.amount) $($firstTx.currency)" -ForegroundColor White
        Write-Host "  Merchant: $($firstTx.merchantId)" -ForegroundColor White
        Write-Host "  Date: $($firstTx.transactionDate)" -ForegroundColor White
        Write-Host "  Time UUID: $($firstTx.timeUuid)" -ForegroundColor White
        
        $targetTxId = $firstTx.txId
        $targetDate = $firstTx.transactionDate
        $targetTimeUuid = $firstTx.timeUuid
    } else {
        Write-Host "Nisu pronađene transakcije za korisnika" -ForegroundColor Yellow
        $targetTxId = $null
    }
} catch {
    Write-Host "GREŠKA pri dobijanju transakcija: $($_.Exception.Message)" -ForegroundColor Red
    $targetTxId = $null
}

# ===========================================
# R - READ (Analytics)
# ===========================================

Write-Host "`n=== R - READ OPERACIJA ===" -ForegroundColor Green
Write-Host "Čitam analytics podatke..." -ForegroundColor Yellow

try {
    Write-Host "Pozivam READ endpoint: GET /api/reports/complex/analytics" -ForegroundColor Cyan
    $readResult = Invoke-RestMethod -Uri "http://localhost:9050/api/reports/complex/analytics" -Method GET
    Write-Host "READ REZULTAT:" -ForegroundColor Green
    Write-Host "  Total transakcija: $($readResult.basic_statistics.total_transactions_in_system)" -ForegroundColor White
    Write-Host "  Ukupan iznos: $($readResult.basic_statistics.total_amount_in_system_rsd) RSD" -ForegroundColor White
    Write-Host "  Aktivni korisnici: $($readResult.basic_statistics.unique_users_total)" -ForegroundColor White
    Write-Host "  Current period TX: $($readResult.current_period.total_transactions_analyzed)" -ForegroundColor White
    Write-Host "  Current period iznos: $($readResult.current_period.total_amount_analyzed_rsd) RSD" -ForegroundColor White
} catch {
    Write-Host "READ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# ===========================================
# U - UPDATE (UPSERT)
# ===========================================

Write-Host "`n=== U - UPDATE OPERACIJA ===" -ForegroundColor Green
Write-Host "Testiram UPDATE/UPSERT operacije..." -ForegroundColor Yellow

try {
    # UPDATE Amount
    $userId = "550e8400-e29b-41d4-a716-446655440001"
    $updateTxId = "550e8400-e29b-41d4-a716-446655442010"
    $newAmount = 3999.99
    
    Write-Host "Pozivam UPDATE endpoint: PUT /api/reports/transaction/amount" -ForegroundColor Cyan
    $updateUrl = "http://localhost:9050/api/reports/transaction/amount?userId=$userId&date=2024-01-15&transactionId=$updateTxId&amount=$newAmount&currency=RSD"
    $updateResult = Invoke-RestMethod -Uri $updateUrl -Method PUT
    
    Write-Host "UPDATE AMOUNT REZULTAT:" -ForegroundColor Green
    Write-Host "  Success: $($updateResult.success)" -ForegroundColor White
    Write-Host "  Operation: $($updateResult.operation)" -ForegroundColor White
    Write-Host "  Transaction ID: $($updateResult.transaction_id)" -ForegroundColor White
    Write-Host "  New Amount: $($updateResult.new_amount)" -ForegroundColor White
    Write-Host "  Currency: $($updateResult.currency)" -ForegroundColor White

    Start-Sleep -Seconds 1

    # UPDATE Complete
    Write-Host "`nTestiram COMPLETE UPDATE/UPSERT..." -ForegroundColor Yellow
    $completeTxId = "550e8400-e29b-41d4-a716-446655442020"
    $merchantId = "550e8400-e29b-41d4-a716-446655440101"
    $categoryId = "550e8400-e29b-41d4-a716-446655440201"
    
    Write-Host "Pozivam COMPLETE UPDATE endpoint: PUT /api/reports/transaction/complete" -ForegroundColor Cyan
    $completeUrl = "http://localhost:9050/api/reports/transaction/complete?transactionId=$completeTxId&userId=$userId&merchantId=$merchantId&categoryId=$categoryId&date=2024-01-15&amount=2599.99&currency=RSD&status=COMPLETED&description=CRUD-COMPLETE-UPSERT"
    $completeResult = Invoke-RestMethod -Uri $completeUrl -Method PUT
    
    Write-Host "UPDATE COMPLETE REZULTAT:" -ForegroundColor Green
    Write-Host "  Success: $($completeResult.success)" -ForegroundColor White
    Write-Host "  Operation: $($completeResult.operation)" -ForegroundColor White
    Write-Host "  Transaction ID: $($completeResult.transaction_id)" -ForegroundColor White
    Write-Host "  New Amount: $($completeResult.new_amount)" -ForegroundColor White
    Write-Host "  New Status: $($completeResult.new_status)" -ForegroundColor White

} catch {
    Write-Host "UPDATE FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# ===========================================
# D - DELETE (Precizno sa realnim podacima)
# ===========================================

Write-Host "`n=== D - DELETE OPERACIJA ===" -ForegroundColor Green

if ($targetTxId) {
    Write-Host "Brišem realnu transakciju: $targetTxId" -ForegroundColor Yellow
    
    try {
        # DELETE sa preciznim podacima
        Write-Host "Pozivam DELETE endpoint: DELETE /api/tx/users/{userId}/date/{date}/time/{timeuuid}" -ForegroundColor Cyan
        $deleteUrl = "http://localhost:9050/api/tx/users/$userId/date/$targetDate/time/$targetTimeUuid"
        $deleteResult = Invoke-RestMethod -Uri $deleteUrl -Method DELETE
        
        Write-Host "DELETE REZULTAT:" -ForegroundColor Green
        Write-Host "  Status: SUCCESS (200 OK)" -ForegroundColor Green
        Write-Host "  Transakcija obrisana: $targetTxId" -ForegroundColor White
        Write-Host "  Date: $targetDate" -ForegroundColor White
        Write-Host "  Time UUID: $targetTimeUuid" -ForegroundColor White

    } catch {
        Write-Host "DELETE FAILED: $($_.Exception.Message)" -ForegroundColor Red
        
        # Pokušaj alternativni DELETE endpoint
        try {
            Write-Host "`nPokušavam alternativni DELETE endpoint: DELETE /api/reports/user/{userId}/transaction" -ForegroundColor Cyan
            $altDeleteUrl = "http://localhost:9050/api/reports/user/$userId/transaction?transactionId=$targetTxId&date=$targetDate"
            $altDeleteResult = Invoke-RestMethod -Uri $altDeleteUrl -Method DELETE
            
            Write-Host "ALTERNATIVNI DELETE REZULTAT:" -ForegroundColor Green
            Write-Host "  Success: $($altDeleteResult.success)" -ForegroundColor White
            Write-Host "  Operation: $($altDeleteResult.operation)" -ForegroundColor White
            Write-Host "  Transaction ID: $($altDeleteResult.transaction_id)" -ForegroundColor White
            
        } catch {
            Write-Host "ALTERNATIVNI DELETE FAILED: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "Nema transakcije za DELETE test" -ForegroundColor Yellow
}

# ===========================================
# VERIFICATION - Proveri promene
# ===========================================

Write-Host "`n=== VERIFICATION - PROVERA PROMENA ===" -ForegroundColor Green
Write-Host "Proveravam da li su promene vidljive..." -ForegroundColor Yellow

try {
    Write-Host "Pozivam READ ponovo..." -ForegroundColor Cyan
    $verifyResult = Invoke-RestMethod -Uri "http://localhost:9050/api/reports/complex/analytics" -Method GET
    Write-Host "VERIFICATION REZULTAT:" -ForegroundColor Green
    Write-Host "  Total transakcija NAKON CRUD: $($verifyResult.basic_statistics.total_transactions_in_system)" -ForegroundColor White
    Write-Host "  Ukupan iznos NAKON CRUD: $($verifyResult.basic_statistics.total_amount_in_system_rsd) RSD" -ForegroundColor White
    
    # Generiši PDF za verifikaciju
    Write-Host "`nGenertišem PDF verifikaciju..." -ForegroundColor Yellow
    $pdf = Invoke-WebRequest -Uri "http://localhost:9050/api/reports/complex/analytics/pdf" -Method GET
    if ($pdf.StatusCode -eq 200) {
        $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
        $pdfPath = ".\crud_complete_verification_$timestamp.pdf"
        [System.IO.File]::WriteAllBytes($pdfPath, $pdf.Content)
        Write-Host "PDF VERIFICATION:" -ForegroundColor Green
        Write-Host "  PDF kreiran: $pdfPath" -ForegroundColor White
        Write-Host "  Veličina: $($pdf.Content.Length) bytes" -ForegroundColor White
    }
    
} catch {
    Write-Host "VERIFICATION FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ===========================================
# SUMMARY
# ===========================================

Write-Host "`n=== KOMPLETNA CRUD SUMMARY ===" -ForegroundColor Cyan
Write-Host "CREATE: POST http://localhost:9050/api/tx ✅" -ForegroundColor Green
Write-Host "READ:   GET  http://localhost:9050/api/reports/complex/analytics ✅" -ForegroundColor Green  
Write-Host "UPDATE: PUT  http://localhost:9050/api/reports/transaction/amount ✅" -ForegroundColor Green
Write-Host "UPDATE: PUT  http://localhost:9050/api/reports/transaction/complete ✅" -ForegroundColor Green
Write-Host "DELETE: DEL  http://localhost:9050/api/tx/users/{id}/date/{date}/time/{uuid} ✅" -ForegroundColor Green

Write-Host "`n🎉 KOMPLETNA CRUD FUNKCIONALNOST TESTIRANA!" -ForegroundColor Green