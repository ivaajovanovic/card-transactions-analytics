package rs.ac.uns.acs.nais.columnar.config;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.DependsOn;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("cqlSession")
public class SchemaInitializer {

    private final CqlTemplate cqlTemplate;

    @PostConstruct
    public void applySchema() {
        try {
            log.info("Applying Cassandra schema programmatically");
            
            // Create all required tables
            createTransactionsTables();
            createAggregatesTables();
            createDedupTable();
            
            log.info("Cassandra schema applied successfully.");
        } catch (Exception e) {
            log.error("Failed to apply Cassandra schema", e);
            throw new RuntimeException("Failed to apply Cassandra schema", e);
        }
    }
    
    private void createTransactionsTables() {
        // transactions_by_user
        String createUsersTable = 
            "CREATE TABLE IF NOT EXISTS transactions_by_user (" +
            "user_id uuid, tx_date date, tx_time timeuuid, tx_id uuid, " +
            "card_id uuid, merchant_id uuid, category_id uuid, amount_cents bigint, " +
            "currency text, status text, " +
            "PRIMARY KEY ((user_id, tx_date), tx_time)) " +
            "WITH CLUSTERING ORDER BY (tx_time DESC)";
        log.info("Creating transactions_by_user table");
        cqlTemplate.execute(createUsersTable);
        
        // transactions_by_merchant
        String createMerchantTable = 
            "CREATE TABLE IF NOT EXISTS transactions_by_merchant (" +
            "merchant_id uuid, tx_date date, tx_time timeuuid, tx_id uuid, " +
            "user_id uuid, card_id uuid, category_id uuid, amount_cents bigint, " +
            "currency text, status text, " +
            "PRIMARY KEY ((merchant_id, tx_date), tx_time)) " +
            "WITH CLUSTERING ORDER BY (tx_time DESC)";
        log.info("Creating transactions_by_merchant table");
        cqlTemplate.execute(createMerchantTable);
        
        // transactions_by_category
        String createCategoryTable = 
            "CREATE TABLE IF NOT EXISTS transactions_by_category (" +
            "category_id uuid, tx_date date, tx_time timeuuid, tx_id uuid, " +
            "user_id uuid, card_id uuid, merchant_id uuid, amount_cents bigint, " +
            "currency text, status text, " +
            "PRIMARY KEY ((category_id, tx_date), tx_time)) " +
            "WITH CLUSTERING ORDER BY (tx_time DESC)";
        log.info("Creating transactions_by_category table");
        cqlTemplate.execute(createCategoryTable);
    }
    
    private void createAggregatesTables() {
        // user_daily_totals
        String createDailyTotalsTable = 
            "CREATE TABLE IF NOT EXISTS user_daily_totals (" +
            "user_id uuid, tx_date date, day_count int, day_amount_cents bigint, " +
            "PRIMARY KEY ((user_id), tx_date)) " +
            "WITH CLUSTERING ORDER BY (tx_date DESC)";
        log.info("Creating user_daily_totals table");
        cqlTemplate.execute(createDailyTotalsTable);
        
        // merchant_aggregates
        String createMerchantAggregatesTable = 
            "CREATE TABLE IF NOT EXISTS merchant_aggregates (" +
            "merchant_id uuid, period text, period_key text, " +
            "tx_count counter, amount_cents counter, " +
            "PRIMARY KEY ((merchant_id, period), period_key))";
        log.info("Creating merchant_aggregates table");
        cqlTemplate.execute(createMerchantAggregatesTable);
        
        // category_aggregates
        String createCategoryAggregatesTable = 
            "CREATE TABLE IF NOT EXISTS category_aggregates (" +
            "category_id uuid, period text, period_key text, " +
            "tx_count counter, amount_cents counter, " +
            "PRIMARY KEY ((category_id, period), period_key))";
        log.info("Creating category_aggregates table");
        cqlTemplate.execute(createCategoryAggregatesTable);
        
        // merchant_aggregates_by_period  
        String createMerchantByPeriodTable = 
            "CREATE TABLE IF NOT EXISTS merchant_aggregates_by_period (" +
            "period text, period_key text, merchant_id uuid, " +
            "tx_count counter, amount_cents counter, " +
            "PRIMARY KEY ((period, period_key), merchant_id))";
        log.info("Creating merchant_aggregates_by_period table");
        cqlTemplate.execute(createMerchantByPeriodTable);
        
        // category_aggregates_by_period
        String createCategoryByPeriodTable = 
            "CREATE TABLE IF NOT EXISTS category_aggregates_by_period (" +
            "period text, period_key text, category_id uuid, " +
            "tx_count counter, amount_cents counter, " +
            "PRIMARY KEY ((period, period_key), category_id))";
        log.info("Creating category_aggregates_by_period table");
        cqlTemplate.execute(createCategoryByPeriodTable);
    }
    
    private void createDedupTable() {
        String createDedupTable = 
            "CREATE TABLE IF NOT EXISTS tx_dedup (" +
            "tx_id uuid, seen_at timestamp, " +
            "PRIMARY KEY (tx_id))";
        log.info("Creating tx_dedup table");
        cqlTemplate.execute(createDedupTable);
    }
}
