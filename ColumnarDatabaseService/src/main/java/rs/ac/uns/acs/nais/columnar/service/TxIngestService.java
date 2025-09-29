package rs.ac.uns.acs.nais.columnar.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.mapper.TxMapper;
import rs.ac.uns.acs.nais.columnar.model.UserDailyTotal;
import rs.ac.uns.acs.nais.columnar.repo.*;

@Service
@RequiredArgsConstructor
public class TxIngestService {

    private final TxByUserRepo userRepo;
    private final TxByMerchantRepo merchantRepo;
    private final TxByCategoryRepo categoryRepo;
    private final UserDailyTotalsRepo dailyRepo;
    private final CqlTemplate cql;
    private final Supplier<UUID> timeUuidSupplier;
    private final DedupService dedupService;

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    @Transactional
    public void ingest(TransactionDTO t) throws DataAccessException {
        // 0) idempotencija: preskoči ako je već viđeno
        if (!dedupService.tryMarkSeen(t.getTxId())) {
            return; // duplikat
        }

        // 1) ključni delovi
        LocalDate date = TxMapper.toUtcDate(t.getOccurredAt());
        UUID timeUuid = timeUuidSupplier.get();
        String dayKey = date.toString();          // YYYY-MM-DD
        String monthKey = MONTH.format(date);     // YYYYMM

        // 2) denormalizovani upis
        userRepo.save(TxMapper.toUserEntity(t, date, timeUuid));
        merchantRepo.save(TxMapper.toMerchantEntity(t, date, timeUuid));
        categoryRepo.save(TxMapper.toCategoryEntity(t, date, timeUuid));

        // 3) user_daily_totals (obični brojači)
        UserDailyTotal row = dailyRepo.findOneDay(t.getUserId(), date)
                .orElse(new UserDailyTotal(t.getUserId(), date, 0, 0L));
        row.setDayCount((row.getDayCount()==null?0:row.getDayCount()) + 1);
        row.setDayAmountCents((row.getDayAmountCents()==null?0L:row.getDayAmountCents()) + t.getAmountCents());
        dailyRepo.save(row);

        // 4) counter agregati (po entitetu)
        cql.execute("UPDATE merchant_aggregates SET tx_count = tx_count + 1, amount_cents = amount_cents + ? " +
                "WHERE merchant_id=? AND period='DAY' AND period_key=?",
                t.getAmountCents(), t.getMerchantId(), dayKey);
        cql.execute("UPDATE merchant_aggregates SET tx_count = tx_count + 1, amount_cents = amount_cents + ? " +
                "WHERE merchant_id=? AND period='MONTH' AND period_key=?",
                t.getAmountCents(), t.getMerchantId(), monthKey);

        cql.execute("UPDATE category_aggregates SET tx_count = tx_count + 1, amount_cents = amount_cents + ? " +
                "WHERE category_id=? AND period='DAY' AND period_key=?",
                t.getAmountCents(), t.getCategoryId(), dayKey);
        cql.execute("UPDATE category_aggregates SET tx_count = tx_count + 1, amount_cents = amount_cents + ? " +
                "WHERE category_id=? AND period='MONTH' AND period_key=?",
                t.getAmountCents(), t.getCategoryId(), monthKey);

        // 5) GLOBAL by_period (za top-N upite)
        cql.execute("UPDATE merchant_aggregates_by_period SET tx_count=tx_count+1, amount_cents=amount_cents+? " +
                "WHERE period='DAY' AND period_key=? AND merchant_id=?",
                t.getAmountCents(), dayKey, t.getMerchantId());
        cql.execute("UPDATE merchant_aggregates_by_period SET tx_count=tx_count+1, amount_cents=amount_cents+? " +
                "WHERE period='MONTH' AND period_key=? AND merchant_id=?",
                t.getAmountCents(), monthKey, t.getMerchantId());

        cql.execute("UPDATE category_aggregates_by_period SET tx_count=tx_count+1, amount_cents=amount_cents+? " +
                "WHERE period='DAY' AND period_key=? AND category_id=?",
                t.getAmountCents(), dayKey, t.getCategoryId());
        cql.execute("UPDATE category_aggregates_by_period SET tx_count=tx_count+1, amount_cents=amount_cents+? " +
                "WHERE period='MONTH' AND period_key=? AND category_id=?",
                t.getAmountCents(), monthKey, t.getCategoryId());
    }
}
