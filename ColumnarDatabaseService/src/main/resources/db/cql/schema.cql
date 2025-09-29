-- =========================================================
--  RAW tabele (denormalizovani pogledi)
-- =========================================================

-- 1) tx_by_user  — PK ((user_id, tx_date), tx_time DESC)
CREATE TABLE IF NOT EXISTS tx_by_user (
  user_id        uuid,
  tx_date        date,
  tx_time        timeuuid,
  tx_id          uuid,
  card_id        uuid,
  merchant_id    uuid,
  category_id    uuid,
  amount_cents   bigint,
  currency       text,
  status         text,
  PRIMARY KEY ((user_id, tx_date), tx_time)
) WITH CLUSTERING ORDER BY (tx_time DESC);

-- 2) tx_by_merchant — PK ((merchant_id, tx_date), tx_time DESC)
CREATE TABLE IF NOT EXISTS tx_by_merchant (
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

-- 3) tx_by_category — PK ((category_id, tx_date), tx_time DESC)
CREATE TABLE IF NOT EXISTS tx_by_category (
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
--  COUNTER agregati
-- =========================================================

-- 4) merchant_aggregates — PK ((merchant_id, period), period_key)
CREATE TABLE IF NOT EXISTS merchant_aggregates (
  merchant_id    uuid,
  period         text,      -- 'DAY' | 'MONTH'
  period_key     text,      -- 'YYYY-MM-DD' ili 'YYYYMM'
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((merchant_id, period), period_key)
);

-- 5) category_aggregates — PK ((category_id, period), period_key)
CREATE TABLE IF NOT EXISTS category_aggregates (
  category_id    uuid,
  period         text,      -- 'DAY' | 'MONTH'
  period_key     text,      -- 'YYYY-MM-DD' ili 'YYYYMM'
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((category_id, period), period_key)
);

-- 7) merchant_aggregates_by_period — PK ((period, period_key), merchant_id)
CREATE TABLE IF NOT EXISTS merchant_aggregates_by_period (
  period         text,      -- 'DAY' | 'MONTH'
  period_key     text,      -- 'YYYY-MM-DD' ili 'YYYYMM'
  merchant_id    uuid,
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((period, period_key), merchant_id)
);

-- 8) category_aggregates_by_period — PK ((period, period_key), category_id)
CREATE TABLE IF NOT EXISTS category_aggregates_by_period (
  period         text,      -- 'DAY' | 'MONTH'
  period_key     text,      -- 'YYYY-MM-DD' ili 'YYYYMM'
  category_id    uuid,
  tx_count       counter,
  amount_cents   counter,
  PRIMARY KEY ((period, period_key), category_id)
);

-- =========================================================
--  Obična agregaciona tabela (za grafove)
-- =========================================================

-- 6) user_daily_totals — PK ((user_id), tx_date DESC)
CREATE TABLE IF NOT EXISTS user_daily_totals (
  user_id          uuid,
  tx_date          date,
  day_count        int,
  day_amount_cents bigint,
  PRIMARY KEY ((user_id), tx_date)
) WITH CLUSTERING ORDER BY (tx_date DESC);

-- =========================================================
--  Dedup tabela (LWT po tx_id)
-- =========================================================

-- 9) tx_dedup — LWT deduplikacija eventa po tx_id
CREATE TABLE IF NOT EXISTS tx_dedup (
  tx_id    uuid PRIMARY KEY,
  seen_at  timestamp
);

-- =========================================================
--  (opciono) podešavanja TTL/kompakcije — primeri
-- =========================================================

-- ALTER TABLE tx_by_user
--   WITH default_time_to_live = 0;  -- 0 = bez TTL

-- ALTER TABLE tx_by_merchant
--   WITH compaction = {'class': 'LeveledCompactionStrategy'};

-- =========================================================
--  (opciono) čišćenje starih "transactions_by_*" ako postoje
-- =========================================================
-- DROP TABLE IF EXISTS transactions_by_user;
-- DROP TABLE IF EXISTS transactions_by_merchant;
-- DROP TABLE IF EXISTS transactions_by_category;
