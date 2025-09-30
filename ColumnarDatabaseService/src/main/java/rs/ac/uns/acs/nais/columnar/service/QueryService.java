package rs.ac.uns.acs.nais.columnar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Service;

import rs.ac.uns.acs.nais.columnar.dto.TopEntryDTO;
import rs.ac.uns.acs.nais.columnar.dto.UserDayTotalsDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.repo.TxByUserRepo;

@Service
@RequiredArgsConstructor
@Slf4j
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
        // Koristi merchant_aggregates_by_period tabelu za bolje performanse
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
        // Koristi category_aggregates_by_period tabelu za bolje performanse
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
        String sql = "SELECT merchant_id, amount_cents FROM transactions_by_category WHERE category_id=? AND tx_date=? LIMIT 10000";
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

    /**
     * SLOŽENI UPIT 1: Najprodavaniji proizvodi po kategoriji sa skladištem
     * Analizira sve transakcije iz baze, grupise po merchant_id (kao proizvod)
     * i računa kompleksne statistike uključujući simulaciju skladišta
     */
    public List<Map<String, Object>> getTopProductsByCategory(UUID categoryId, int limit) {
        String sql = "SELECT merchant_id, amount_cents, status FROM transactions_by_category " +
                     "WHERE category_id=? ALLOW FILTERING";
        
        try {
            log.error("🔍🔍🔍 DEBUG: Executing query for categoryId: " + categoryId);
            log.error("🔍🔍🔍 DEBUG: SQL = " + sql);
            
            // FAZA 1: Dobij sve transakcije za kategoriju
            List<Map<String, Object>> transactions = cql.query(sql, ps -> ps.bind(categoryId),
                (row, idx) -> {
                    Map<String, Object> tx = new HashMap<>();
                    tx.put("merchant_id", row.getUuid("merchant_id"));
                    tx.put("amount_cents", row.getLong("amount_cents"));
                    tx.put("status", row.getString("status"));
                    return tx;
                });
            
            log.error("🔍🔍🔍 DEBUG: Found " + transactions.size() + " transactions for category " + categoryId);
            
            // FAZA 2: Grupiši po merchant_id i računaj statistike
            Map<UUID, Map<String, Object>> merchantStats = new HashMap<>();
            
            for (Map<String, Object> tx : transactions) {
                UUID merchantId = (UUID) tx.get("merchant_id");
                long amountCents = (Long) tx.get("amount_cents");
                String status = (String) tx.get("status");
                
                merchantStats.computeIfAbsent(merchantId, k -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("merchant_id", k);
                    stats.put("product_name", "Proizvod-" + k.toString().substring(0, 8));
                    stats.put("category_id", categoryId);
                    stats.put("total_transactions", 0L);
                    stats.put("total_amount_cents", 0L);
                    stats.put("success_count", 0L);
                    stats.put("failed_count", 0L);
                    stats.put("base_stock", 0L);
                    return stats;
                });
                
                Map<String, Object> stats = merchantStats.get(merchantId);
                stats.put("total_transactions", (Long) stats.get("total_transactions") + 1);
                stats.put("total_amount_cents", (Long) stats.get("total_amount_cents") + amountCents);
                
                if ("SUCCESS".equals(status)) {
                    stats.put("success_count", (Long) stats.get("success_count") + 1);
                    // Simulacija: uspešne transakcije = prodaja
                    stats.put("base_stock", (Long) stats.get("base_stock") + (amountCents / 500));
                } else {
                    stats.put("failed_count", (Long) stats.get("failed_count") + 1);
                }
            }
            
            // FAZA 3: Kalkuliši finalne metrike
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> stats : merchantStats.values()) {
                long totalTx = (Long) stats.get("total_transactions");
                long totalAmount = (Long) stats.get("total_amount_cents");
                long successCount = (Long) stats.get("success_count");
                long baseStock = (Long) stats.get("base_stock");
                
                // Složene kalkulacije
                double avgAmount = totalTx > 0 ? (double) totalAmount / totalTx : 0.0;
                double successRate = totalTx > 0 ? (double) successCount / totalTx * 100 : 0.0;
                long availableStock = Math.max(10, baseStock - (successCount / 2)); // Min 10 jedinica
                double popularityScore = successCount * avgAmount / 10000;
                
                stats.put("avg_amount_rsd", avgAmount / 100.0);
                stats.put("success_rate_percent", successRate);
                stats.put("available_stock_quantity", availableStock);
                stats.put("popularity_score", popularityScore);
                stats.put("total_amount_rsd", totalAmount / 100.0);
                
                results.add(stats);
            }
            
            // FAZA 4: Sortiraj po dostupnosti skladišta (kao traženo)
            return results.stream()
                .sorted((r1, r2) -> Long.compare(
                    (Long) r2.get("available_stock_quantity"), 
                    (Long) r1.get("available_stock_quantity")))
                .limit(limit)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * SLOŽENI UPIT 2: Kompleksna analiza transakcijskih trendova
     * Analizira sve transakcije za period i kreira detaljnu analizu
     * PLUS celokupna istorija transakcija
     */
    public Map<String, Object> getAdvancedTransactionAnalysis(LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> analysis = new HashMap<>();
        
        log.error("🔥🔥🔥 NOVA IMPLEMENTACIJA POZVANA! Enhanced analytics sa 4 sekcije");
        
        try {
            // FAZA 1A: Analiza za zadati period (30 dana)
            String userSql = "SELECT user_id, tx_date, day_count, day_amount_cents " +
                           "FROM user_daily_totals WHERE tx_date >= ? AND tx_date <= ? ALLOW FILTERING";
            
            // FAZA 1B: Analiza za CELOKUPNU ISTORIJU
            String historicalSql = "SELECT user_id, tx_date, day_count, day_amount_cents " +
                                 "FROM user_daily_totals ALLOW FILTERING";
            
            // IZVRŠAVANJE UPITA ZA PERIOD (30 dana)
            List<Map<String, Object>> userDailyData = cql.query(userSql, 
                ps -> ps.bind(fromDate, toDate),
                (row, idx) -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("user_id", row.getUuid("user_id"));
                    data.put("tx_date", row.getLocalDate("tx_date"));
                    data.put("day_count", row.getInt("day_count"));
                    data.put("day_amount_cents", row.getLong("day_amount_cents"));
                    return data;
                });
            
            // IZVRŠAVANJE UPITA ZA CELOKUPNU ISTORIJU
            List<Map<String, Object>> historicalData = cql.query(historicalSql, 
                (row, idx) -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("user_id", row.getUuid("user_id"));
                    data.put("tx_date", row.getLocalDate("tx_date"));
                    data.put("day_count", row.getInt("day_count"));
                    data.put("day_amount_cents", row.getLong("day_amount_cents"));
                    return data;
                });
            
            // FAZA 2A: PROSTA SEKCIJA - OSNOVNE STATISTIKE
            Map<String, Object> basicStats = new HashMap<>();
            basicStats.put("total_transactions_in_system", historicalData.stream().mapToInt(d -> (Integer) d.get("day_count")).sum());
            basicStats.put("total_amount_in_system_rsd", historicalData.stream().mapToLong(d -> (Long) d.get("day_amount_cents")).sum() / 100.0);
            basicStats.put("oldest_transaction_date", historicalData.stream().map(d -> (LocalDate) d.get("tx_date")).min(LocalDate::compareTo).orElse(null));
            basicStats.put("newest_transaction_date", historicalData.stream().map(d -> (LocalDate) d.get("tx_date")).max(LocalDate::compareTo).orElse(null));
            basicStats.put("active_days_total", historicalData.size());
            basicStats.put("unique_users_total", historicalData.stream().map(d -> (UUID) d.get("user_id")).distinct().count());
            
            // FAZA 2B: PROSTA SEKCIJA - POREĐENJE PERIODA
            Map<String, Object> comparison = new HashMap<>();
            double currentPeriodTotal = userDailyData.stream().mapToLong(d -> (Long) d.get("day_amount_cents")).sum() / 100.0;
            double historicalTotal = historicalData.stream().mapToLong(d -> (Long) d.get("day_amount_cents")).sum() / 100.0;
            long currentPeriodTx = userDailyData.stream().mapToInt(d -> (Integer) d.get("day_count")).sum();
            long historicalTx = historicalData.stream().mapToInt(d -> (Integer) d.get("day_count")).sum();
            
            comparison.put("current_period_percentage_of_total", historicalTotal > 0 ? (currentPeriodTotal / historicalTotal) * 100 : 0);
            comparison.put("current_period_tx_percentage", historicalTx > 0 ? (double) currentPeriodTx / historicalTx * 100 : 0);
            comparison.put("period_vs_historical_activity_ratio", historicalTx > 0 ? (double) currentPeriodTx / (historicalTx - currentPeriodTx) : 0);
            comparison.put("is_current_period_above_average", currentPeriodTotal > (historicalTotal / Math.max(1, historicalData.size() / Math.max(1, userDailyData.size()))));
            
            // FAZA 2C: SLOŽENA ANALIZA TRENUTNOG PERIODA (30 dana)
            Map<String, Object> periodAnalysis = analyzeDataSet(userDailyData, "current_period");
            
            // FAZA 2D: SLOŽENA ANALIZA CELOKUPNE ISTORIJE
            Map<String, Object> historicalAnalysis = analyzeDataSet(historicalData, "historical_overview");
            
            // FAZA 3: Kombinovanje rezultata
            // FAZA 3: Kombinovanje rezultata
            analysis.put("basic_statistics", basicStats);
            analysis.put("period_comparison", comparison);
            analysis.put("current_period", periodAnalysis);
            analysis.put("historical_overview", historicalAnalysis);
            analysis.put("analysis_type", "enhanced_transaction_analytics");
            analysis.put("requested_from", fromDate.toString());
            analysis.put("requested_to", toDate.toString());
            analysis.put("analysis_period", fromDate.toString() + " do " + toDate.toString());
            analysis.put("analysis_timestamp", java.time.Instant.now().toString());
            
            return analysis;
            
        } catch (Exception e) {
            log.error("Error in getAdvancedTransactionAnalysis", e);
            return Collections.emptyMap();
        }
    }
    
    // HELPER METODA za analizu dataset-a
    private Map<String, Object> analyzeDataSet(List<Map<String, Object>> dataset, String analysisType) {
        Map<String, Object> result = new HashMap<>();
        
        // Grupiši i analiziraj po korisnicima
        Map<UUID, Map<String, Object>> userAnalysis = new HashMap<>();
        int totalDaysAnalyzed = 0;
        long totalTransactionsAnalyzed = 0;
        double totalAmountAnalyzed = 0.0;
        
        for (Map<String, Object> dailyData : dataset) {
            UUID userId = (UUID) dailyData.get("user_id");
            int dayCount = (Integer) dailyData.get("day_count");
            long dayAmount = (Long) dailyData.get("day_amount_cents");
            
            totalDaysAnalyzed++;
            totalTransactionsAnalyzed += dayCount;
            totalAmountAnalyzed += dayAmount / 100.0;
            
            userAnalysis.computeIfAbsent(userId, k -> {
                Map<String, Object> userStats = new HashMap<>();
                userStats.put("user_id", k);
                userStats.put("user_name", "Korisnik-" + k.toString().substring(0, 8));
                userStats.put("total_transactions", 0L);
                userStats.put("total_amount_rsd", 0.0);
                userStats.put("active_days", 0);
                userStats.put("max_daily_amount", 0.0);
                userStats.put("max_daily_transactions", 0);
                return userStats;
            });
            
            Map<String, Object> userStats = userAnalysis.get(userId);
            userStats.put("total_transactions", (Long) userStats.get("total_transactions") + dayCount);
            userStats.put("total_amount_rsd", (Double) userStats.get("total_amount_rsd") + dayAmount / 100.0);
            userStats.put("active_days", (Integer) userStats.get("active_days") + 1);
            
            // Računaj maksimume
            double dailyAmountRsd = dayAmount / 100.0;
            if (dailyAmountRsd > (Double) userStats.get("max_daily_amount")) {
                userStats.put("max_daily_amount", dailyAmountRsd);
            }
            if (dayCount > (Integer) userStats.get("max_daily_transactions")) {
                userStats.put("max_daily_transactions", dayCount);
            }
        }
        
        // Kalkuliši finalne metrike za korisnike
        List<Map<String, Object>> topUsers = new ArrayList<>();
        for (Map<String, Object> userStats : userAnalysis.values()) {
            long totalTx = (Long) userStats.get("total_transactions");
            double totalAmount = (Double) userStats.get("total_amount_rsd");
            int activeDays = (Integer) userStats.get("active_days");
            
            if (totalTx > 0) {
                userStats.put("avg_transaction_value", totalAmount / totalTx);
                userStats.put("avg_daily_transactions", activeDays > 0 ? (double) totalTx / activeDays : 0.0);
                userStats.put("avg_daily_amount", activeDays > 0 ? totalAmount / activeDays : 0.0);
                userStats.put("activity_intensity", totalTx * totalAmount / 1000); // Kombinovani skor
                
                topUsers.add(userStats);
            }
        }
        
        // Sortiraj i ograniči top korisnike
        List<Map<String, Object>> finalTopUsers = topUsers.stream()
            .sorted((u1, u2) -> Double.compare(
                (Double) u2.get("activity_intensity"), 
                (Double) u1.get("activity_intensity")))
            .limit(15)
            .collect(Collectors.toList());
        
        // Kreiraj konačnu analizu
        result.put("analysis_type", analysisType);
        result.put("total_days_with_data", totalDaysAnalyzed);
        result.put("total_transactions_analyzed", totalTransactionsAnalyzed);
        result.put("total_amount_analyzed_rsd", totalAmountAnalyzed);
        result.put("average_transaction_value", 
            totalTransactionsAnalyzed > 0 ? totalAmountAnalyzed / totalTransactionsAnalyzed : 0.0);
        result.put("unique_active_users", userAnalysis.size());
        result.put("average_transactions_per_day", 
            totalDaysAnalyzed > 0 ? (double) totalTransactionsAnalyzed / totalDaysAnalyzed : 0.0);
        result.put("top_users_by_activity", finalTopUsers);
        
        return result;
    }
    
    // ================================
    // UPDATE OPERATIONS FOR KEY TABLES
    // ================================
    
    /**
     * UPDATE operacije za transactions_by_user tabelu
     * Cassandra UPSERT - ako ne postoji, kreira se; ako postoji, ažurira se
     */
    public boolean updateTransactionAmount(UUID userId, LocalDate transactionDate, UUID transactionId, 
                                         double newAmount, String currency) {
        log.info("=== UPDATE TRANSACTION AMOUNT === user_id: {}, date: {}, tx_id: {}, new_amount: {} {}", 
                userId, transactionDate, transactionId, newAmount, currency);
        
        try {
            // Konvertuj amount u cente
            long amountCents = Math.round(newAmount * 100);
            
            // UPSERT sa INSERT (Cassandra semantika)
            String sql = "INSERT INTO transactions_by_user " +
                        "(user_id, tx_date, tx_time, tx_id, card_id, merchant_id, category_id, " +
                        "amount_cents, currency, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            // Generiši potrebne UUID-jeve
            UUID timeUuid = com.datastax.oss.driver.api.core.uuid.Uuids.timeBased();
            UUID cardId = java.util.UUID.randomUUID();
            UUID merchantId = java.util.UUID.randomUUID();
            UUID categoryId = java.util.UUID.randomUUID();
            
            cql.execute(sql, userId, transactionDate, timeUuid, transactionId, cardId, 
                       merchantId, categoryId, amountCents, currency, "PENDING");
            
            log.info("Successfully inserted/updated transaction amount: {} cents", amountCents);
            return true;
        } catch (Exception e) {
            log.error("Error updating transaction amount: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * UPDATE status transakcije u svim relevantnim tabelama
     */
    public boolean updateTransactionStatus(UUID userId, String merchantId, String categoryId, 
                                         LocalDate transactionDate, UUID transactionId, String newStatus) {
        log.info("=== UPDATE TRANSACTION STATUS === tx_id: {}, new_status: {}", transactionId, newStatus);
        
        try {
            UUID timeUuid = com.datastax.oss.driver.api.core.uuid.Uuids.timeBased();
            UUID merchantUuid = UUID.fromString(merchantId);
            UUID categoryUuid = UUID.fromString(categoryId);
            
            // UPSERT u transactions_by_user (potreban tx_time za clustering key)
            String sqlUser = "UPDATE transactions_by_user SET status = ? " +
                           "WHERE user_id = ? AND tx_date = ? AND tx_time = ? AND tx_id = ?";
            cql.execute(sqlUser, newStatus, userId, transactionDate, timeUuid, transactionId);
            
            // UPSERT u transactions_by_merchant
            String sqlMerchant = "UPDATE transactions_by_merchant SET status = ? " +
                               "WHERE merchant_id = ? AND tx_date = ? AND tx_time = ? AND tx_id = ?";
            cql.execute(sqlMerchant, newStatus, merchantUuid, transactionDate, timeUuid, transactionId);
            
            // UPSERT u transactions_by_category
            String sqlCategory = "UPDATE transactions_by_category SET status = ? " +
                               "WHERE category_id = ? AND tx_date = ? AND tx_time = ? AND tx_id = ?";
            cql.execute(sqlCategory, newStatus, categoryUuid, transactionDate, timeUuid, transactionId);
            
            log.info("Successfully updated transaction status in all tables with status: {}", newStatus);
            return true;
        } catch (Exception e) {
            log.error("Error updating transaction status: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * UPDATE agregata za merchant-a
     */
    public boolean updateMerchantAggregate(String merchantId, long totalTransactions, double totalAmount, 
                                         double avgAmount, String lastUpdated) {
        log.info("=== UPDATE MERCHANT AGGREGATE === merchant_id: {}, total_tx: {}, total_amount: {}", 
                merchantId, totalTransactions, totalAmount);
        
        try {
            String sql = "UPDATE merchant_aggregates SET " +
                        "total_transactions = ?, " +
                        "total_amount = ?, " +
                        "avg_amount = ?, " +
                        "last_updated = ?, " +
                        "updated_at = ? " +
                        "WHERE merchant_id = ?";
            
            cql.execute(sql, totalTransactions, totalAmount, avgAmount, lastUpdated, 
                       LocalDateTime.now(), merchantId);
            
            log.info("Successfully updated merchant aggregate");
            return true;
        } catch (Exception e) {
            log.error("Error updating merchant aggregate: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * UPDATE dnevnih totala za korisnika
     */
    public boolean updateUserDailyTotal(UUID userId, LocalDate txDate, int newDayCount, long newDayAmountCents) {
        log.info("=== UPDATE USER DAILY TOTAL === user_id: {}, date: {}, count: {}, amount: {}", 
                userId, txDate, newDayCount, newDayAmountCents);
        
        try {
            String sql = "UPDATE user_daily_totals SET " +
                        "day_count = ?, " +
                        "day_amount_cents = ? " +
                        "WHERE user_id = ? AND tx_date = ?";
            
            cql.execute(sql, newDayCount, newDayAmountCents, userId, txDate);
            
            log.info("Successfully updated user daily total");
            return true;
        } catch (Exception e) {
            log.error("Error updating user daily total: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * BATCH UPDATE - Ažurira transakciju u svim tabelama sa novim podacima (UPSERT semantika)
     */
    public boolean updateTransactionCompletely(UUID transactionId, UUID userId, String merchantId, String categoryId,
                                             LocalDate transactionDate, Double newAmount, String newCurrency, 
                                             String newStatus, String newDescription) {
        log.info("=== UPDATE TRANSACTION COMPLETELY === tx_id: {}, amount: {}, status: {}", 
                transactionId, newAmount, newStatus);
        
        try {
            // Konvertuj IDs i amount
            UUID merchantUuid = UUID.fromString(merchantId);
            UUID categoryUuid = UUID.fromString(categoryId);
            long amountCents = Math.round(newAmount * 100);
            UUID timeUuid = com.datastax.oss.driver.api.core.uuid.Uuids.timeBased(); // Cassandra timeuuid
            UUID cardId = java.util.UUID.randomUUID(); // Mock card_id
            
            // UPSERT u transactions_by_user
            String sqlUser = "INSERT INTO transactions_by_user " +
                           "(user_id, tx_date, tx_time, tx_id, card_id, merchant_id, category_id, " +
                           "amount_cents, currency, status) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            cql.execute(sqlUser, userId, transactionDate, timeUuid, transactionId, cardId, 
                       merchantUuid, categoryUuid, amountCents, newCurrency, newStatus);
            
            // UPSERT u transactions_by_merchant
            String sqlMerchant = "INSERT INTO transactions_by_merchant " +
                               "(merchant_id, tx_date, tx_time, tx_id, user_id, card_id, category_id, " +
                               "amount_cents, currency, status) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            cql.execute(sqlMerchant, merchantUuid, transactionDate, timeUuid, transactionId, userId, 
                       cardId, categoryUuid, amountCents, newCurrency, newStatus);
            
            // UPSERT u transactions_by_category
            String sqlCategory = "INSERT INTO transactions_by_category " +
                               "(category_id, tx_date, tx_time, tx_id, user_id, card_id, merchant_id, " +
                               "amount_cents, currency, status) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            cql.execute(sqlCategory, categoryUuid, transactionDate, timeUuid, transactionId, userId, 
                       cardId, merchantUuid, amountCents, newCurrency, newStatus);
            
            log.info("Successfully completed UPSERT transaction in all tables: {} cents, status: {}", 
                    amountCents, newStatus);
            return true;
        } catch (Exception e) {
            log.error("Error during complete transaction UPSERT: {}", e.getMessage(), e);
            return false;
        }
    }
    
    // ================================
    // DELETE OPERATIONS FOR ALL TABLES
    // ================================
    
    /**
     * DELETE operacije za transactions_by_user tabelu
     */
    public boolean deleteTransactionByUser(UUID userId, LocalDate transactionDate, UUID transactionId) {
        log.info("=== DELETE TRANSACTION BY USER === user_id: {}, date: {}, tx_id: {}", userId, transactionDate, transactionId);
        
        try {
            String sql = "DELETE FROM transactions_by_user WHERE user_id = ? AND transaction_date = ? AND transaction_id = ?";
            cql.execute(sql, userId, transactionDate, transactionId);
            log.info("Successfully deleted transaction from transactions_by_user");
            return true;
        } catch (Exception e) {
            log.error("Error deleting transaction from transactions_by_user: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public boolean deleteAllTransactionsForUser(UUID userId) {
        log.info("=== DELETE ALL TRANSACTIONS FOR USER === user_id: {}", userId);
        
        try {
            String sql = "DELETE FROM transactions_by_user WHERE user_id = ?";
            cql.execute(sql, userId);
            log.info("Successfully deleted all transactions for user from transactions_by_user");
            return true;
        } catch (Exception e) {
            log.error("Error deleting all transactions for user from transactions_by_user: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * DELETE operacije za transactions_by_merchant tabelu
     */
    public boolean deleteTransactionByMerchant(String merchantId, LocalDate transactionDate, UUID transactionId) {
        log.info("=== DELETE TRANSACTION BY MERCHANT === merchant_id: {}, date: {}, tx_id: {}", merchantId, transactionDate, transactionId);
        
        try {
            String sql = "DELETE FROM transactions_by_merchant WHERE merchant_id = ? AND transaction_date = ? AND transaction_id = ?";
            cql.execute(sql, merchantId, transactionDate, transactionId);
            log.info("Successfully deleted transaction from transactions_by_merchant");
            return true;
        } catch (Exception e) {
            log.error("Error deleting transaction from transactions_by_merchant: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public boolean deleteAllTransactionsForMerchant(String merchantId) {
        log.info("=== DELETE ALL TRANSACTIONS FOR MERCHANT === merchant_id: {}", merchantId);
        
        try {
            String sql = "DELETE FROM transactions_by_merchant WHERE merchant_id = ?";
            cql.execute(sql, merchantId);
            log.info("Successfully deleted all transactions for merchant from transactions_by_merchant");
            return true;
        } catch (Exception e) {
            log.error("Error deleting all transactions for merchant from transactions_by_merchant: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * DELETE operacije za transactions_by_category tabelu
     */
    public boolean deleteTransactionByCategory(String categoryId, LocalDate transactionDate, UUID transactionId) {
        log.info("=== DELETE TRANSACTION BY CATEGORY === category_id: {}, date: {}, tx_id: {}", categoryId, transactionDate, transactionId);
        
        try {
            String sql = "DELETE FROM transactions_by_category WHERE category_id = ? AND transaction_date = ? AND transaction_id = ?";
            cql.execute(sql, categoryId, transactionDate, transactionId);
            log.info("Successfully deleted transaction from transactions_by_category");
            return true;
        } catch (Exception e) {
            log.error("Error deleting transaction from transactions_by_category: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public boolean deleteAllTransactionsForCategory(String categoryId) {
        log.info("=== DELETE ALL TRANSACTIONS FOR CATEGORY === category_id: {}", categoryId);
        
        try {
            String sql = "DELETE FROM transactions_by_category WHERE category_id = ?";
            cql.execute(sql, categoryId);
            log.info("Successfully deleted all transactions for category from transactions_by_category");
            return true;
        } catch (Exception e) {
            log.error("Error deleting all transactions for category from transactions_by_category: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * DELETE operacije za user_daily_totals tabelu
     */
    public boolean deleteUserDailyTotal(UUID userId, LocalDate transactionDate) {
        log.info("=== DELETE USER DAILY TOTAL === user_id: {}, date: {}", userId, transactionDate);
        
        try {
            String sql = "DELETE FROM user_daily_totals WHERE user_id = ? AND tx_date = ?";
            cql.execute(sql, userId, transactionDate);
            log.info("Successfully deleted user daily total");
            return true;
        } catch (Exception e) {
            log.error("Error deleting user daily total: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public boolean deleteAllDailyTotalsForUser(UUID userId) {
        log.info("=== DELETE ALL DAILY TOTALS FOR USER === user_id: {}", userId);
        
        try {
            String sql = "DELETE FROM user_daily_totals WHERE user_id = ?";
            cql.execute(sql, userId);
            log.info("Successfully deleted all daily totals for user");
            return true;
        } catch (Exception e) {
            log.error("Error deleting all daily totals for user: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * DELETE operacije za merchant_aggregates tabelu
     */
    public boolean deleteMerchantAggregate(String merchantId) {
        log.info("=== DELETE MERCHANT AGGREGATE === merchant_id: {}", merchantId);
        
        try {
            String sql = "DELETE FROM merchant_aggregates WHERE merchant_id = ?";
            cql.execute(sql, merchantId);
            log.info("Successfully deleted merchant aggregate");
            return true;
        } catch (Exception e) {
            log.error("Error deleting merchant aggregate: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * DELETE operacije za category_aggregates tabelu
     */
    public boolean deleteCategoryAggregate(String categoryId) {
        log.info("=== DELETE CATEGORY AGGREGATE === category_id: {}", categoryId);
        
        try {
            String sql = "DELETE FROM category_aggregates WHERE category_id = ?";
            cql.execute(sql, categoryId);
            log.info("Successfully deleted category aggregate");
            return true;
        } catch (Exception e) {
            log.error("Error deleting category aggregate: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * DELETE operacije za merchant_aggregates_by_period tabelu
     */
    public boolean deleteMerchantAggregateByPeriod(String merchantId, String period) {
        log.info("=== DELETE MERCHANT AGGREGATE BY PERIOD === merchant_id: {}, period: {}", merchantId, period);
        
        try {
            String sql = "DELETE FROM merchant_aggregates_by_period WHERE merchant_id = ? AND period = ?";
            cql.execute(sql, merchantId, period);
            log.info("Successfully deleted merchant aggregate by period");
            return true;
        } catch (Exception e) {
            log.error("Error deleting merchant aggregate by period: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public boolean deleteAllMerchantAggregatesByPeriod(String merchantId) {
        log.info("=== DELETE ALL MERCHANT AGGREGATES BY PERIOD === merchant_id: {}", merchantId);
        
        try {
            String sql = "DELETE FROM merchant_aggregates_by_period WHERE merchant_id = ?";
            cql.execute(sql, merchantId);
            log.info("Successfully deleted all merchant aggregates by period");
            return true;
        } catch (Exception e) {
            log.error("Error deleting all merchant aggregates by period: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * DELETE operacije za category_aggregates_by_period tabelu
     */
    public boolean deleteCategoryAggregateByPeriod(String categoryId, String period) {
        log.info("=== DELETE CATEGORY AGGREGATE BY PERIOD === category_id: {}, period: {}", categoryId, period);
        
        try {
            String sql = "DELETE FROM category_aggregates_by_period WHERE category_id = ? AND period = ?";
            cql.execute(sql, categoryId, period);
            log.info("Successfully deleted category aggregate by period");
            return true;
        } catch (Exception e) {
            log.error("Error deleting category aggregate by period: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public boolean deleteAllCategoryAggregatesByPeriod(String categoryId) {
        log.info("=== DELETE ALL CATEGORY AGGREGATES BY PERIOD === category_id: {}", categoryId);
        
        try {
            String sql = "DELETE FROM category_aggregates_by_period WHERE category_id = ?";
            cql.execute(sql, categoryId);
            log.info("Successfully deleted all category aggregates by period");
            return true;
        } catch (Exception e) {
            log.error("Error deleting all category aggregates by period: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * DELETE operacije za tx_dedup tabelu
     */
    public boolean deleteTxDedup(UUID transactionId) {
        log.info("=== DELETE TX DEDUP === transaction_id: {}", transactionId);
        
        try {
            String sql = "DELETE FROM tx_dedup WHERE transaction_id = ?";
            cql.execute(sql, transactionId);
            log.info("Successfully deleted transaction from tx_dedup");
            return true;
        } catch (Exception e) {
            log.error("Error deleting transaction from tx_dedup: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * BATCH DELETE - Briše transakciju iz svih tabela odjednom
     */
    public boolean deleteTransactionCompletely(UUID transactionId, UUID userId, String merchantId, String categoryId, LocalDate transactionDate) {
        log.info("=== DELETE TRANSACTION COMPLETELY === tx_id: {}, user: {}, merchant: {}, category: {}, date: {}", 
                transactionId, userId, merchantId, categoryId, transactionDate);
        
        try {
            // Batch delete iz svih tabela
            boolean success = true;
            
            success &= deleteTransactionByUser(userId, transactionDate, transactionId);
            success &= deleteTransactionByMerchant(merchantId, transactionDate, transactionId);
            success &= deleteTransactionByCategory(categoryId, transactionDate, transactionId);
            success &= deleteTxDedup(transactionId);
            
            if (success) {
                log.info("Successfully deleted transaction completely from all tables");
                return true;
            } else {
                log.error("Some deletions failed during complete transaction deletion");
                return false;
            }
        } catch (Exception e) {
            log.error("Error during complete transaction deletion: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * UTILITY - Briše sve podatke iz svih tabela (za testiranje)
     */
    public boolean deleteAllData() {
        log.warn("=== DELETE ALL DATA === Deleting all data from all tables (DANGEROUS OPERATION)");
        
        try {
            cql.execute("TRUNCATE transactions_by_user");
            cql.execute("TRUNCATE transactions_by_merchant");
            cql.execute("TRUNCATE transactions_by_category");
            cql.execute("TRUNCATE user_daily_totals");
            cql.execute("TRUNCATE merchant_aggregates");
            cql.execute("TRUNCATE category_aggregates");
            cql.execute("TRUNCATE merchant_aggregates_by_period");
            cql.execute("TRUNCATE category_aggregates_by_period");
            cql.execute("TRUNCATE tx_dedup");
            
            log.warn("All tables truncated successfully");
            return true;
        } catch (Exception e) {
            log.error("Error during truncate all: {}", e.getMessage(), e);
            return false;
        }
    }
}
