package rs.ac.uns.acs.nais.columnar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.acs.nais.columnar.model.*;
import rs.ac.uns.acs.nais.columnar.repo.UserDailyTotalsRepo;
import rs.ac.uns.acs.nais.columnar.repo.TxByCategoryRepo;
import rs.ac.uns.acs.nais.columnar.repo.TxByMerchantRepo;
import rs.ac.uns.acs.nais.columnar.repo.TxByUserRepo;
import rs.ac.uns.acs.nais.columnar.util.TimeUtil;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompensationService {

    private final TxByUserRepo userRepo;
    private final TxByMerchantRepo merchantRepo;
    private final TxByCategoryRepo categoryRepo;
    private final UserDailyTotalsRepo dailyRepo;
    private final CqlTemplate cql;

    /**
     * Briše transakciju (na osnovu user ključa) i kompenzuje sve agregate.
     * @param userId  particioni ključ 1
     * @param txDate  particioni ključ 2
     * @param txTime  timeuuid (clustering)
     */
    @Transactional
    public boolean deleteByUserKey(UUID userId, LocalDate txDate, UUID txTime) {
        // 1) Nađi bazni zapis (iz user tabele)
        TxByUserKey key = TxByUserKey.builder()
                .userId(userId).txDate(txDate).txTime(txTime).build();
        Optional<TxByUser> opt = userRepo.findById(key);
        if (opt.isEmpty()) return false;

        TxByUser row = opt.get();

        // 2) Briši iz sve 3 denorm tabele
        userRepo.deleteById(key);

        var mKey = TxByMerchantKey.builder()
                .merchantId(row.getMerchantId()).txDate(txDate).txTime(txTime).build();
        merchantRepo.deleteById(mKey);

        var cKey = TxByCategoryKey.builder()
                .categoryId(row.getCategoryId()).txDate(txDate).txTime(txTime).build();
        categoryRepo.deleteById(cKey);

        // 3) Kompenzuj agregate
        long amount = row.getAmountCents() == null ? 0L : row.getAmountCents();
        String dayKey = TimeUtil.toDayKey(txDate);
        String monthKey = TimeUtil.toMonthKey(txDate);

        // 3a) user_daily_totals
        UserDailyTotal daily = dailyRepo.findOneDay(userId, txDate)
                .orElse(new UserDailyTotal(userId, txDate, 0, 0L));
        int newCount = Math.max(0, (daily.getDayCount() == null ? 0 : daily.getDayCount()) - 1);
        long newAmount = Math.max(0L, (daily.getDayAmountCents() == null ? 0L : daily.getDayAmountCents()) - amount);
        daily.setDayCount(newCount);
        daily.setDayAmountCents(newAmount);
        dailyRepo.save(daily);

        // 3b) merchant/category counter agregati (negativne kompenzacije)
        cql.execute("UPDATE merchant_aggregates SET tx_count = tx_count - 1, amount_cents = amount_cents - ? " +
                "WHERE merchant_id=? AND period='DAY' AND period_key=?",
                amount, row.getMerchantId(), dayKey);
        cql.execute("UPDATE merchant_aggregates SET tx_count = tx_count - 1, amount_cents = amount_cents - ? " +
                "WHERE merchant_id=? AND period='MONTH' AND period_key=?",
                amount, row.getMerchantId(), monthKey);

        cql.execute("UPDATE category_aggregates SET tx_count = tx_count - 1, amount_cents = amount_cents - ? " +
                "WHERE category_id=? AND period='DAY' AND period_key=?",
                amount, row.getCategoryId(), dayKey);
        cql.execute("UPDATE category_aggregates SET tx_count = tx_count - 1, amount_cents = amount_cents - ? " +
                "WHERE category_id=? AND period='MONTH' AND period_key=?",
                amount, row.getCategoryId(), monthKey);

        // 3c) GLOBAL by_period agregati (za top-N)
        cql.execute("UPDATE merchant_aggregates_by_period SET tx_count = tx_count - 1, amount_cents = amount_cents - ? " +
                "WHERE period='DAY' AND period_key=? AND merchant_id=?",
                amount, dayKey, row.getMerchantId());
        cql.execute("UPDATE merchant_aggregates_by_period SET tx_count = tx_count - 1, amount_cents = amount_cents - ? " +
                "WHERE period='MONTH' AND period_key=? AND merchant_id=?",
                amount, monthKey, row.getMerchantId());

        cql.execute("UPDATE category_aggregates_by_period SET tx_count = tx_count - 1, amount_cents = amount_cents - ? " +
                "WHERE period='DAY' AND period_key=? AND category_id=?",
                amount, dayKey, row.getCategoryId());
        cql.execute("UPDATE category_aggregates_by_period SET tx_count = tx_count - 1, amount_cents = amount_cents - ? " +
                "WHERE period='MONTH' AND period_key=? AND category_id=?",
                amount, monthKey, row.getCategoryId());

        return true;
    }
}
