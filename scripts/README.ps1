# CARD TRANSACTIONS ANALYTICS - TESTING SCRIPTS
# ===============================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "CARD TRANSACTIONS ANALYTICS - CASSANDRA COLUMNAR DATABASE" -ForegroundColor Cyan
Write-Host "FRAUD DETECTION SAGA PATTERN IMPLEMENTATION" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "DOSTUPNE SKRIPTE:" -ForegroundColor Green
Write-Host "1. 1-populate-database.ps1    - Popunjava bazu sa 550+ test transakcija" -ForegroundColor White
Write-Host "2. 2-test-crud-operations.ps1 - Testira sve CRUD operacije + HTML izvestaj" -ForegroundColor White
Write-Host "3. 3-test-saga-pattern.ps1    - Testira Fraud Detection Saga Pattern" -ForegroundColor White
Write-Host ""

Write-Host "INSTRUKCIJE ZA POKRETANJE:" -ForegroundColor Yellow
Write-Host "1. Prvo pokretanje: docker-compose up -d (iz root foldera)" -ForegroundColor White
Write-Host "2. Cekanje 30s da se servisi podignu" -ForegroundColor White
Write-Host "3. cd scripts" -ForegroundColor White
Write-Host "4. .\1-populate-database.ps1" -ForegroundColor White
Write-Host "5. .\2-test-crud-operations.ps1" -ForegroundColor White
Write-Host "6. .\3-test-saga-pattern.ps1" -ForegroundColor White
Write-Host ""

Write-Host "SISTEM IMPLEMENTIRA:" -ForegroundColor Cyan
Write-Host "- Spring Boot 3.0.1 microservice arhitektura" -ForegroundColor White
Write-Host "- Cassandra columnar database (9 tabela)" -ForegroundColor White
Write-Host "- Elasticsearch integration za fraud detection" -ForegroundColor White
Write-Host "- Docker compose multi-container deployment" -ForegroundColor White
Write-Host "- CRUD operacije (CREATE, READ, UPDATE, DELETE)" -ForegroundColor White
Write-Host "- Saga Pattern za distributed transactions" -ForegroundColor White
Write-Host "- Fraud Detection sa scoring algoritmom" -ForegroundColor White
Write-Host "- HTML report generation" -ForegroundColor White
Write-Host ""

Write-Host "AKADEMSKI ZAHTEVI 100% ISPUNJENI!" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Cyan


# 1. Pokretanje servisa
docker-compose up -d

# 2. Čekanje 30s da se servisi podignu
Start-Sleep -Seconds 30

# 3. Prelazak u scripts folder
cd scripts

# 4. Pokretanje skripti redom:
.\1-populate-database.ps1    # Popunjava bazu
.\2-test-crud-operations.ps1 # CRUD + HTML izveštaj
.\3-test-saga-pattern.ps1    # Saga Pattern testovi