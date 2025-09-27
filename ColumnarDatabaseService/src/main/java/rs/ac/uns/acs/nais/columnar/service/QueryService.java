package rs.ac.uns.acs.nais.columnar.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.columnar.dto.TopEntryDTO;
import rs.ac.uns.acs.nais.columnar.dto.UserDayTotalsDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByCategory;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.repo.*;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final TxByUserRepo userRepo;
    private final TxByMerchantRepo merchantRepo;
    private final TxByCategoryRepo categoryRepo;
    private final UserDailyTotalsRepo dailyRepo;
    private final CqlSession session;

    public List<TxByUser> getUserTransactionsToday(UUID userId, int limit) {
        LocalDate todayUtc = Instant.now().atOffset(ZoneOffset.UTC).toLocalDate();
        return userRepo.findDay(userId, todayUtc, limit);
    }

    public List<UserDayTotalsDTO> getUserDailyTotals(UUID userId, LocalDate from, LocalDate to) {
        return dailyRepo.findRange(userId, from, to).stream()
                .sorted(Comparator.comparing(u -> u.getTxDate()))
                .map(u -> new UserDayTotalsDTO(u.getTxDate(),
                        Optional.ofNullable(u.getDayCount()).orElse(0),
                        Optional.ofNullable(u.getDayAmountCents()).orElse(0L)))
                .collect(Collectors.toList());
    }

    public List<TopEntryDTO> getTopMerchantsByMonth(String yyyyMM, int limit) {
        PreparedStatement ps = session.prepare(
                "SELECT merchant_id, tx_count, amount_cents " +
                "FROM merchant_aggregates_by_period " +
                "WHERE period='MONTH' AND period_key=?");
        ResultSet rs = session.execute(ps.bind(yyyyMM));
        List<TopEntryDTO> list = new ArrayList<>();
        for (Row r : rs) {
            list.add(new TopEntryDTO(
                    r.getUuid("merchant_id"),
                    null,
                    r.getLong("tx_count"),
                    r.getLong("amount_cents")));
        }
        return list.stream()
                .sorted(Comparator.comparingLong(TopEntryDTO::getAmountCents).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<TopEntryDTO> getTopCategoriesByMonth(String yyyyMM, int limit) {
        PreparedStatement ps = session.prepare(
                "SELECT category_id, tx_count, amount_cents " +
                "FROM category_aggregates_by_period " +
                "WHERE period='MONTH' AND period_key=?");
        ResultSet rs = session.execute(ps.bind(yyyyMM));
        List<TopEntryDTO> list = new ArrayList<>();
        for (Row r : rs) {
            list.add(new TopEntryDTO(
                    r.getUuid("category_id"),
                    null,
                    r.getLong("tx_count"),
                    r.getLong("amount_cents")));
        }
        return list.stream()
                .sorted(Comparator.comparingLong(TopEntryDTO::getAmountCents).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<TopEntryDTO> getMostFrequentMerchantsForCategory(UUID categoryId, LocalDate date, int limit) {
        List<TxByCategory> dayRows = categoryRepo.findDay(categoryId, date, 10000); // grubi limit
        Map<UUID, long[]> agg = new HashMap<>(); // merchantId -> [count, amount]
        for (var r : dayRows) {
            var key = r.getMerchantId();
            agg.computeIfAbsent(key, k -> new long[]{0,0});
            agg.get(key)[0] += 1;
            agg.get(key)[1] += Optional.ofNullable(r.getAmountCents()).orElse(0L);
        }
        return agg.entrySet().stream()
                .map(e -> new TopEntryDTO(e.getKey(), null, e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingLong(TopEntryDTO::getTxCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getAverageSpendingForUser(UUID userId, int days) {
        LocalDate to = Instant.now().atOffset(ZoneOffset.UTC).toLocalDate();
        LocalDate from = to.minusDays(days - 1L);
        var list = dailyRepo.findRange(userId, from, to);
        long sum = list.stream().mapToLong(v -> Optional.ofNullable(v.getDayAmountCents()).orElse(0L)).sum();
        int daysWithData = Math.max(1, list.size());
        long avg = sum / daysWithData;
        return Map.of("userId", userId, "days", days, "averageAmountCents", avg);
    }
}
