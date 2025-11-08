#!/bin/bash

# ElasticSearch Database Service Test Script
# Run these commands to test your ElasticSearch service

BASE_URL="http://localhost:9080"
ES_URL="http://localhost:9200"

echo "🚀 Testing ElasticSearch Database Service"
echo "================================================"

echo ""
echo "📊 1. GENERATING SAMPLE DATA"
echo "----------------------------"

echo "Generating sample data (1000 transactions + 100 user activities)..."
curl -X POST "$BASE_URL/api/data-generator/sample-data" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

sleep 2

echo ""
echo "📈 2. CHECKING DATA COUNTS"
echo "-------------------------"

echo "Total transactions:"
curl -s "$BASE_URL/api/transactions/count"
echo ""

echo "Total user activities:"
curl -s "$BASE_URL/api/user-activities/count"
echo ""

echo ""
echo "🔍 3. TESTING TRANSACTION ENDPOINTS"
echo "----------------------------------"

echo "Getting first 5 transactions:"
curl -s "$BASE_URL/api/transactions" | head -c 500
echo "...(truncated)"
echo ""

echo "Searching for transactions with 'coffee' in description:"
curl -s "$BASE_URL/api/transactions/search?description=coffee"
echo ""

echo "Getting transactions in amount range 50-200:"
curl -s "$BASE_URL/api/transactions/amount-range?minAmount=50&maxAmount=200" | head -c 300
echo "...(truncated)"
echo ""

echo "Getting top transactions by amount:"
curl -s "$BASE_URL/api/transactions/top-amounts" | head -c 400
echo "...(truncated)"
echo ""

echo ""
echo "👥 4. TESTING USER ACTIVITY ENDPOINTS" 
echo "------------------------------------"

echo "Getting top spenders:"
curl -s "$BASE_URL/api/user-activities/top-spenders" | head -c 400
echo "...(truncated)"
echo ""

echo "Getting most active users:"
curl -s "$BASE_URL/api/user-activities/most-active" | head -c 400
echo "...(truncated)"
echo ""

echo "Getting users with spending range 100-500:"
curl -s "$BASE_URL/api/user-activities/spending-range?minSpent=100&maxSpent=500" | head -c 300
echo "...(truncated)"
echo ""

echo ""
echo "🏥 5. HEALTH CHECKS"
echo "------------------"

echo "ElasticSearch service health:"
curl -s "$BASE_URL/actuator/health" 2>/dev/null || echo "Health endpoint not available"
echo ""

echo "ElasticSearch cluster health:"
curl -s "$ES_URL/_cluster/health?pretty"
echo ""

echo "ElasticSearch indices:"
curl -s "$ES_URL/_cat/indices?v"
echo ""

echo ""
echo "➕ 6. TESTING CREATE OPERATIONS"
echo "------------------------------"

echo "Creating a custom transaction:"
curl -X POST "$BASE_URL/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "cardId": "TEST-CARD-001",
    "merchantId": "TEST-MERCHANT-001",
    "amount": 123.45,
    "date": "2024-01-20T14:30:00",
    "description": "Test Coffee Shop Purchase"
  }' \
  -w "\nStatus: %{http_code}\n"

echo ""
echo "Creating a custom user activity:"
curl -X POST "$BASE_URL/api/user-activities" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "TEST-USER-001",
    "txCount": 5,
    "lastTxDate": "2024-01-20T14:30:00",
    "totalSpent": 345.67
  }' \
  -w "\nStatus: %{http_code}\n"

echo ""
echo "Updated transaction count:"
curl -s "$BASE_URL/api/transactions/count"
echo ""

echo "Updated user activity count:"
curl -s "$BASE_URL/api/user-activities/count"
echo ""

echo ""
echo "🔍 7. TESTING SEARCH WITH CUSTOM DATA"
echo "------------------------------------"

echo "Searching for 'Test Coffee' transactions:"
curl -s "$BASE_URL/api/transactions/search?description=Test Coffee"
echo ""

echo "Getting transactions for TEST-CARD-001:"
curl -s "$BASE_URL/api/transactions/card/TEST-CARD-001"
echo ""

echo ""
echo "✅ Testing completed!"
echo "==================="
echo ""
echo "📝 Available endpoints summary:"
echo "- Transactions: $BASE_URL/api/transactions/*"
echo "- User Activities: $BASE_URL/api/user-activities/*" 
echo "- Data Generator: $BASE_URL/api/data-generator/*"
echo "- ElasticSearch: $ES_URL/*"
echo "- Eureka: http://localhost:8761/"
echo ""
echo "For more detailed testing, use the elasticsearch-test-requests.http file"
echo "with VS Code REST Client extension or import into Postman."