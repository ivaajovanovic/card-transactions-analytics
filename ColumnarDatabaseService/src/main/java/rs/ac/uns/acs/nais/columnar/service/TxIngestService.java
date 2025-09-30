package rs.ac.uns.acs.nais.columnar.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Service;

import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.mapper.TxMapper;
import rs.ac.uns.acs.nais.columnar.model.UserDailyTotal;
import rs.ac.uns.acs.nais.columnar.repo.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TxIngestService {

    private final TxByUserRepo userRepo;
    private final TxByMerchantRepo merchantRepo;
    private final TxByCategoryRepo categoryRepo;
    private final UserDailyTotalsRepo dailyRepo;
    private final CqlTemplate cql;
    private final Supplier<UUID> timeUuidSupplier;
    private final DedupService dedupService;
    private final ElasticsearchIntegrationService elasticsearchService;

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    public void ingest(TransactionDTO t) throws DataAccessException {
        try {
            System.err.println("💥💥💥 INGEST METHOD ENTRY: TxIngestService.ingest() ACTUALLY CALLED for: " + (t != null ? t.getTxId() : "NULL"));
            log.error("💥💥💥 INGEST METHOD ENTRY: TxIngestService.ingest() ACTUALLY CALLED for: {}", (t != null ? t.getTxId() : "NULL"));
            
            log.error("🔥🔥🔥 INGEST START LOG.ERROR: TxIngestService.ingest() called for: {}", t.getTxId());
            System.out.println("🔥🔥🔥 INGEST START: TxIngestService.ingest() called for: " + t.getTxId());
            System.err.println("🔥🔥🔥 INGEST START: TxIngestService.ingest() called for: " + t.getTxId());
            System.out.flush();
            System.err.flush();
        
        // 0) idempotencija: preskoči ako je već viđeno
        if (!dedupService.tryMarkSeen(t.getTxId())) {
            System.out.println("🔥🔥🔥 INGEST DUPLICATE: " + t.getTxId());
            System.err.println("🔥🔥🔥 INGEST DUPLICATE: " + t.getTxId());
            return; // duplikat
        }

        System.out.println("🔥🔥🔥 STEP 1: Processing transaction " + t.getTxId());
        System.err.println("🔥🔥🔥 STEP 1: Processing transaction " + t.getTxId());

        // 1) ključni delovi
        LocalDate date = TxMapper.toUtcDate(t.getOccurredAt());
        UUID timeUuid = timeUuidSupplier.get();
        String dayKey = date.toString();          // YYYY-MM-DD
        String monthKey = MONTH.format(date);     // YYYYMM

        System.out.println("🔥🔥🔥 STEP 2: About to save to Cassandra tables for " + t.getTxId());

        // 2) denormalizovani upis
        userRepo.save(TxMapper.toUserEntity(t, date, timeUuid));
        merchantRepo.save(TxMapper.toMerchantEntity(t, date, timeUuid));
        categoryRepo.save(TxMapper.toCategoryEntity(t, date, timeUuid));

        System.out.println("🔥🔥🔥 STEP 3: Cassandra saves completed for " + t.getTxId());

        // 3) user_daily_totals (obični brojači)
        UserDailyTotal row = dailyRepo.findOneDay(t.getUserId(), date)
                .orElse(new UserDailyTotal(t.getUserId(), date, 0, 0L));
        row.setDayCount((row.getDayCount()==null?0:row.getDayCount()) + 1);
        row.setDayAmountCents((row.getDayAmountCents()==null?0L:row.getDayAmountCents()) + t.getAmountCents());
        dailyRepo.save(row);

        System.out.println("🔥🔥🔥 STEP 4: Daily totals completed for " + t.getTxId());

        // 4) Aggregate counteri - ISPRAVNO za Cassandra schema
        // Dnevni aggregati
        cql.execute("UPDATE merchant_aggregates SET tx_count = tx_count + 1, amount_cents = amount_cents + ? WHERE merchant_id = ? AND period = 'DAY' AND period_key = ?",
                t.getAmountCents(), t.getMerchantId(), dayKey);

        cql.execute("UPDATE category_aggregates SET tx_count = tx_count + 1, amount_cents = amount_cents + ? WHERE category_id = ? AND period = 'DAY' AND period_key = ?",
                t.getAmountCents(), t.getCategoryId(), dayKey);

        System.out.println("🔥🔥🔥 STEP 5: Daily aggregates completed for " + t.getTxId());

        // 5) Mesečni aggregati
        cql.execute("UPDATE merchant_aggregates SET tx_count = tx_count + 1, amount_cents = amount_cents + ? WHERE merchant_id = ? AND period = 'MONTH' AND period_key = ?",
                t.getAmountCents(), t.getMerchantId(), monthKey);

        cql.execute("UPDATE category_aggregates SET tx_count = tx_count + 1, amount_cents = amount_cents + ? WHERE category_id = ? AND period = 'MONTH' AND period_key = ?",
                t.getAmountCents(), t.getCategoryId(), monthKey);

        System.out.println("🔥🔥🔥 STEP 6: Monthly aggregates completed for " + t.getTxId());

        // 6) DODATNE period tabele (merchant_aggregates_by_period, category_aggregates_by_period)
        cql.execute("UPDATE merchant_aggregates_by_period SET tx_count = tx_count + 1, amount_cents = amount_cents + ? WHERE period = 'DAY' AND period_key = ? AND merchant_id = ?",
                t.getAmountCents(), dayKey, t.getMerchantId());

        cql.execute("UPDATE merchant_aggregates_by_period SET tx_count = tx_count + 1, amount_cents = amount_cents + ? WHERE period = 'MONTH' AND period_key = ? AND merchant_id = ?",
                t.getAmountCents(), monthKey, t.getMerchantId());

        cql.execute("UPDATE category_aggregates_by_period SET tx_count = tx_count + 1, amount_cents = amount_cents + ? WHERE period = 'DAY' AND period_key = ? AND category_id = ?",
                t.getAmountCents(), dayKey, t.getCategoryId());

        cql.execute("UPDATE category_aggregates_by_period SET tx_count = tx_count + 1, amount_cents = amount_cents + ? WHERE period = 'MONTH' AND period_key = ? AND category_id = ?",
                t.getAmountCents(), monthKey, t.getCategoryId());

        System.out.println("🔥🔥🔥 STEP 7: Period-by-entity aggregates completed for " + t.getTxId());

        // 6) Forward to Elasticsearch for analytics
        System.err.println("��� CHECKPOINT A: About to check elasticsearchService for " + t.getTxId());
        log.error("🚀🚀🚀 CHECKPOINT A: About to check elasticsearchService for " + t.getTxId());
        
        if (elasticsearchService == null) {
            System.err.println("��� FATAL: elasticsearchService is NULL!");
            log.error("🚀🚀🚀 FATAL: elasticsearchService is NULL!");
            return;
        }
        
        System.err.println("🚀🚀🚀 CHECKPOINT B: elasticsearchService is NOT NULL, calling forwardTransactionToElasticsearch for " + t.getTxId());
        log.error("🚀🚀🚀 CHECKPOINT B: elasticsearchService is NOT NULL, calling forwardTransactionToElasticsearch for " + t.getTxId());
        
        try {
            System.err.println("��� CHECKPOINT C: CALLING elasticsearchService.forwardTransactionToElasticsearch() for " + t.getTxId());
            log.error("��� CHECKPOINT C: CALLING elasticsearchService.forwardTransactionToElasticsearch() for " + t.getTxId());
            
            elasticsearchService.forwardTransactionToElasticsearch(t);
            
            System.err.println("��� CHECKPOINT D: SUCCESS - elasticsearchService.forwardTransactionToElasticsearch() completed for " + t.getTxId());
            log.error("🚀🚀🚀 CHECKPOINT D: SUCCESS - elasticsearchService.forwardTransactionToElasticsearch() completed for " + t.getTxId());
        } catch (Exception e) {
            System.err.println("��� CHECKPOINT E: EXCEPTION in elasticsearchService.forwardTransactionToElasticsearch() for " + t.getTxId() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            log.error("🚀🚀🚀 CHECKPOINT E: EXCEPTION in elasticsearchService.forwardTransactionToElasticsearch() for {}: {} - {}", t.getTxId(), e.getClass().getSimpleName(), e.getMessage(), e);
            e.printStackTrace();
            // Log error but don't fail the main transaction
        }

        System.out.println("🔥🔥🔥 STEP 9: INGEST COMPLETED for " + t.getTxId());
        System.err.println("🔥🔥🔥 STEP 9: INGEST COMPLETED for " + t.getTxId());
        
        } catch (Exception ex) {
            log.error("🔥🔥🔥 INGEST EXCEPTION: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
            System.err.println("🔥🔥🔥 INGEST EXCEPTION: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            throw new RuntimeException("Transaction processing failed", ex);
        }
    }
}
