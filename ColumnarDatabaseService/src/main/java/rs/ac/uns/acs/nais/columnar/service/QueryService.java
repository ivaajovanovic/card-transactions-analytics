package rs.ac.uns.acs.nais.columnar.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Service;

import rs.ac.uns.acs.nais.columnar.dto.TopEntryDTO;
import rs.ac.uns.acs.nais.columnar.dto.UserDayTotalsDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.repo.TxByUserRepo;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final TxByUserRepo userRepo;
    private final CqlTemplate cql;

    public List<TxByUser> getUserTransactionsToday(UUID userId, int limit) {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        return userRepo.findDay(userId, todayUtc, limit);
    }

    public List<UserDayTotalsDTO> getUserDailyTotals(UUID userId, LocalDate from, LocalDate to) {
        String sql = "SELECT tx_date, day_count, day_amount_cents " +
                     "FROM user_daily_totals WHERE user_id=? AND tx_date >= ? AND tx_date <= ?";

        // Vraćamo kako dođe iz baze (clustering je DESC po tx_date); nema oslanjanja na nepostojeći getter.
        return cql.query(sql, ps -> ps.bind(userId, from, to),
            (row, idx) -> new UserDayTotalsDTO(
                row.getLocalDate("tx_date"),
                row.getInt("day_count"),
                row.getLong("day_amount_cents"))
        );
    }

    public Map<String, Object> getAverageSpendingForUser(UUID userId, int days) {
        LocalDate to = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = to.minusDays(days - 1L);

        String sql = "SELECT day_count, day_amount_cents " +
                     "FROM user_daily_totals WHERE user_id=? AND tx_date >= ? AND tx_date <= ?";

        List<Map<String, Object>> agg = cql.query(sql, ps -> ps.bind(userId, from, to),
            (row, idx) -> {
                Map<String, Object> m = new HashMap<>();
                m.put("cnt", row.getInt("day_count"));
                m.put("sum", row.getLong("day_amount_cents"));
                return m;
            });

        long totalCents = agg.stream().mapToLong(m -> (Long) m.get("sum")).sum();
        long txCount    = agg.stream().mapToLong(m -> (Integer) m.get("cnt")).sum();
        long daysWith   = agg.size();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", userId);
        out.put("days", days);
        out.put("from", from.toString());
        out.put("to", to.toString());
        out.put("totalCents", totalCents);
        out.put("txCount", txCount);
        out.put("daysWithData", daysWith);
        out.put("avgPerDayCents", daysWith == 0 ? 0 : totalCents / daysWith);
        out.put("avgPerTxCents", txCount == 0 ? 0 : totalCents / txCount);
        return out;
    }

    public List<TopEntryDTO> getTopMerchantsByMonth(String month, int limit) {
        String sql = "SELECT merchant_id, tx_count, amount_cents " +
                     "FROM merchant_aggregates_by_period WHERE period='MONTH' AND period_key=?";
        List<TopEntryDTO> all = cql.query(sql, ps -> ps.bind(month), (row, i) ->
            TopEntryDTO.builder()
                .id(row.getUuid("merchant_id"))
                .label(null)
                .txCount(row.getLong("tx_count"))
                .amountCents(row.getLong("amount_cents"))
                .build()
        );
        return all.stream()
                  .sorted(Comparator.comparing(TopEntryDTO::getAmountCents).reversed())
                  .limit(limit)
                  .collect(Collectors.toList());
        }

    public List<TopEntryDTO> getTopCategoriesByMonth(String month, int limit) {
        String sql = "SELECT category_id, tx_count, amount_cents " +
                     "FROM category_aggregates_by_period WHERE period='MONTH' AND period_key=?";
        List<TopEntryDTO> all = cql.query(sql, ps -> ps.bind(month), (row, i) ->
            TopEntryDTO.builder()
                .id(row.getUuid("category_id"))
                .label(null)
                .txCount(row.getLong("tx_count"))
                .amountCents(row.getLong("amount_cents"))
                .build()
        );
        return all.stream()
                  .sorted(Comparator.comparing(TopEntryDTO::getAmountCents).reversed())
                  .limit(limit)
                  .collect(Collectors.toList());
    }

    public List<TopEntryDTO> getMostFrequentMerchantsForCategory(UUID categoryId, LocalDate date, int limit) {
        String sql = "SELECT merchant_id, amount_cents FROM tx_by_category WHERE category_id=? AND tx_date=? LIMIT 10000";
        Map<UUID, long[]> acc = new HashMap<>(); // [0]=count, [1]=sumCents

        cql.query(sql, ps -> ps.bind(categoryId, date), (row, i) -> {
            UUID m   = row.getUuid("merchant_id");
            long amt = row.getLong("amount_cents");
            long[] a = acc.computeIfAbsent(m, k -> new long[]{0L, 0L});
            a[0]++; a[1] += amt;
            return null;
        });

        return acc.entrySet().stream()
            .map(e -> TopEntryDTO.builder()
                    .id(e.getKey())
                    .label(null)
                    .txCount(e.getValue()[0])
                    .amountCents(e.getValue()[1])
                    .build())
            .sorted(Comparator.<TopEntryDTO>comparingLong(TopEntryDTO::getTxCount).reversed()
                    .thenComparing(Comparator.comparingLong(TopEntryDTO::getAmountCents).reversed()))
            .limit(limit)
            .collect(Collectors.toList());
    }
}
