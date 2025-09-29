package rs.ac.uns.acs.nais.columnar.service;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Kompenzaciona operacija za brisanje jedne transakcije na osnovu (userId, txDate, txTime).
 * Koraci:
 *  1) iz tx_by_user izvučemo merchant_id, category_id, amount_cents, tx_id
 *  2) obrišemo redove iz tx_by_user / tx_by_merchant / tx_by_category i tx_dedup
 *  3) dekrementiramo countere u *aggregates* i *_by_period* (DAY i MONTH)
 *  4) update/brisanje iz user_daily_totals
 */
@Service
@RequiredArgsConstructor
public class CompensationService {

    private final CqlTemplate cql;

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * Briše transakciju i kompenzuje sve prateće agregate.
     *
     * @return true ako je transakcija nađena i obrisana, false ako ne postoji u tx_by_user.
     */
    public boolean deleteByUserKey(UUID userId, LocalDate date, UUID timeUuid) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(timeUuid, "timeUuid");

        // 1) Učitavanje reda iz tx_by_user (da dobijemo merchant/category/amount/tx_id)
        final String selectSql =
                "SELECT tx_id, merchant_id, category_id, amount_cents " +
                "FROM tx_by_user WHERE user_id=? AND tx_date=? AND tx_time=?";

        var selectStmt = SimpleStatement.newInstance(selectSql, userId, date, timeUuid);

        List<TxRow> found = cql.query(selectStmt,
                (row, i) -> new TxRow(
                        row.getUuid("tx_id"),
                        row.getUuid("merchant_id"),
                        row.getUuid("category_id"),
                        row.getLong("amount_cents")));

        if (found == null || found.isEmpty()) {
            return false; // nema šta da brišemo
        }

        TxRow row = found.get(0);

        // 2) Fizička brisanja u sve tri tabele + dedup
        cql.execute(SimpleStatement.newInstance(
                "DELETE FROM tx_by_user WHERE user_id=? AND tx_date=? AND tx_time=?",
                userId, date, timeUuid));

        if (row.merchantId != null) {
            cql.execute(SimpleStatement.newInstance(
                    "DELETE FROM tx_by_merchant WHERE merchant_id=? AND tx_date=? AND tx_time=?",
                    row.merchantId, date, timeUuid));
        }

        if (row.categoryId != null) {
            cql.execute(SimpleStatement.newInstance(
                    "DELETE FROM tx_by_category WHERE category_id=? AND tx_date=? AND tx_time=?",
                    row.categoryId, date, timeUuid));
        }

        if (row.txId != null) {
            cql.execute(SimpleStatement.newInstance(
                    "DELETE FROM tx_dedup WHERE tx_id=?",
                    row.txId));
        }

        // 3) Counter dekrementi u agregatima
        String dayKey   = date.toString();        // yyyy-MM-dd
        String monthKey = MONTH_KEY.format(date); // yyyyMM
        long amount     = row.amountCents;
        long negAmount  = -amount;

        if (row.merchantId != null) {
            // merchant_aggregates
            cql.execute(SimpleStatement.newInstance(
                    "UPDATE merchant_aggregates SET tx_count = tx_count + (-1), amount_cents = amount_cents + ? " +
                    "WHERE merchant_id=? AND period='DAY' AND period_key=?",
                    negAmount, row.merchantId, dayKey));

            cql.execute(SimpleStatement.newInstance(
                    "UPDATE merchant_aggregates SET tx_count = tx_count + (-1), amount_cents = amount_cents + ? " +
                    "WHERE merchant_id=? AND period='MONTH' AND period_key=?",
                    negAmount, row.merchantId, monthKey));

            // merchant_aggregates_by_period (GLOBAL)
            cql.execute(SimpleStatement.newInstance(
                    "UPDATE merchant_aggregates_by_period SET tx_count = tx_count + (-1), amount_cents = amount_cents + ? " +
                    "WHERE period='DAY' AND period_key=? AND merchant_id=?",
                    negAmount, dayKey, row.merchantId));

            cql.execute(SimpleStatement.newInstance(
                    "UPDATE merchant_aggregates_by_period SET tx_count = tx_count + (-1), amount_cents = amount_cents + ? " +
                    "WHERE period='MONTH' AND period_key=? AND merchant_id=?",
                    negAmount, monthKey, row.merchantId));
        }

        if (row.categoryId != null) {
            // category_aggregates
            cql.execute(SimpleStatement.newInstance(
                    "UPDATE category_aggregates SET tx_count = tx_count + (-1), amount_cents = amount_cents + ? " +
                    "WHERE category_id=? AND period='DAY' AND period_key=?",
                    negAmount, row.categoryId, dayKey));

            cql.execute(SimpleStatement.newInstance(
                    "UPDATE category_aggregates SET tx_count = tx_count + (-1), amount_cents = amount_cents + ? " +
                    "WHERE category_id=? AND period='MONTH' AND period_key=?",
                    negAmount, row.categoryId, monthKey));

            // category_aggregates_by_period (GLOBAL)
            cql.execute(SimpleStatement.newInstance(
                    "UPDATE category_aggregates_by_period SET tx_count = tx_count + (-1), amount_cents = amount_cents + ? " +
                    "WHERE period='DAY' AND period_key=? AND category_id=?",
                    negAmount, dayKey, row.categoryId));

            cql.execute(SimpleStatement.newInstance(
                    "UPDATE category_aggregates_by_period SET tx_count = tx_count + (-1), amount_cents = amount_cents + ? " +
                    "WHERE period='MONTH' AND period_key=? AND category_id=?",
                    negAmount, monthKey, row.categoryId));
        }

        // 4) user_daily_totals (redukcija broja i sume za taj dan)
        var udtSelect = SimpleStatement.newInstance(
                "SELECT day_count, day_amount_cents FROM user_daily_totals WHERE user_id=? AND tx_date=?",
                userId, date);

        List<UserDaily> daily = cql.query(udtSelect,
                (r, i) -> new UserDaily(r.getInt("day_count"), r.getLong("day_amount_cents")));

        if (daily != null && !daily.isEmpty()) {
            int  oldCnt = daily.get(0).count;
            long oldSum = daily.get(0).sum;

            int  newCnt = Math.max(0, oldCnt - 1);
            long newSum = Math.max(0L, oldSum - amount);

            if (newCnt == 0 && newSum == 0L) {
                cql.execute(SimpleStatement.newInstance(
                        "DELETE FROM user_daily_totals WHERE user_id=? AND tx_date=?",
                        userId, date));
            } else {
                cql.execute(SimpleStatement.newInstance(
                        "UPDATE user_daily_totals SET day_count=?, day_amount_cents=? WHERE user_id=? AND tx_date=?",
                        newCnt, newSum, userId, date));
            }
        }

        return true;
    }

    // --- helper DTO-i za mapiranje rezultata ---

    private static final class TxRow {
        final UUID txId;
        final UUID merchantId;
        final UUID categoryId;
        final long amountCents;

        TxRow(UUID txId, UUID merchantId, UUID categoryId, long amountCents) {
            this.txId = txId;
            this.merchantId = merchantId;
            this.categoryId = categoryId;
            this.amountCents = amountCents;
        }
    }

    private static final class UserDaily {
        final int count;
        final long sum;

        UserDaily(int count, long sum) {
            this.count = count;
            this.sum = sum;
        }
    }
}
