-- =========================================================
--  Keyspace (dev)
-- =========================================================
CREATE KEYSPACE IF NOT EXISTS nais
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE nais;

-- =========================================================
--  1) transactions_by_user
--     PRIMARY KEY ((user_id, tx_date), tx_time DESC)
-- =========================================================
CREATE TABLE IF NOT EXISTS transactions_by_user (
  user_id        uuid,
  tx_date        date,       -- UTC bucket (YYYY-MM-DD)
  tx_time        timeuuid,   -- za sort DESC i jedinstvenost
  tx_id          uuid,
  card_id        uuid,
  merchant_id    uuid,
  category_id    uuid,
  amount_cents   bigint,
  currency       text,
  status         text,
  PRIMARY KEY ((user_id, tx_date), tx_time)
) WITH CLUSTERING ORDER BY (tx_time DESC);

-- =========================================================
--  2) transactions_by_merchant
--     PRIMARY KEY ((merchant_id, tx_date), tx_time DESC)
-- =========================================================
CREATE TABLE IF NOT EXISTS transactions_by_merchant (
  merchant_id    uuid,
  tx_date        date,
  tx_time        timeuuid,
  tx_id          uuid,
  user_id        uuid,
  card_id        uuid,
  category_id    uuid,
  amount_cents   bigint,
  currency       text,
  status         text,
  PRIMARY KEY ((merchant_id, tx_date), tx_time)
) WITH CLUSTERING ORDER BY (tx_time DESC);

-- =========================================================
--  3) transactions_by_category
--     PRIMARY KEY ((category_id, tx_date), tx_time DESC)
-- =========================================================
CREATE TABLE IF NOT EXISTS transactions_by_category (
  category_id    uuid,
  tx_date        date,
  tx_time        timeuuid,
  tx_id          uuid,
  user_id        uuid,
  card_id        uuid,
  merchant_id    uuid,
  amount_cents   bigint,
  currency       text,
  status         text,
  PRIMARY KEY ((category_id, tx_date), tx_time)
) WITH CLUSTERING ORDER BY (tx_time DESC);

-- =========================================================
--  4) merchant_aggregates  (po trgovcu i periodu)
--     PRIMARY KEY ((merchant_id, period), period_key)
--     COUNTER tabela: sve non-key kolone moraju biti counter
-- =========================================================
CREATE TABLE IF NOT EXISTS merchant_aggregates (
  merchant_id    uuid,
  period         text,      -- 'DAY' | 'MONTH'
  period_key     text,      -- 'YYYY-MM-DD' ili 'YYYYMM'
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((merchant_id, period), period_key)
);

-- =========================================================
--  5) category_aggregates  (po kategoriji i periodu)
--     PRIMARY KEY ((category_id, period), period_key)
-- =========================================================
CREATE TABLE IF NOT EXISTS category_aggregates (
  category_id    uuid,
  period         text,      -- 'DAY' | 'MONTH'
  period_key     text,      -- 'YYYY-MM-DD' ili 'YYYYMM'
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((category_id, period), period_key)
);

-- =========================================================
--  6) user_daily_totals (obična tabela za range/graf)
--     PRIMARY KEY ((user_id), tx_date DESC)
-- =========================================================
CREATE TABLE IF NOT EXISTS user_daily_totals (
  user_id          uuid,
  tx_date          date,
  day_count        int,
  day_amount_cents bigint,
  PRIMARY KEY ((user_id), tx_date)
) WITH CLUSTERING ORDER BY (tx_date DESC);

-- =========================================================
--  7) merchant_aggregates_by_period  (GLOBAL: top-N za period)
--     PRIMARY KEY ((period, period_key), merchant_id)
-- =========================================================
CREATE TABLE IF NOT EXISTS merchant_aggregates_by_period (
  period         text,     -- 'DAY' | 'MONTH'
  period_key     text,     -- 'YYYY-MM-DD' ili 'YYYYMM'
  merchant_id    uuid,
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((period, period_key), merchant_id)
);

-- =========================================================
--  8) category_aggregates_by_period  (GLOBAL: top-N za period)
--     PRIMARY KEY ((period, period_key), category_id)
-- =========================================================
CREATE TABLE IF NOT EXISTS category_aggregates_by_period (
  period         text,     -- 'DAY' | 'MONTH'
  period_key     text,     -- 'YYYY-MM-DD' ili 'YYYYMM'
  category_id    uuid,
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((period, period_key), category_id)
);

-- =========================================================
--  9) tx_dedup (opciono) — LWT deduplikacija eventa po tx_id
-- =========================================================
CREATE TABLE IF NOT EXISTS tx_dedup (
  tx_id    uuid PRIMARY KEY,
  seen_at  timestamp
);

-- ===========================================
--  (opciono) default TTL ili kompakcija primeri
--  (odkomentariši ako želiš automatsko isticanje)
-- ===========================================
-- ALTER TABLE transactions_by_user
--   WITH default_time_to_live = 0;  -- 0 = bez TTL
--
-- ALTER TABLE transactions_by_merchant
--   WITH compaction = {'class': 'LeveledCompactionStrategy'};
