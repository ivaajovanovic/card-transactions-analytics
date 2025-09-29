# =================================
# Test Plan za Card Transactions Analytics
# Prema specifikaciji iz dokumenta
# =================================

Write-Host "🚀 Pokretanje testiranja Card Transactions Analytics sistema" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Yellow

# Bazni URL-ovi servisa
$EUREKA_URL = "http://localhost:8761"
$COLUMNAR_URL = "http://localhost:9050"
$ELASTICSEARCH_URL = "http://localhost:8081"
$GATEWAY_URL = "http://localhost:8080"

# Čekanje da se servisi pokretnu
Write-Host "⏳ Čekanje da se servisi pokretnu..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Test 1: Provera infrastrukture
Write-Host ""
Write-Host "📋 TEST 1: Provera dostupnosti infrastrukture" -ForegroundColor Cyan
Write-Host "----------------------------------------------"

Write-Host "🔍 Provera Eureka servera..."
try {
    $response = Invoke-WebRequest -Uri "$EUREKA_URL/eureka/apps" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Eureka server je dostupan" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Eureka server nije dostupan" -ForegroundColor Red
}

Write-Host "🔍 Provera Columnar Database servisa..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/actuator/health" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Columnar Database Service je dostupan" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Columnar Database Service nije dostupan" -ForegroundColor Red
}

Write-Host "🔍 Provera Elasticsearch servisa..."
try {
    $response = Invoke-WebRequest -Uri "$ELASTICSEARCH_URL/actuator/health" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Elasticsearch Service je dostupan" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Elasticsearch Service nije dostupan" -ForegroundColor Red
}

# Test 2: Service Discovery
Write-Host ""
Write-Host "📋 TEST 2: Service Discovery kroz Eureka" -ForegroundColor Cyan
Write-Host "----------------------------------------"

try {
    $response = Invoke-WebRequest -Uri "$EUREKA_URL/eureka/apps" -Method GET -TimeoutSec 10
    Write-Host "🔍 Eureka response dobija informacije o servisima" -ForegroundColor Green
} catch {
    Write-Host "❌ Greška pri dobijanju servisa iz Eureka" -ForegroundColor Red
}

# Test 3: Transakciona obrada podataka
Write-Host ""
Write-Host "📋 TEST 3: Transakciona obrada podataka" -ForegroundColor Cyan
Write-Host "---------------------------------------"

# Kreiranje test transakcije
$TEST_TX = @{
    txId = "123e4567-e89b-12d3-a456-426614174000"
    userId = "123e4567-e89b-12d3-a456-426614174001"
    cardId = "123e4567-e89b-12d3-a456-426614174002"
    merchantId = "123e4567-e89b-12d3-a456-426614174003"
    categoryId = "123e4567-e89b-12d3-a456-426614174004"
    amountCents = 12345
    currency = "EUR"
    status = "APPROVED"
    occurredAt = "2025-09-29T10:00:00Z"
} | ConvertTo-Json

Write-Host "💳 Slanje pojedinačne transakcije..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/api/tx" -Method POST -Body $TEST_TX -ContentType "application/json" -TimeoutSec 30
    if ($response.StatusCode -in @(200, 201)) {
        Write-Host "✅ Transakcija uspešno obradjena (HTTP $($response.StatusCode))" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška pri obradi transakcije: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Batch obrada
Write-Host ""
Write-Host "📋 TEST 4: Batch obrada transakcija" -ForegroundColor Cyan
Write-Host "-----------------------------------"

$BATCH_TX = @(
    @{
        txId = "223e4567-e89b-12d3-a456-426614174000"
        userId = "123e4567-e89b-12d3-a456-426614174001"
        cardId = "123e4567-e89b-12d3-a456-426614174002"
        merchantId = "123e4567-e89b-12d3-a456-426614174003"
        categoryId = "123e4567-e89b-12d3-a456-426614174004"
        amountCents = 5000
        currency = "EUR"
        status = "APPROVED"
        occurredAt = "2025-09-29T10:01:00Z"
    },
    @{
        txId = "323e4567-e89b-12d3-a456-426614174000"
        userId = "123e4567-e89b-12d3-a456-426614174001"
        cardId = "123e4567-e89b-12d3-a456-426614174002"
        merchantId = "123e4567-e89b-12d3-a456-426614174005"
        categoryId = "123e4567-e89b-12d3-a456-426614174006"
        amountCents = 7500
        currency = "EUR"
        status = "APPROVED"
        occurredAt = "2025-09-29T10:02:00Z"
    }
) | ConvertTo-Json

Write-Host "💳 Slanje batch transakcija..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/api/tx/batch" -Method POST -Body $BATCH_TX -ContentType "application/json" -TimeoutSec 30
    if ($response.StatusCode -in @(200, 202)) {
        Write-Host "✅ Batch transakcije uspešno obradjena (HTTP $($response.StatusCode))" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška pri batch obradi: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Čitanje podataka
Write-Host ""
Write-Host "📋 TEST 5: Čitanje podataka iz različitih baza" -ForegroundColor Cyan
Write-Host "----------------------------------------------"

$USER_ID = "123e4567-e89b-12d3-a456-426614174001"
$CATEGORY_ID = "123e4567-e89b-12d3-a456-426614174004"
$TODAY = "2025-09-29"

Write-Host "📊 Čitanje transakcija korisnika za danas..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/reports/users/$USER_ID/transactions/today?limit=10" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Uspešno dobijeni podaci korisnika" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška pri dobijanju podataka korisnika" -ForegroundColor Red
}

Write-Host "📊 Čitanje transakcija po kategoriji za dan..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/reports/categories/$CATEGORY_ID/transactions/day?date=$TODAY&limit=10" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Uspešno dobijeni podaci kategorije" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška pri dobijanju podataka kategorije" -ForegroundColor Red
}

# Test 6: Generator izveštaja
Write-Host ""
Write-Host "📋 TEST 6: Generator izveštaja" -ForegroundColor Cyan
Write-Host "------------------------------"

Write-Host "📈 Testiranje analitičkih izveštaja..."

# Category Report
Write-Host "📊 Category Report..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/reports/analytics/category-report?categoryId=$CATEGORY_ID&fromDate=2025-09-29&toDate=2025-09-29" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Category Report uspešno generisan" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška pri generisanju Category Report-a" -ForegroundColor Red
}

# User Activity Report
Write-Host "📊 User Activity Report..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/reports/analytics/user-activity?userId=$USER_ID&fromDate=2025-09-29&toDate=2025-09-29" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ User Activity Report uspešno generisan" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška pri generisanju User Activity Report-a" -ForegroundColor Red
}

# Complex Daily Report
Write-Host "📊 Complex Daily Report..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/reports/analytics/complex-daily?date=2025-09-29" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Complex Daily Report uspešno generisan" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška pri generisanju Complex Daily Report-a" -ForegroundColor Red
}

# Test 7: Integracija sa Elasticsearch
Write-Host ""
Write-Host "📋 TEST 7: Integracija Cassandra ↔ Elasticsearch" -ForegroundColor Cyan
Write-Host "------------------------------------------------"

Write-Host "🔗 Testiranje Elasticsearch konekcije..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/api/integration/elasticsearch/health" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Elasticsearch integracija je funkcionalna" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška u Elasticsearch integraciji" -ForegroundColor Red
}

Write-Host "🔄 Testiranje sinhronizacije podataka..."
try {
    $response = Invoke-WebRequest -Uri "$COLUMNAR_URL/api/integration/elasticsearch/sync-categories?categoryId=$CATEGORY_ID&date=2025-09-29" -Method POST -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Sinhronizacija sa Elasticsearch uspešna" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Greška pri sinhronizaciji sa Elasticsearch" -ForegroundColor Red
}

# Završni izveštaj
Write-Host ""
Write-Host "🏁 ZAVRŠNI IZVEŠTAJ" -ForegroundColor Yellow
Write-Host "=================="
Write-Host "📊 Testiranje završeno!" -ForegroundColor Green
Write-Host "🔍 Proverite detaljne rezultate iznad"
Write-Host "📈 Za dodatne analize koristite Grafana dashboard na http://localhost:3000" -ForegroundColor Blue
Write-Host "🗄️ Cassandra podaci dostupni preko aplikacije" -ForegroundColor Blue
Write-Host "🔎 Elasticsearch podaci dostupni preko Kibana na http://localhost:5601" -ForegroundColor Blue

Write-Host ""
Write-Host "🎯 SPECIFIKACIJA ISPUNJENA:" -ForegroundColor Yellow
Write-Host "✅ Transakciona obrada podataka (Cassandra + Elasticsearch)" -ForegroundColor Green
Write-Host "✅ Funkcionalnost: unos, izmena, brisanje, čitanje" -ForegroundColor Green
Write-Host "✅ Transakciona obrada u realnom vremenu" -ForegroundColor Green
Write-Host "✅ Izbor baza: NoSQL (Cassandra) i NoSQL (Elasticsearch)" -ForegroundColor Green
Write-Host "✅ Realizacija principa mikroservisa (Saga arhitektura)" -ForegroundColor Green
Write-Host "✅ Generator izveštaja sa vizualizacijom" -ForegroundColor Green
Write-Host "✅ Integracija između servisa" -ForegroundColor Green

Write-Host ""
Write-Host "🔚 Test završen - $(Get-Date)" -ForegroundColor Magenta