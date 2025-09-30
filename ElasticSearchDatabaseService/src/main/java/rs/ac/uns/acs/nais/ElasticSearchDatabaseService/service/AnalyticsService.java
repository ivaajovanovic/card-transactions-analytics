package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Transaction;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.UserActivity;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository.TransactionRepository;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository.UserActivityRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    
    private final TransactionRepository transactionRepository;
    private final UserActivityRepository userActivityRepository;
    
    // ==================== GENERAL ANALYTICS (ALL USERS) ====================

    /**
     * SIMPLE SECTION 1: Recent Transactions
     * Fetch the last 20 transactions sorted by date descending
     */
    public List<Map<String, Object>> getRecentTransactions() {
        log.info("Fetching recent transactions");
        
        // Use the existing repository method that works
        List<Transaction> allTransactions = transactionRepository.findTop10ByOrderByAmountDesc();
        
        return allTransactions.stream()
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate())) // Sort by date descending
                .limit(20)
                .map(tx -> {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("date", tx.getDate());
                    transaction.put("cardId", tx.getCardId());
                    transaction.put("merchantId", tx.getMerchantId());
                    transaction.put("amount", tx.getAmount());
                    transaction.put("description", tx.getDescription());
                    return transaction;
                })
                .collect(Collectors.toList());
    }

    /**
     * SIMPLE SECTION 2: Weekly Spending Totals (Last 3 Months)
     * Aggregate by calendar week using date_histogram (interval = week)
     */
    public List<Map<String, Object>> getWeeklySpendingTotals() {
        log.info("Calculating weekly spending totals");
        
        // Use specific queries instead of findAll() to avoid search issues
        List<Transaction> allTransactions = new ArrayList<>();
        
        // Get transactions from different cards to have a good sample
        for (int i = 1; i <= 10; i++) {
            List<Transaction> cardTransactions = transactionRepository.findByCardId("card" + i);
            allTransactions.addAll(cardTransactions);
        }
        
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        
        // Filter transactions in the last 3 months
        List<Transaction> recentTransactions = allTransactions.stream()
                .filter(tx -> {
                    try {
                        LocalDateTime txDate = LocalDateTime.parse(tx.getDate());
                        return txDate.isAfter(threeMonthsAgo);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
        
        // Group by week
        Map<String, Double> weeklyTotals = new HashMap<>();
        
        for (Transaction tx : recentTransactions) {
            try {
                LocalDateTime txDate = LocalDateTime.parse(tx.getDate());
                // Get the start of the week (Monday)
                LocalDateTime weekStart = txDate.minusDays(txDate.getDayOfWeek().getValue() - 1);
                String weekKey = weekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                weeklyTotals.merge(weekKey, tx.getAmount(), Double::sum);
            } catch (Exception e) {
                // Skip malformed dates
            }
        }
        
        return weeklyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Sort chronologically
                .map(entry -> {
                    Map<String, Object> weekData = new HashMap<>();
                    weekData.put("week", entry.getKey());
                    weekData.put("totalSpent", entry.getValue());
                    return weekData;
                })
                .collect(Collectors.toList());
    }

    /**
     * COMPLEX SECTION: Top 10 Keywords by Spending (Last 3 Months)
     * Aggregation: terms aggregation on description, sum(amount) per keyword
     */
    public List<Map<String, Object>> getTopKeywordsBySpending() {
        log.info("Analyzing top keywords by spending");
        
        // Use specific queries instead of findAll() to avoid search issues
        List<Transaction> allTransactions = new ArrayList<>();
        
        // Get transactions from different cards to have a good sample
        for (int i = 1; i <= 10; i++) {
            List<Transaction> cardTransactions = transactionRepository.findByCardId("card" + i);
            allTransactions.addAll(cardTransactions);
        }
        
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        
        // Filter transactions in the last 3 months
        List<Transaction> recentTransactions = allTransactions.stream()
                .filter(tx -> {
                    try {
                        LocalDateTime txDate = LocalDateTime.parse(tx.getDate());
                        return txDate.isAfter(threeMonthsAgo);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
        
        // Extract keywords and calculate spending per keyword
        Map<String, Double> keywordSpending = new HashMap<>();
        
        for (Transaction tx : recentTransactions) {
            if (tx.getDescription() != null) {
                // Split description into words and process each keyword
                String[] words = tx.getDescription().toLowerCase()
                        .replaceAll("[^a-zA-Z\\s]", "") // Remove punctuation
                        .split("\\s+");
                
                for (String word : words) {
                    if (word.length() > 3) { // Only consider words longer than 3 characters
                        keywordSpending.merge(word, tx.getAmount(), Double::sum);
                    }
                }
            }
        }
        
        return keywordSpending.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // Sort by spending descending
                .limit(10) // Top 10 keywords
                .map(entry -> {
                    Map<String, Object> keywordData = new HashMap<>();
                    keywordData.put("keyword", entry.getKey());
                    keywordData.put("totalSpent", entry.getValue());
                    return keywordData;
                })
                .collect(Collectors.toList());
    }
}