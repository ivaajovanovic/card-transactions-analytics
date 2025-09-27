# Transaction Search Microservice (Elasticsearch)

This microservice provides full-text search and filtering capabilities for transactions using Elasticsearch as the search database.

## Features

- **Transaction Management**: CRUD operations for transactions
- **User Activity Tracking**: Store and query user activity summaries
- **Full-text Search**: Search transactions by description using Elasticsearch's standard analyzer
- **Advanced Filtering**: Filter by amount ranges, date ranges, card ID, merchant ID
- **Analytics**: Top spenders, most active users, transaction counts
- **Pagination Support**: Handle large datasets efficiently

## Elasticsearch Indices

### 1. Transactions Index (`transactions`)
- **id**: Keyword (unique identifier)
- **cardId**: Keyword (card identifier)
- **merchantId**: Keyword (merchant identifier)
- **amount**: Double (transaction amount)
- **date**: Date (ISO format)
- **description**: Text with standard analyzer for full-text search

Example document:
```json
{
  "id": "tx1",
  "cardId": "c1",
  "merchantId": "m1",
  "amount": 45.99,
  "date": "2025-09-01T10:00:00Z",
  "description": "Grocery purchase at SuperMart"
}
```

### 2. User Activity Index (`user_activity`)
- **userId**: Keyword (unique user identifier)
- **txCount**: Integer (total transaction count)
- **lastTxDate**: Date (last transaction date)
- **totalSpent**: Double (total amount spent)

Example document:
```json
{
  "userId": "u1",
  "txCount": 50,
  "lastTxDate": "2025-09-25T12:00:00Z",
  "totalSpent": 1234.56
}
```

## API Endpoints

### Transaction Endpoints

#### Basic Operations
- `POST /api/transactions` - Create a single transaction
- `POST /api/transactions/batch` - Create multiple transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `GET /api/transactions` - Get all transactions
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction
- `DELETE /api/transactions` - Delete all transactions

#### Search and Filter Operations
- `GET /api/transactions/card/{cardId}` - Get transactions by card ID
- `GET /api/transactions/card/{cardId}/paginated?page=0&size=10` - Get transactions by card ID with pagination
- `GET /api/transactions/merchant/{merchantId}` - Get transactions by merchant ID
- `GET /api/transactions/amount-range?minAmount=10&maxAmount=100` - Filter by amount range
- `GET /api/transactions/date-range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59` - Filter by date range
- `GET /api/transactions/search?description=grocery` - Full-text search in description
- `GET /api/transactions/top-amounts` - Get top 10 transactions by amount
- `GET /api/transactions/card/{cardId}/count` - Count transactions by card ID
- `GET /api/transactions/count` - Get total transaction count

### User Activity Endpoints

#### Basic Operations
- `POST /api/user-activities` - Create user activity
- `POST /api/user-activities/batch` - Create multiple user activities
- `GET /api/user-activities/{userId}` - Get user activity by user ID
- `GET /api/user-activities` - Get all user activities
- `PUT /api/user-activities/{userId}` - Update user activity
- `DELETE /api/user-activities/{userId}` - Delete user activity
- `DELETE /api/user-activities` - Delete all user activities

#### Analytics Operations
- `GET /api/user-activities/transaction-count-range?minCount=1&maxCount=100` - Filter by transaction count range
- `GET /api/user-activities/spending-range?minSpent=100&maxSpent=1000` - Filter by spending range
- `GET /api/user-activities/last-transaction-range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59` - Filter by last transaction date
- `GET /api/user-activities/top-spenders` - Get top 10 spenders
- `GET /api/user-activities/most-active` - Get top 10 most active users
- `GET /api/user-activities/recent-activity` - Get users with activity in last 7 days
- `GET /api/user-activities/inactive` - Get users inactive for 30+ days
- `GET /api/user-activities/active/paginated?minTransactionCount=5&page=0&size=10` - Get active users with pagination
- `GET /api/user-activities/spending-range/count?minSpent=100&maxSpent=1000` - Count users by spending range
- `GET /api/user-activities/count` - Get total user activity count

## Example Usage

### Create a Transaction
```bash
curl -X POST http://localhost:9080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "id": "tx001",
    "cardId": "card123",
    "merchantId": "merchant456",
    "amount": 29.99,
    "date": "2025-09-26T14:30:00",
    "description": "Coffee purchase at Starbucks downtown"
  }'
```

### Search Transactions by Description
```bash
curl "http://localhost:9080/api/transactions/search?description=coffee"
```

### Get Transactions by Amount Range
```bash
curl "http://localhost:9080/api/transactions/amount-range?minAmount=20&maxAmount=50"
```

### Create User Activity
```bash
curl -X POST http://localhost:9080/api/user-activities \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user001",
    "txCount": 25,
    "lastTxDate": "2025-09-26T14:30:00",
    "totalSpent": 750.50
  }'
```

### Get Top Spenders
```bash
curl "http://localhost:9080/api/user-activities/top-spenders"
```

## Configuration

The service connects to Elasticsearch using the configuration in `application.yml`:

```yaml
spring:
  elasticsearch:
    uris: http://elasticsearch:9200
```

## Setup and Deployment

1. **Prerequisites**: Elasticsearch must be running and accessible
2. **Auto-Configuration**: Indices and mappings are created automatically on startup
3. **Docker Integration**: Configured to work with Docker Compose setup
4. **Service Discovery**: Registers with Eureka server for microservice communication

## Features Implemented

✅ **Full-text search** with standard analyzer on transaction descriptions  
✅ **Advanced filtering** by amount, date, card ID, merchant ID  
✅ **Pagination support** for large datasets  
✅ **User activity analytics** with spending and transaction patterns  
✅ **Automatic index creation** with proper mappings  
✅ **RESTful API** with comprehensive endpoints  
✅ **Microservice integration** with Eureka service discovery  
✅ **Docker-ready configuration**  

## Future Enhancements

- Aggregation queries for advanced analytics
- Bulk operations optimization
- Search suggestions and auto-complete
- Data population scripts for testing
- Performance monitoring and metrics