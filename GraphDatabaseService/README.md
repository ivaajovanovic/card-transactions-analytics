# Card Transactions Graph Service (Spring Data Neo4j)

Backend servis za graph analitiku kartičnih transakcija korišćenjem **Spring Boot 3.3.4 + Spring Data Neo4j 7**.

## 📋 Pregled

Ovaj servis omogućava:
- **User Analytics**: Analiza potrošnje korisnika po kategorijama, merchantima, kanalima
- **Merchant Analytics**: Analiza ponašanja kupaca, failure rate-ova, repeat customers
- **Admin Analytics**: Globalna analitika - acceptance coverage, failure reasons, card type analysis

## 🏗️ Arhitektura

### Domain Model

Graph model sa **transakcijama kao relationship properties**:
- **Nodes**: User, Card, Merchant, Category, Region, Device, Terminal, TimeBucket, TransactionItem
- **Relationships**: OWNS, IN_CATEGORY, IN_REGION, ACCEPTS, TRANSACTED_WITH (sa properties)

### Struktura Paketa

```
rs.ac.uns.acs.nais.GraphDatabaseService/
├─ domain/               # Node i Relationship entiteti
│  ├─ enums/             # CardNetwork, CardType, PaymentPurpose, etc.
│  ├─ *Node.java         # Neo4j @Node entiteti
│  └─ *Rel.java          # @RelationshipProperties
├─ dto/                  # Data Transfer Objects
├─ repository/           # Spring Data Neo4j repositories
├─ service/              # Business logika
│  └─ impl/
├─ web/                  # REST Controllers
├─ config/               # Konfiguracija (Neo4j, OpenAPI)
└─ util/                 # Helper klase
```

## 🚀 Quick Start

### Preduslovi

- **Java 17+**
- **Maven 3.8+**
- **Neo4j 5.x** (running on `bolt://localhost:7687`)
- **Eureka Server** (optional, za service discovery)

### 1. Pokreni Neo4j

```bash
docker run -d \
  --name neo4j \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/password \
  neo4j:5.14
```

### 2. Kreiraj Schema

Otvori Neo4j Browser (`http://localhost:7474`) i izvršicypher skript:

```bash
cypher-shell -u neo4j -p password < src/main/resources/schema.cql
```

### 3. Pokreni Aplikaciju

```bash
mvn clean install
mvn spring-boot:run
```

Servis će biti dostupan na: `http://localhost:8083`

### 4. Swagger UI

API dokumentacija: `http://localhost:8083/swagger-ui/index.html`

## 📊 API Endpoints

### Transaction Management

| Method | Endpoint | Opis |
|--------|----------|------|
| POST | `/api/transactions/ingest` | Unos nove transakcije |
| GET | `/api/transactions/limit-check/{panHash}` | Provera card limita |

### User Analytics

| Method | Endpoint | Opis |
|--------|----------|------|
| GET | `/api/analytics/users/{userId}/spend` | Potrošnja po grupama (purpose/category/merchant) |
| GET | `/api/analytics/users/{userId}/top-merchants` | Top N merchantova |
| GET | `/api/analytics/users/{userId}/channel-mix` | Distribucija po kanalima (APP/WEB/IN_STORE) |
| GET | `/api/analytics/users/{userId}/basket-category` | Košarica po kategorijama |
| GET | `/api/analytics/users/{userId}/time-of-day` | Potrošnja po satima dana |
| GET | `/api/analytics/users/{userId}/limit-utilization` | Iskorišćenost limita |

### Merchant Analytics

| Method | Endpoint | Opis |
|--------|----------|------|
| GET | `/api/analytics/merchants/{merchantId}/spend-by-purpose` | Potrošnja po purpose |
| GET | `/api/analytics/merchants/{merchantId}/channel-mix` | Kanali kupovine |
| GET | `/api/analytics/merchants/{merchantId}/card-network-share` | Udeo card network-a |
| GET | `/api/analytics/merchants/{merchantId}/failure-reasons` | Razlozi neuspelih transakcija |
| GET | `/api/analytics/merchants/{merchantId}/avg-ticket-over-time` | Prosečan račun kroz vreme |
| GET | `/api/analytics/merchants/{merchantId}/repeat-customers` | Repeat vs New customers |
| GET | `/api/analytics/merchants/{merchantId}/region-heatmap` | Geografska distribucija |
| GET | `/api/analytics/merchants/{merchantId}/contactless-share` | Contactless vs ostalo |

### Admin Analytics

| Method | Endpoint | Opis |
|--------|----------|------|
| GET | `/api/analytics/admin/card-type-failures` | Failure rate po card type |
| GET | `/api/analytics/admin/acceptance-coverage` | Acceptance coverage merchantova |
| GET | `/api/analytics/admin/failed-by-merchant` | Top N merchantova sa najvećim brojem fails |
| GET | `/api/analytics/admin/failure-reasons` | Globalna distribucija failure razloga |
| GET | `/api/analytics/admin/acceptance-gaps` | Acceptance gaps analiza |

## 📝 Primer Korišćenja

### Unos Transakcije

```bash
curl -X POST http://localhost:8083/api/transactions/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "user": {
      "externalId": "user123",
      "fullName": "Marko Marković",
      "email": "marko@example.com"
    },
    "card": {
      "panHash": "abc123hash",
      "network": "VISA",
      "type": "DEBIT",
      "monthlyLimit": 100000.0
    },
    "merchant": {
      "merchantId": "merch001",
      "name": "Supermarket Idea",
      "city": "Belgrade"
    },
    "category": {
      "code": "5411",
      "name": "Grocery Stores"
    },
    "region": {
      "name": "Belgrade"
    },
    "txn": {
      "timestamp": "2025-01-15T10:30:00Z",
      "amount": 2500.0,
      "currency": "RSD",
      "paymentType": "POS",
      "purpose": "GROCERIES",
      "status": "SUCCESS",
      "channel": "IN_STORE"
    }
  }'
```

### User Analytics Query

```bash
curl "http://localhost:8083/api/analytics/users/user123/spend?\
from=2025-01-01T00:00:00Z&\
to=2025-12-31T23:59:59Z&\
groupBy=category"
```

## 🔧 Konfiguracija

### application.yml

```yaml
server:
  port: 8083

spring:
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: password
```

### Environment Variables

```bash
NEO4J_URI=bolt://localhost:7687
NEO4J_USERNAME=neo4j
NEO4J_PASSWORD=password
EUREKA_URL=http://localhost:8761/eureka/
```

## 🧪 Data Generator

Za generisanje test podataka, kreiraj Python skriptu ili koristi Cypher:

```cypher
// Primer: Kreiraj 10 random transakcija
MATCH (u:User), (c:Card), (m:Merchant)
WHERE u.externalId = 'user123' AND c.panHash = 'abc123hash'
WITH u, c, collect(m)[toInteger(rand() * size(collect(m)))] AS randomMerchant
LIMIT 10
CREATE (c)-[:TRANSACTED_WITH {
  timestamp: datetime(),
  amount: rand() * 10000,
  currency: 'RSD',
  status: 'SUCCESS',
  purpose: 'GROCERIES'
}]->(randomMerchant)
```

## 📦 Dependencies

- Spring Boot 3.3.4
- Spring Data Neo4j 7.x
- Spring Cloud Netflix Eureka Client
- SpringDoc OpenAPI (Swagger)
- Lombok
- Jackson (datetime support)

## 🎯 Roadmap

- [ ] Data generator script (Python/Java)
- [ ] ML modeli za fraud detection
- [ ] Real-time streaming analytics
- [ ] GraphQL API
- [ ] Recommendation engine
- [ ] Advanced time-series analytics

## 📄 License

MIT License

## 👥 Authors

- Vaš Tim

---

**Napomena**: Ovo je backend-only servis. Za frontend dashboard, integriši sa React/Angular/Vue aplikacijom.
