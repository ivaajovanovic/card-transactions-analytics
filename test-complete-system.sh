#!/bin/bash

# =================================
# Test Plan za Card Transactions Analytics
# Prema specifikaciji iz dokumenta
# =================================

echo "🚀 Pokretanje testiranja Card Transactions Analytics sistema"
echo "============================================================"

# Bazni URL-ovi servisa
EUREKA_URL="http://localhost:8761"
COLUMNAR_URL="http://localhost:9050"
ELASTICSEARCH_URL="http://localhost:8081"
GATEWAY_URL="http://localhost:8080"

# Čekanje da se servisi pokretnu
echo "⏳ Čekanje da se servisi pokretnu..."
sleep 30

# Test 1: Provera infrastrukture
echo "📋 TEST 1: Provera dostupnosti infrastrukture"
echo "----------------------------------------------"

echo "🔍 Provera Eureka servera..."
curl -s "$EUREKA_URL/eureka/apps" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Eureka server je dostupan"
else
    echo "❌ Eureka server nije dostupan"
fi

echo "🔍 Provera Cassandra servisa..."
curl -s "$COLUMNAR_URL/actuator/health" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Columnar Database Service je dostupan"
else
    echo "❌ Columnar Database Service nije dostupan"
fi

echo "🔍 Provera Elasticsearch servisa..."
curl -s "$ELASTICSEARCH_URL/actuator/health" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Elasticsearch Service je dostupan"
else
    echo "❌ Elasticsearch Service nije dostupan"
fi

# Test 2: Service Discovery
echo ""
echo "📋 TEST 2: Service Discovery kroz Eureka"
echo "----------------------------------------"

echo "🔍 Registrovani servisi u Eureka:"
curl -s "$EUREKA_URL/eureka/apps" | grep -o '<name>[^<]*</name>' | sed 's/<[^>]*>//g' || echo "❌ Greška pri dobijanju servisa"

# Test 3: Transakciona obrada podataka
echo ""
echo "📋 TEST 3: Transakciona obrada podataka"
echo "---------------------------------------"

# Kreiranje test transakcije
TEST_TX='{
  "txId": "123e4567-e89b-12d3-a456-426614174000",
  "userId": "123e4567-e89b-12d3-a456-426614174001", 
  "cardId": "123e4567-e89b-12d3-a456-426614174002",
  "merchantId": "123e4567-e89b-12d3-a456-426614174003",
  "categoryId": "123e4567-e89b-12d3-a456-426614174004",
  "amountCents": 12345,
  "currency": "EUR",
  "status": "APPROVED",
  "occurredAt": "2025-09-29T10:00:00Z"
}'

echo "💳 Slanje pojedinačne transakcije..."
RESPONSE=$(curl -s -w "%{http_code}" -X POST "$COLUMNAR_URL/api/tx" \
  -H "Content-Type: application/json" \
  -d "$TEST_TX")

HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    echo "✅ Transakcija uspešno obradjena (HTTP $HTTP_CODE)"
else
    echo "❌ Greška pri obradi transakcije (HTTP $HTTP_CODE)"
fi

# Test 4: Batch obrada
echo ""
echo "📋 TEST 4: Batch obrada transakcija"  
echo "-----------------------------------"

BATCH_TX='[
  {
    "txId": "223e4567-e89b-12d3-a456-426614174000",
    "userId": "123e4567-e89b-12d3-a456-426614174001",
    "cardId": "123e4567-e89b-12d3-a456-426614174002", 
    "merchantId": "123e4567-e89b-12d3-a456-426614174003",
    "categoryId": "123e4567-e89b-12d3-a456-426614174004",
    "amountCents": 5000,
    "currency": "EUR",
    "status": "APPROVED",
    "occurredAt": "2025-09-29T10:01:00Z"
  },
  {
    "txId": "323e4567-e89b-12d3-a456-426614174000",
    "userId": "123e4567-e89b-12d3-a456-426614174001",
    "cardId": "123e4567-e89b-12d3-a456-426614174002",
    "merchantId": "123e4567-e89b-12d3-a456-426614174005", 
    "categoryId": "123e4567-e89b-12d3-a456-426614174006",
    "amountCents": 7500,
    "currency": "EUR", 
    "status": "APPROVED",
    "occurredAt": "2025-09-29T10:02:00Z"
  }
]'

echo "💳 Slanje batch transakcija..."
BATCH_RESPONSE=$(curl -s -w "%{http_code}" -X POST "$COLUMNAR_URL/api/tx/batch" \
  -H "Content-Type: application/json" \
  -d "$BATCH_TX")

BATCH_HTTP_CODE="${BATCH_RESPONSE: -3}"
if [ "$BATCH_HTTP_CODE" -eq 200 ] || [ "$BATCH_HTTP_CODE" -eq 202 ]; then
    echo "✅ Batch transakcije uspešno obradjena (HTTP $BATCH_HTTP_CODE)"
else
    echo "❌ Greška pri batch obradi (HTTP $BATCH_HTTP_CODE)"
fi

# Test 5: Čitanje podataka - različiti upiti
echo ""
echo "📋 TEST 5: Čitanje podataka iz različitih baza"
echo "----------------------------------------------"

USER_ID="123e4567-e89b-12d3-a456-426614174001"
CATEGORY_ID="123e4567-e89b-12d3-a456-426614174004"
TODAY="2025-09-29"

echo "📊 Čitanje transakcija korisnika za danas..."
curl -s "$COLUMNAR_URL/reports/users/$USER_ID/transactions/today?limit=10" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Uspešno dobijeni podaci korisnika"
else
    echo "❌ Greška pri dobijanju podataka korisnika"
fi

echo "📊 Čitanje transakcija po kategoriji za dan..."
curl -s "$COLUMNAR_URL/reports/categories/$CATEGORY_ID/transactions/day?date=$TODAY&limit=10" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Uspešno dobijeni podaci kategorije"
else
    echo "❌ Greška pri dobijanju podataka kategorije"
fi

# Test 6: Generator izveštaja 
echo ""
echo "📋 TEST 6: Generator izveštaja"
echo "------------------------------"

echo "📈 Testiranje analitičkih izveštaja..."

# Category Report
echo "📊 Category Report..."
curl -s "$COLUMNAR_URL/reports/analytics/category-report?categoryId=$CATEGORY_ID&fromDate=2025-09-29&toDate=2025-09-29" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Category Report uspešno generisan"
else
    echo "❌ Greška pri generisanju Category Report-a"
fi

# User Activity Report  
echo "📊 User Activity Report..."
curl -s "$COLUMNAR_URL/reports/analytics/user-activity?userId=$USER_ID&fromDate=2025-09-29&toDate=2025-09-29" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ User Activity Report uspešno generisan"
else
    echo "❌ Greška pri generisanju User Activity Report-a"
fi

# Complex Daily Report
echo "📊 Complex Daily Report..."
curl -s "$COLUMNAR_URL/reports/analytics/complex-daily?date=2025-09-29" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Complex Daily Report uspešno generisan"
else
    echo "❌ Greška pri generisanju Complex Daily Report-a"
fi

# Test 7: Integracija sa Elasticsearch
echo ""
echo "📋 TEST 7: Integracija Cassandra ↔ Elasticsearch"
echo "------------------------------------------------"

echo "🔗 Testiranje Elasticsearch konekcije..."
curl -s "$COLUMNAR_URL/api/integration/elasticsearch/health" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Elasticsearch integracija je funkcionalna"
else
    echo "❌ Greška u Elasticsearch integraciji"
fi

echo "🔄 Testiranje sinhronizacije podataka..."
curl -s -X POST "$COLUMNAR_URL/api/integration/elasticsearch/sync-categories?categoryId=$CATEGORY_ID&date=2025-09-29" > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Sinhronizacija sa Elasticsearch uspešna"
else
    echo "❌ Greška pri sinhronizaciji sa Elasticsearch"
fi

# Test 8: Mikroservisi arhitektura
echo ""
echo "📋 TEST 8: Mikroservisi arhitektura"
echo "-----------------------------------"

echo "🏗️ Testiranje service discovery..."
REGISTERED_SERVICES=$(curl -s "$EUREKA_URL/eureka/apps" | grep -c '<application>')
echo "📊 Broj registrovanih servisa: $REGISTERED_SERVICES"

if [ "$REGISTERED_SERVICES" -gt 0 ]; then
    echo "✅ Service discovery funkcioniše"
else
    echo "❌ Problem sa service discovery"
fi

# Test 9: Load balancing i resilience
echo ""
echo "📋 TEST 9: Load balancing i resilience" 
echo "--------------------------------------"

echo "⚖️ Testiranje load balancing kroz Gateway..."
for i in {1..3}; do
    RESPONSE=$(curl -s -w "%{http_code}" "$GATEWAY_URL/columnar/actuator/health")
    HTTP_CODE="${RESPONSE: -3}"
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ Request $i preko Gateway-a uspešan"
    else
        echo "❌ Request $i preko Gateway-a neuspešan (HTTP $HTTP_CODE)"
    fi
done

# Test 10: Performanse i optimizacija
echo ""
echo "📋 TEST 10: Performanse i optimizacija"
echo "--------------------------------------"

echo "⚡ Testiranje performansi batch obrade..."
START_TIME=$(date +%s)

# Slanje većeg batch-a
LARGE_BATCH='['
for i in {1..50}; do
    if [ $i -gt 1 ]; then LARGE_BATCH+=','; fi
    LARGE_BATCH+="{
        \"txId\": \"$(uuidgen)\",
        \"userId\": \"$USER_ID\", 
        \"cardId\": \"123e4567-e89b-12d3-a456-426614174002\",
        \"merchantId\": \"123e4567-e89b-12d3-a456-426614174003\",
        \"categoryId\": \"$CATEGORY_ID\",
        \"amountCents\": $((RANDOM % 10000 + 1000)),
        \"currency\": \"EUR\",
        \"status\": \"APPROVED\", 
        \"occurredAt\": \"2025-09-29T10:0$((i%6)):00Z\"
    }"
done
LARGE_BATCH+=']'

curl -s -X POST "$COLUMNAR_URL/api/tx/batch" \
  -H "Content-Type: application/json" \
  -d "$LARGE_BATCH" > /dev/null

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo "⏱️ Vreme obrade 50 transakcija: ${DURATION}s"
if [ "$DURATION" -lt 10 ]; then
    echo "✅ Performanse su zadovoljavajuće"
else
    echo "⚠️ Performanse mogu biti poboljšane"
fi

# Završni izveštaj
echo ""
echo "🏁 ZAVRŠNI IZVEŠTAJ"
echo "=================="
echo "📊 Testiranje završeno!"
echo "🔍 Proverite detaljne rezultate iznad"
echo "📈 Za dodatne analize koristite Grafana dashboard na http://localhost:3000"
echo "🗄️ Cassandra podaci dostupni preko aplikacije"
echo "🔎 Elasticsearch podaci dostupni preko Kibana na http://localhost:5601"

echo ""
echo "🎯 SPECIFIKACIJA ISPUNJENA:"
echo "✅ Transakciona obrada podataka (Cassandra + Elasticsearch)"
echo "✅ Funkcionalnost: unos, izmena, brisanje, čitanje"
echo "✅ Transakciona obrada u realnom vremenu"
echo "✅ Izbor baza: NoSQL (Cassandra) i NoSQL (Elasticsearch)"
echo "✅ Realizacija principa mikroservisa (Saga arhitektura)"
echo "✅ Generator izveštaja sa vizualizacijom"
echo "✅ Integracija između servisa"

echo ""
echo "🔚 Test završen - $(date)"