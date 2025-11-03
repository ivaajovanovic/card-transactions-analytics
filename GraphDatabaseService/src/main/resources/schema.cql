// Neo4j Schema Constraints and Indexes for Card Transaction Graph Analytics
// Run these in Neo4j Browser or cypher-shell before starting the application

// ===== CONSTRAINTS (Uniqueness) =====

// User constraints
CREATE CONSTRAINT user_external_unique IF NOT EXISTS FOR (u:User) REQUIRE u.externalId IS UNIQUE;

// Card constraints
CREATE CONSTRAINT card_panHash_unique IF NOT EXISTS FOR (c:Card) REQUIRE c.panHash IS UNIQUE;

// Merchant constraints
CREATE CONSTRAINT merchant_merchantId_unique IF NOT EXISTS FOR (m:Merchant) REQUIRE m.merchantId IS UNIQUE;

// Category constraints
CREATE CONSTRAINT category_code_unique IF NOT EXISTS FOR (cat:Category) REQUIRE cat.code IS UNIQUE;

// ===== INDEXES (Performance) =====

// Category indexes
CREATE INDEX category_code IF NOT EXISTS FOR (c:Category) ON (c.code);
CREATE INDEX category_name IF NOT EXISTS FOR (c:Category) ON (c.name);

// Merchant indexes
CREATE INDEX merchant_mcc IF NOT EXISTS FOR (m:Merchant) ON (m.mcc);
CREATE INDEX merchant_city IF NOT EXISTS FOR (m:Merchant) ON (m.city);
CREATE INDEX merchant_country IF NOT EXISTS FOR (m:Merchant) ON (m.country);

// Region indexes
CREATE INDEX region_name IF NOT EXISTS FOR (r:Region) ON (r.name);
CREATE INDEX region_lat_lon IF NOT EXISTS FOR (r:Region) ON (r.lat, r.lon);

// Device indexes
CREATE INDEX device_deviceId IF NOT EXISTS FOR (d:Device) ON (d.deviceId);

// Terminal indexes
CREATE INDEX terminal_terminalId IF NOT EXISTS FOR (t:Terminal) ON (t.terminalId);

// TimeBucket indexes
CREATE INDEX timebucket_key IF NOT EXISTS FOR (tb:TimeBucket) ON (tb.granularity, tb.year, tb.month, tb.week);

// Card indexes
CREATE INDEX card_network IF NOT EXISTS FOR (c:Card) ON (c.network);
CREATE INDEX card_type IF NOT EXISTS FOR (c:Card) ON (c.type);

// User indexes
CREATE INDEX user_segment IF NOT EXISTS FOR (u:User) ON (u.segment);

// ===== SAMPLE DATA (Optional - for testing) =====

// Create sample acceptance nodes
CREATE (:Acceptance {network:'VISA', type:'DEBIT'});
CREATE (:Acceptance {network:'VISA', type:'CREDIT'});
CREATE (:Acceptance {network:'MASTERCARD', type:'DEBIT'});
CREATE (:Acceptance {network:'MASTERCARD', type:'CREDIT'});
CREATE (:Acceptance {network:'AMEX', type:'CREDIT'});
CREATE (:Acceptance {network:'MAESTRO', type:'DEBIT'});

// Create sample categories
CREATE (:Category {code:'5411', name:'Grocery Stores', parentCode:'5000'});
CREATE (:Category {code:'5812', name:'Restaurants', parentCode:'5000'});
CREATE (:Category {code:'5541', name:'Gas Stations', parentCode:'5000'});
CREATE (:Category {code:'5999', name:'Miscellaneous', parentCode:'5000'});

// ===== VERIFICATION QUERIES =====

// List all constraints
SHOW CONSTRAINTS;

// List all indexes
SHOW INDEXES;

// Count nodes by label
MATCH (n)
RETURN labels(n) AS Label, count(n) AS Count
ORDER BY Count DESC;
