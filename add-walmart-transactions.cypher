// Add diverse transactions for Walmart merchant from different users

// Find Walmart merchant and users (we'll create cards if they don't exist)
MATCH (m:Merchant {email: 'walmart@merchant.test'})
MATCH (u1:User {email: 'robert.smith179@example.com'})
MATCH (u2:User {email: 'james.smith180@example.com'})
MATCH (u3:User {email: 'sarah.smith173@example.com'})
MATCH (u4:User {email: 'david.johnson174@example.com'})
MATCH (u5:User {email: 'james.williams175@example.com'})
MATCH (u6:User {email: 'robert.brown176@example.com'})
MATCH (u7:User {email: 'mary.davis177@example.com'})
MATCH (u8:User {email: 'emily.davis186@example.com'})
MATCH (u9:User {email: 'robert.jones188@example.com'})
MATCH (u10:User {email: 'lisa.smith181@example.com'})

// Get or create cards for each user
MERGE (u1)-[:OWNS]->(c1:Card {cardNumber: '4532123456789001'})
ON CREATE SET c1.network = 'VISA', c1.type = 'DEBIT', c1.expiryDate = '2027-12-31', c1.issuer = 'Chase Bank', c1.contactless = true

MERGE (u2)-[:OWNS]->(c2:Card {cardNumber: '5425233430109002'})
ON CREATE SET c2.network = 'MASTERCARD', c2.type = 'CREDIT', c2.expiryDate = '2026-11-30', c2.issuer = 'Bank of America', c2.contactless = true

MERGE (u3)-[:OWNS]->(c3:Card {cardNumber: '374245455400003'})
ON CREATE SET c3.network = 'AMEX', c3.type = 'CREDIT', c3.expiryDate = '2028-10-31', c3.issuer = 'American Express', c3.contactless = true

MERGE (u4)-[:OWNS]->(c4:Card {cardNumber: '4532234567890004'})
ON CREATE SET c4.network = 'VISA', c4.type = 'CREDIT', c4.expiryDate = '2027-09-30', c4.issuer = 'Wells Fargo', c4.contactless = false

MERGE (u5)-[:OWNS]->(c5:Card {cardNumber: '5425345678900005'})
ON CREATE SET c5.network = 'MASTERCARD', c5.type = 'DEBIT', c5.expiryDate = '2026-08-31', c5.issuer = 'Citibank', c5.contactless = true

MERGE (u6)-[:OWNS]->(c6:Card {cardNumber: '4532456789010006'})
ON CREATE SET c6.network = 'VISA', c6.type = 'CREDIT', c6.expiryDate = '2027-07-31', c6.issuer = 'Chase Bank', c6.contactless = true

MERGE (u7)-[:OWNS]->(c7:Card {cardNumber: '5425567890120007'})
ON CREATE SET c7.network = 'MASTERCARD', c7.type = 'CREDIT', c7.expiryDate = '2028-06-30', c7.issuer = 'HSBC', c7.contactless = false

MERGE (u8)-[:OWNS]->(c8:Card {cardNumber: '4532678901230008'})
ON CREATE SET c8.network = 'VISA', c8.type = 'DEBIT', c8.expiryDate = '2026-05-31', c8.issuer = 'Wells Fargo', c8.contactless = true

MERGE (u9)-[:OWNS]->(c9:Card {cardNumber: '5425789012340009'})
ON CREATE SET c9.network = 'MASTERCARD', c9.type = 'CREDIT', c9.expiryDate = '2027-04-30', c9.issuer = 'Bank of America', c9.contactless = true

MERGE (u10)-[:OWNS]->(c10:Card {cardNumber: '4532890123450010'})
ON CREATE SET c10.network = 'VISA', c10.type = 'CREDIT', c10.expiryDate = '2028-03-31', c10.issuer = 'Citibank', c10.contactless = false

WITH m, 
     collect(c1)[0] as card1, collect(c2)[0] as card2, collect(c3)[0] as card3, 
     collect(c4)[0] as card4, collect(c5)[0] as card5, collect(c6)[0] as card6,
     collect(c7)[0] as card7, collect(c8)[0] as card8, collect(c9)[0] as card9,
     collect(c10)[0] as card10

// Create diverse transactions
CREATE (card1)-[:TRANSACTED_WITH {
  amount: 125.50,
  timestamp: datetime('2024-10-15T10:30:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card1)-[:TRANSACTED_WITH {
  amount: 67.99,
  timestamp: datetime('2024-10-28T15:45:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card2)-[:TRANSACTED_WITH {
  amount: 89.00,
  timestamp: datetime('2024-10-20T09:15:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'ONLINE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card2)-[:TRANSACTED_WITH {
  amount: 210.75,
  timestamp: datetime('2024-11-01T14:20:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'ONLINE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card3)-[:TRANSACTED_WITH {
  amount: 450.00,
  timestamp: datetime('2024-10-18T11:00:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card3)-[:TRANSACTED_WITH {
  amount: 75.25,
  timestamp: datetime('2024-10-22T16:30:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card3)-[:TRANSACTED_WITH {
  amount: 98.50,
  timestamp: datetime('2024-11-02T10:15:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'MOBILE_APP',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card4)-[:TRANSACTED_WITH {
  amount: 156.80,
  timestamp: datetime('2024-10-25T13:45:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card4)-[:TRANSACTED_WITH {
  amount: 42.30,
  timestamp: datetime('2024-10-30T08:20:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card5)-[:TRANSACTED_WITH {
  amount: 320.00,
  timestamp: datetime('2024-10-16T12:00:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'ONLINE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card5)-[:TRANSACTED_WITH {
  amount: 88.99,
  timestamp: datetime('2024-10-29T17:30:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card6)-[:TRANSACTED_WITH {
  amount: 199.99,
  timestamp: datetime('2024-10-17T10:45:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'MOBILE_APP',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card6)-[:TRANSACTED_WITH {
  amount: 55.40,
  timestamp: datetime('2024-10-31T14:00:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card7)-[:TRANSACTED_WITH {
  amount: 112.75,
  timestamp: datetime('2024-10-19T11:30:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card7)-[:TRANSACTED_WITH {
  amount: 267.50,
  timestamp: datetime('2024-10-26T15:15:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'ONLINE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card7)-[:TRANSACTED_WITH {
  amount: 33.20,
  timestamp: datetime('2024-11-03T09:00:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card8)-[:TRANSACTED_WITH {
  amount: 145.00,
  timestamp: datetime('2024-10-21T13:00:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'MOBILE_APP',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card8)-[:TRANSACTED_WITH {
  amount: 78.60,
  timestamp: datetime('2024-10-27T16:45:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card9)-[:TRANSACTED_WITH {
  amount: 512.00,
  timestamp: datetime('2024-10-23T10:00:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'ONLINE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card9)-[:TRANSACTED_WITH {
  amount: 95.30,
  timestamp: datetime('2024-10-24T12:30:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

CREATE (card10)-[:TRANSACTED_WITH {
  amount: 188.25,
  timestamp: datetime('2024-10-14T14:20:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card10)-[:TRANSACTED_WITH {
  amount: 64.50,
  timestamp: datetime('2024-10-28T11:00:00Z'),
  paymentType: 'CARD_NOT_PRESENT',
  channel: 'MOBILE_APP',
  contactless: false,
  status: 'SUCCESS'
}]->(m)

CREATE (card10)-[:TRANSACTED_WITH {
  amount: 129.99,
  timestamp: datetime('2024-11-02T15:30:00Z'),
  paymentType: 'CARD_PRESENT',
  channel: 'IN_STORE',
  contactless: true,
  status: 'SUCCESS'
}]->(m)

RETURN 'Added 23 new transactions from 10 different users to Walmart merchant' as result;
