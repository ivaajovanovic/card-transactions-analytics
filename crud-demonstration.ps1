# CRUD DEMONSTRATION SKRIPTA - Detaljno testiranje

Write-Host "=== CRUD OPERACIJE DEMONSTRATION ===" -ForegroundColor Cyan
Write-Host "Testiram svaku CRUD operaciju sa jasnim prikazom rezultata`n" -ForegroundColor Yellow

# ===========================================
# C - CREATE (TxIngestService)
# ===========================================

Write-Host "=== C - CREATE OPERACIJA ===" -ForegroundColor Green
Write-Host "Dodajem novu test transakciju..." -ForegroundColor Yellow

$newTransaction = @{
    userId = "550e8400-e29b-41d4-a716-446655440001"
    amount = 1234.56
    merchantId = "CRUD_TEST_MERCHANT"
    category = "ELECTRONICS"
    description = "CRUD Test Transaction"
    currency = "RSD"
} | ConvertTo-Json

try {
    Write-Host "Pozivam CREATE endpoint: POST /api/ingest/transaction" -ForegroundColor Cyan
    $createResult = Invoke-RestMethod -Uri "http://localhost:8080/api/ingest/transaction" -Method POST -Body $newTransaction -ContentType "application/json"
    Write-Host "CREATE REZULTAT:" -ForegroundColor Green
    Write-Host "  Status: SUCCESS" -ForegroundColor Green
    Write-Host "  Transaction ID: $($createResult.transactionId)" -ForegroundColor White
    Write-Host "  Amount: $($createResult.amount) $($createResult.currency)" -ForegroundColor White
    $createdTxId = $createResult.transactionId
} catch {
    Write-Host "CREATE FAILED: $($_.Exception.Message)" -ForegroundColor Red
    $createdTxId = $null
}

Start-Sleep -Seconds 2

# ===========================================
# R - READ (QueryService Analytics)
# ===========================================

Write-Host "`n=== R - READ OPERACIJA ===" -ForegroundColor Green
Write-Host "Čitam podatke iz baze..." -ForegroundColor Yellow

try {
    Write-Host "Pozivam READ endpoint: GET /api/reports/complex/analytics" -ForegroundColor Cyan
    $readResult = Invoke-RestMethod -Uri "http://localhost:9050/api/reports/complex/analytics" -Method GET
    Write-Host "READ REZULTAT:" -ForegroundColor Green
    Write-Host "  Total transakcija u sistemu: $($readResult.basic_statistics.total_transactions_in_system)" -ForegroundColor White
    Write-Host "  Ukupan iznos: $($readResult.basic_statistics.total_amount_in_system_rsd) RSD" -ForegroundColor White
    Write-Host "  Aktivni korisnici: $($readResult.basic_statistics.unique_users_total)" -ForegroundColor White
    Write-Host "  Period analiza - transakcije: $($readResult.current_period.total_transactions_analyzed)" -ForegroundColor White
    Write-Host "  Period analiza - iznos: $($readResult.current_period.total_amount_analyzed_rsd) RSD" -ForegroundColor White
} catch {
    Write-Host "READ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# ===========================================
# U - UPDATE (QueryService UPSERT)
# ===========================================

Write-Host "`n=== U - UPDATE OPERACIJA ===" -ForegroundColor Green
Write-Host "Ažuriram postojeću transakciju..." -ForegroundColor Yellow

try {
    # UPDATE Amount test
    $userId = "550e8400-e29b-41d4-a716-446655440001"
    $updateTxId = "550e8400-e29b-41d4-a716-446655442001"
    $newAmount = 2999.99
    
    Write-Host "Pozivam UPDATE endpoint: PUT /api/reports/transaction/amount" -ForegroundColor Cyan
    $updateUrl = "http://localhost:9050/api/reports/transaction/amount?userId=$userId&date=2024-01-15&transactionId=$updateTxId&amount=$newAmount&currency=RSD"
    $updateResult = Invoke-RestMethod -Uri $updateUrl -Method PUT
    
    Write-Host "UPDATE AMOUNT REZULTAT:" -ForegroundColor Green
    Write-Host "  Success: $($updateResult.success)" -ForegroundColor White
    Write-Host "  Operation: $($updateResult.operation)" -ForegroundColor White
    Write-Host "  Transaction ID: $($updateResult.transaction_id)" -ForegroundColor White
    Write-Host "  New Amount: $($updateResult.new_amount)" -ForegroundColor White
    Write-Host "  Currency: $($updateResult.currency)" -ForegroundColor White
    Write-Host "  Timestamp: $($updateResult.timestamp)" -ForegroundColor White

    Start-Sleep -Seconds 1

    # UPDATE Complete transaction test
    Write-Host "`nTestiram COMPLETE UPDATE (UPSERT)..." -ForegroundColor Yellow
    $completeTxId = "550e8400-e29b-41d4-a716-446655442002"
    $merchantId = "550e8400-e29b-41d4-a716-446655440101"
    $categoryId = "550e8400-e29b-41d4-a716-446655440201"
    
    Write-Host "Pozivam COMPLETE UPDATE endpoint: PUT /api/reports/transaction/complete" -ForegroundColor Cyan
    $completeUrl = "http://localhost:9050/api/reports/transaction/complete?transactionId=$completeTxId&userId=$userId&merchantId=$merchantId&categoryId=$categoryId&date=2024-01-15&amount=1599.99&currency=RSD&status=COMPLETED&description=CRUD-UPDATE-TEST"
    $completeResult = Invoke-RestMethod -Uri $completeUrl -Method PUT
    
    Write-Host "UPDATE COMPLETE REZULTAT:" -ForegroundColor Green
    Write-Host "  Success: $($completeResult.success)" -ForegroundColor White
    Write-Host "  Operation: $($completeResult.operation)" -ForegroundColor White
    Write-Host "  Transaction ID: $($completeResult.transaction_id)" -ForegroundColor White
    Write-Host "  New Amount: $($completeResult.new_amount)" -ForegroundColor White
    Write-Host "  New Status: $($completeResult.new_status)" -ForegroundColor White
    Write-Host "  Description: $($completeResult.new_description)" -ForegroundColor White

} catch {
    Write-Host "UPDATE FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# ===========================================
# D - DELETE (QueryService DELETE)
# ===========================================

Write-Host "`n=== D - DELETE OPERACIJA ===" -ForegroundColor Green
Write-Host "Brišem test transakciju..." -ForegroundColor Yellow

try {
    # DELETE User transaction
    $deleteTxId = "550e8400-e29b-41d4-a716-446655442001"
    
    Write-Host "Pozivam DELETE endpoint: DELETE /api/reports/user/{userId}/transaction" -ForegroundColor Cyan
    $deleteUrl = "http://localhost:9050/api/reports/user/$userId/transaction?transactionId=$deleteTxId&date=2024-01-15"
    $deleteResult = Invoke-RestMethod -Uri $deleteUrl -Method DELETE
    
    Write-Host "DELETE REZULTAT:" -ForegroundColor Green
    Write-Host "  Success: $($deleteResult.success)" -ForegroundColor White
    Write-Host "  Operation: $($deleteResult.operation)" -ForegroundColor White
    Write-Host "  User ID: $($deleteResult.user_id)" -ForegroundColor White
    Write-Host "  Transaction ID: $($deleteResult.transaction_id)" -ForegroundColor White
    Write-Host "  Transaction Date: $($deleteResult.transaction_date)" -ForegroundColor White
    Write-Host "  Timestamp: $($deleteResult.timestamp)" -ForegroundColor White

} catch {
    Write-Host "DELETE FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# ===========================================
# VERIFICATION - Proveri promene
# ===========================================

Write-Host "`n=== VERIFICATION - PROVERA PROMENA ===" -ForegroundColor Green
Write-Host "Proveravam da li su promene vidljive u sistemu..." -ForegroundColor Yellow

try {
    Write-Host "Pozivam READ ponovo da vidim promene..." -ForegroundColor Cyan
    $verifyResult = Invoke-RestMethod -Uri "http://localhost:9050/api/reports/complex/analytics" -Method GET
    Write-Host "VERIFICATION REZULTAT:" -ForegroundColor Green
    Write-Host "  Total transakcija NAKON CRUD operacija: $($verifyResult.basic_statistics.total_transactions_in_system)" -ForegroundColor White
    Write-Host "  Ukupan iznos NAKON CRUD operacija: $($verifyResult.basic_statistics.total_amount_in_system_rsd) RSD" -ForegroundColor White
    
    # Generiši PDF da vidiš podatke
    Write-Host "`nGenertišem PDF izveštaj sa ažuriranim podacima..." -ForegroundColor Yellow
    $pdf = Invoke-WebRequest -Uri "http://localhost:9050/api/reports/complex/analytics/pdf" -Method GET
    if ($pdf.StatusCode -eq 200) {
        $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
        $pdfPath = ".\crud_verification_$timestamp.pdf"
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

Write-Host "`n=== CRUD OPERATIONS SUMMARY ===" -ForegroundColor Cyan
Write-Host "CREATE: Dodavanje novih transakcija ✅" -ForegroundColor Green
Write-Host "READ:   Čitanje i analiza podataka ✅" -ForegroundColor Green  
Write-Host "UPDATE: Ažuriranje postojećih zapisa ✅" -ForegroundColor Green
Write-Host "DELETE: Brisanje postojećih zapisa ✅" -ForegroundColor Green

Write-Host "`nSvi CRUD endpoint-i:" -ForegroundColor Yellow
Write-Host "  CREATE: POST http://localhost:8080/api/ingest/transaction" -ForegroundColor White
Write-Host "  READ:   GET  http://localhost:9050/api/reports/complex/analytics" -ForegroundColor White
Write-Host "  UPDATE: PUT  http://localhost:9050/api/reports/transaction/amount" -ForegroundColor White
Write-Host "  UPDATE: PUT  http://localhost:9050/api/reports/transaction/complete" -ForegroundColor White
Write-Host "  DELETE: DEL  http://localhost:9050/api/reports/user/{id}/transaction" -ForegroundColor White

Write-Host "`nCRUD DEMONSTRATION ZAVRŠENA!" -ForegroundColor Green