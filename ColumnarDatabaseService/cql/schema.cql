-- KEYSPACE
CREATE KEYSPACE IF NOT EXISTS nais
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE nais;

-- 1) transactions_by_user  (partition=user_id, cluster=tx_date, tx_time DESC)
CREATE TABLE IF NOT EXISTS transactions_by_user (
  user_id       uuid,
  tx_date       date,        -- bucket (UTC)
  tx_time       timeuuid,    -- za sort DESC i jedinstvenost
  tx_id         uuid,
  card_id       uuid,
  merchant_id   uuid,
  category_id   uuid,
  amount_cents  bigint,
  currency      text,
  status        text,
  PRIMARY KEY ((user_id, tx_date), tx_time)
) WITH CLUSTERING ORDER BY (tx_time DESC);

-- 2) transactions_by_merchant (partition=merchant_id, cluster=tx_date, tx_time DESC)
CREATE TABLE IF NOT EXISTS transactions_by_merchant (
  merchant_id   uuid,
  tx_date       date,
  tx_time       timeuuid,
  tx_id         uuid,
  user_id       uuid,
  card_id       uuid,
  category_id   uuid,
  amount_cents  bigint,
  currency      text,
  status        text,
  PRIMARY KEY ((merchant_id, tx_date), tx_time)
) WITH CLUSTERING ORDER BY (tx_time DESC);

-- 3) transactions_by_category (partition=category_id, cluster=tx_date, tx_time DESC)
CREATE TABLE IF NOT EXISTS transactions_by_category (
  category_id   uuid,
  tx_date       date,
  tx_time       timeuuid,
  tx_id         uuid,
  user_id       uuid,
  card_id       uuid,
  merchant_id   uuid,
  amount_cents  bigint,
  currency      text,
  status        text,
  PRIMARY KEY ((category_id, tx_date), tx_time)
) WITH CLUSTERING ORDER BY (tx_time DESC);

-- 4) merchant_aggregates (precomputed totals per merchant per day + month)
CREATE TABLE IF NOT EXISTS merchant_aggregates (
  merchant_id     uuid,
  period         text,        -- 'DAY' | 'MONTH'
  period_key     text,        -- 'YYYY-MM-DD' ili 'YYYYMM'
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((merchant_id, period), period_key)
);

-- 5) category_aggregates (precomputed totals per category per day + month)
CREATE TABLE IF NOT EXISTS category_aggregates (
  category_id    uuid,
  period         text,        -- 'DAY' | 'MONTH'
  period_key     text,        -- 'YYYY-MM-DD' ili 'YYYYMM'
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((category_id, period), period_key)
);

-- 6) user_daily_totals (agregat za grafove/opseg)
CREATE TABLE IF NOT EXISTS user_daily_totals (
  user_id       uuid,
  tx_date       date,
  day_count     int,
  day_amount_cents bigint,
  PRIMARY KEY ((user_id), tx_date)
) WITH CLUSTERING ORDER BY (tx_date DESC);

-- 7) tx_dedup (idempotencija - table za duplicate detection)
CREATE TABLE IF NOT EXISTS tx_dedup (
  tx_id         uuid,
  seen_at       timestamp,
  PRIMARY KEY (tx_id)
);
