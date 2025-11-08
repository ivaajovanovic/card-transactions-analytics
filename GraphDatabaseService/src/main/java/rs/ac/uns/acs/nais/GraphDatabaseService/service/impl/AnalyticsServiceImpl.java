package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.TransactionRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.AnalyticsService;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
    private final TransactionRepository txRepo;
        private final Neo4jClient neo4jClient;  

    // za useraaa
    @Override
    public List<AggregateAmountDTO> userSpendByGroup(String userId, Instant from, Instant to, TransactionStatus status, String groupBy) {
        return txRepo.userSpendByGroup(userId, from, to, status.toString(), groupBy);
    }

    @Override
    public List<TopMerchantDTO> topMerchants(String userId, Instant from, Instant to, long limit) {
        return txRepo.topMerchants(userId, from, to, limit);
    }

    @Override
    public List<SpendByGroupDTO> channelMix(String userId, Instant from, Instant to) {
        return txRepo.channelMix(userId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> userTimeOfDay(String userId, Instant from, Instant to) {
        return txRepo.userTimeOfDay(userId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> userLimitUtilizationRaw(String userId, Instant from, Instant to) {
        return txRepo.userLimitUtilizationRaw(userId, from, to);
    }

    @Override
    public SpendByGroupDTO userLimitUtilization(String userId, Instant from, Instant to) {
        var list = userLimitUtilizationRaw(userId, from, to);
        return list.isEmpty() ? new SpendByGroupDTO("limitUtilization", 0.0, 0L) : list.get(0);
    }

    @Override
    public List<SpendByGroupDTO> basketByCategory(String userId, Instant from, Instant to) {
        return txRepo.basketByCategory(userId, from, to);
    }

    // za merchanta
    @Override
    public List<SpendByGroupDTO> merchantSpendByPurpose(String merchantId, Instant from, Instant to) {
        return txRepo.merchantSpendByPurpose(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantCardNetworkShare(String merchantId, Instant from, Instant to) {
        return txRepo.merchantCardNetworkShare(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantFailureReasons(String merchantId, Instant from, Instant to) {
        return txRepo.merchantFailureReasons(merchantId, from, to);
    }

    @Override
    public List<AggregateAmountDTO> merchantAvgTicketOverTime(String merchantId, Instant from, Instant to) {
        return txRepo.merchantAvgTicketOverTime(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantRepeatCustomers(String merchantId, Instant from, Instant to) {
        return txRepo.merchantRepeatCustomers(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantBasketByCategory(String merchantId, Instant from, Instant to) {
        return txRepo.merchantBasketByCategory(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantRegionHeatmap(String merchantId, Instant from, Instant to) {
        return txRepo.merchantRegionHeatmap(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantChannelMix(String merchantId, Instant from, Instant to) {
        return txRepo.merchantChannelMix(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> contactlessShare(String merchantId, Instant from, Instant to) {
        return txRepo.contactlessShare(merchantId, from, to);
    }

    // za adminaa
    @Override
    public List<SpendByGroupDTO> cardTypeFailureRates(Instant from, Instant to) {
        return txRepo.cardTypeFailureRates(from, to);
    }

    @Override
    public List<SpendByGroupDTO> acceptanceCoverage() {
        return txRepo.acceptanceCoverage();
    }

    @Override
    public List<SpendByGroupDTO> failedByMerchant(Instant from, Instant to, long limit) {
        return txRepo.failedByMerchant(from, to, limit);
    }

    @Override
    public List<SpendByGroupDTO> failureReasons(Instant from, Instant to) {
        return txRepo.failureReasons(from, to);
    }

    @Override
    public List<SpendByGroupDTO> acceptanceGaps(Instant from, Instant to, long limit) {
        return txRepo.acceptanceGaps(from, to, limit);
    }

    // KPI za admina
    @Override
    public AdminKPIResponse getAdminKPIs() {
        Instant oneMonthAgo = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);

        Long totalUsers = (long) getAllUsersWithStats().size();
        Long totalMerchants = txRepo.countTotalMerchants();
        Long totalTransactions = txRepo.countTotalTransactions();
        Double totalVolume = txRepo.sumTotalVolume();

        Double userGrowth = txRepo.calculateUserGrowth(oneMonthAgo);
        Double merchantGrowth = txRepo.calculateMerchantGrowth(oneMonthAgo);
        Double transactionGrowth = txRepo.calculateTransactionGrowth(oneMonthAgo);
        Double volumeGrowth = txRepo.calculateVolumeGrowth(oneMonthAgo);

        return new AdminKPIResponse(
            totalUsers != null ? totalUsers : 0L,
            totalMerchants != null ? totalMerchants : 0L,
            totalTransactions != null ? totalTransactions : 0L,
            totalVolume != null ? totalVolume : 0.0,
            userGrowth != null ? userGrowth * 100 : 0.0,
            merchantGrowth != null ? merchantGrowth * 100 : 0.0,
            transactionGrowth != null ? transactionGrowth * 100 : 0.0,
            volumeGrowth != null ? volumeGrowth * 100 : 0.0
        );
    }

    @Override
    public List<UserSegmentDTO> getUserSegments() {
        List<UserSegmentDTO> segments = txRepo.getUserSegments();
        long total = segments.stream().mapToLong(UserSegmentDTO::getValue).sum();
        if (total > 0) {
            segments.forEach(s -> s.setPercentage((double) s.getValue() / total * 100));
        }
        return segments;
    }

    @Override
    public List<CategoryStatsDTO> getTopCategories() {
        return txRepo.getTopCategories();
    }

    @Override
    public List<LocationStatsDTO> getTopLocations() {
        return txRepo.getTopLocations();
    }

    @Override
    public List<TransactionTrendDTO> getTransactionTrends(Instant fromDate, Instant toDate) {
        if (fromDate == null) {
            fromDate = Instant.now().minus(180, java.time.temporal.ChronoUnit.DAYS);
        }
        if (toDate == null) {
            toDate = Instant.now();
        }
        return txRepo.getTransactionTrends(fromDate, toDate);
    }

   @Override
public List<Map<String, Object>> getSuspiciousTransactions(Double minMultiplier) {
    String cypher = """
        MATCH (u:User)-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->()
        WITH u, avg(t.amount) AS userAvg
        MATCH (u)-[:OWNS]->(c2:Card)-[t2:TRANSACTED_WITH]->(m:Merchant)
        WITH u, m, userAvg, t2, (t2.amount / userAvg) AS multiplier
        WHERE multiplier >= $minMultiplier
        RETURN u.externalId AS userId, u.email AS email, coalesce(m.name, m.merchantId) AS merchantId,
               t2.amount AS amount, userAvg AS avgAmount, multiplier AS multiplier,
               t2.timestamp AS timestamp
        ORDER BY multiplier DESC, timestamp DESC
        LIMIT 100
    """;

    return new ArrayList<>(neo4jClient.query(cypher)
        .bind(minMultiplier).to("minMultiplier")
        .fetch().all());
}

@Override
public List<Map<String, Object>> predictPurchaseProbability(String userId, Integer limit) {
    String cypher = """
        MATCH (u:User {externalId: $userId})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
        WITH u, m, 
             count(t) AS frequency,
             avg(t.amount) AS avgAmount,
             sum(t.amount) AS totalSpent,
             max(t.timestamp) AS lastPurchase,
             duration.between(max(t.timestamp), datetime()).days AS daysSinceLastPurchase
        
        // Calculate RFM scores
        WITH u, m, frequency, avgAmount, totalSpent, lastPurchase, daysSinceLastPurchase,
             CASE 
                 WHEN daysSinceLastPurchase <= 30 THEN 5.0
                 WHEN daysSinceLastPurchase <= 60 THEN 4.0
                 WHEN daysSinceLastPurchase <= 90 THEN 3.0
                 WHEN daysSinceLastPurchase <= 180 THEN 2.0
                 ELSE 1.0
             END AS recencyScore,
             CASE 
                 WHEN frequency >= 10 THEN 5.0
                 WHEN frequency >= 5 THEN 4.0
                 WHEN frequency >= 3 THEN 3.0
                 WHEN frequency >= 2 THEN 2.0
                 ELSE 1.0
             END AS frequencyScore,
             CASE 
                 WHEN totalSpent >= 1000 THEN 5.0
                 WHEN totalSpent >= 500 THEN 4.0
                 WHEN totalSpent >= 200 THEN 3.0
                 WHEN totalSpent >= 100 THEN 2.0
                 ELSE 1.0
             END AS monetaryScore
        
        // Get similar users (collaborative filtering)
        OPTIONAL MATCH (u)-[:OWNS]->(:Card)-[:TRANSACTED_WITH]->(sharedMerchant:Merchant)<-[:TRANSACTED_WITH]-(:Card)<-[:OWNS]-(similarUser:User)
        WHERE u <> similarUser
        WITH u, m, frequency, avgAmount, totalSpent, lastPurchase, daysSinceLastPurchase,
             recencyScore, frequencyScore, monetaryScore,
             count(DISTINCT similarUser) AS similarUserCount
        
        // Calculate probability (weighted RFM + collaborative signal)
        WITH m, frequency, avgAmount, totalSpent, lastPurchase, daysSinceLastPurchase,
             ((recencyScore * 0.35) + (frequencyScore * 0.35) + (monetaryScore * 0.20) + 
              (CASE WHEN similarUserCount > 0 THEN 0.5 ELSE 0 END)) AS rawProbability
        
        // Normalize to 0-1 range
        WITH m, frequency, avgAmount, totalSpent, lastPurchase, daysSinceLastPurchase,
             (rawProbability / 5.5) AS probability
        
        RETURN m.merchantId AS merchantId,
               coalesce(m.name, m.merchantId) AS merchantName,
               m.category.name AS category,
               probability,
               frequency AS purchaseCount,
               avgAmount,
               totalSpent,
               lastPurchase,
               daysSinceLastPurchase
        ORDER BY probability DESC, frequency DESC
        LIMIT $limit
    """;

    return new ArrayList<>(neo4jClient.query(cypher)
        .bind(userId).to("userId")
        .bind(limit).to("limit")
        .fetch().all());
}

@Override
public List<Map<String, Object>> predictMerchantPurchaseProbability(String merchantId, Integer limit) {
    String cypher = """
        MATCH (m:Merchant {merchantId: $merchantId})
        
        MATCH (u:User)
        OPTIONAL MATCH (u)-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m)
        WITH m, u, 
             count(t) AS frequency,
             avg(t.amount) AS avgAmount,
             sum(t.amount) AS totalSpent,
             max(t.timestamp) AS lastPurchase,
             CASE 
                WHEN max(t.timestamp) IS NOT NULL 
                THEN duration.between(max(t.timestamp), datetime()).days 
                ELSE null 
             END AS daysSinceLastPurchase
        
        WITH u, m, frequency, avgAmount, totalSpent, lastPurchase, daysSinceLastPurchase,
             CASE 
                 WHEN daysSinceLastPurchase IS NULL THEN 0.0
                 WHEN daysSinceLastPurchase <= 30 THEN 5.0
                 WHEN daysSinceLastPurchase <= 60 THEN 4.0
                 WHEN daysSinceLastPurchase <= 90 THEN 3.0
                 WHEN daysSinceLastPurchase <= 180 THEN 2.0
                 ELSE 1.0
             END AS recencyScore,
             CASE 
                 WHEN frequency >= 10 THEN 5.0
                 WHEN frequency >= 5 THEN 4.0
                 WHEN frequency >= 3 THEN 3.0
                 WHEN frequency >= 1 THEN 2.0
                 ELSE 0.0
             END AS frequencyScore,
             CASE 
                 WHEN totalSpent >= 1000 THEN 5.0
                 WHEN totalSpent >= 500 THEN 4.0
                 WHEN totalSpent >= 200 THEN 3.0
                 WHEN totalSpent >= 50 THEN 2.0
                 WHEN totalSpent > 0 THEN 1.0
                 ELSE 0.0
             END AS monetaryScore
        
        OPTIONAL MATCH (m)-[:IN_CATEGORY]->(cat:Category)<-[:IN_CATEGORY]-(otherM:Merchant)
        WHERE otherM <> m
        OPTIONAL MATCH (u)-[:OWNS]->(:Card)-[:TRANSACTED_WITH]->(otherM)
        WITH u, frequency, avgAmount, totalSpent, lastPurchase, daysSinceLastPurchase,
             recencyScore, frequencyScore, monetaryScore,
             count(DISTINCT otherM) AS sameCategoryMerchants
        
    
        WITH u, frequency, avgAmount, totalSpent, lastPurchase, daysSinceLastPurchase,
             ((recencyScore * 0.35) + (frequencyScore * 0.35) + (monetaryScore * 0.20) + 
              (CASE WHEN sameCategoryMerchants > 0 THEN 0.5 ELSE 0 END)) AS rawProbability
        
        // Normalizacijaa
        WITH u, frequency, avgAmount, totalSpent, lastPurchase, daysSinceLastPurchase,
             (rawProbability / 5.5) AS probability
        
        WHERE probability > 0.0
        
        RETURN u.externalId AS userId,
               coalesce(u.fullName, u.email) AS userName,
               u.email AS userEmail,
               probability,
               frequency AS purchaseCount,
               avgAmount,
               totalSpent,
               lastPurchase,
               daysSinceLastPurchase
        ORDER BY probability DESC, frequency DESC
        LIMIT $limit
    """;

    return new ArrayList<>(neo4jClient.query(cypher)
        .bind(merchantId).to("merchantId")
        .bind(limit).to("limit")
        .fetch().all());
}

@Override
public List<Map<String, Object>> getRegionalPerformance() {
    String cypher = """
        MATCH (m:Merchant)-[:IN_REGION]->(r:Region)
        OPTIONAL MATCH (m)<-[t:TRANSACTED_WITH]-(:Card)<-[:OWNS]-(u:User)
        WHERE t.status = 'SUCCESS'
        WITH r.city AS city, r.country AS country,
             count(DISTINCT m) AS merchantCount,
             count(t) AS transactionCount,
             sum(t.amount) AS totalRevenue,
             avg(t.amount) AS avgTicketSize
        WHERE totalRevenue IS NOT NULL
        RETURN city, country, merchantCount, transactionCount, 
               totalRevenue, avgTicketSize
        ORDER BY totalRevenue DESC
        LIMIT 20
    """;
    
    return new ArrayList<>(neo4jClient.query(cypher).fetch().all());
}

@Override
public List<Map<String, Object>> getShoppingAffinity() {
    String cypher = """
        MATCH (u:User)-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)-[:IN_REGION]->(mRegion:Region)
        WHERE t.status = 'SUCCESS'
        WITH u, mRegion,
             CASE 
               WHEN u.homeRegion.city = mRegion.city THEN 'Local'
               WHEN u.homeRegion.country = mRegion.country AND u.homeRegion.city <> mRegion.city THEN 'Domestic'
               ELSE 'Cross-Border'
             END AS shoppingType,
             count(t) AS txnCount,
             sum(t.amount) AS totalSpent
        WITH shoppingType, 
             count(DISTINCT u) AS userCount,
             sum(txnCount) AS totalTransactions,
             sum(totalSpent) AS totalRevenue,
             avg(totalSpent) AS avgSpentPerUser
        RETURN shoppingType, userCount, totalTransactions, totalRevenue, avgSpentPerUser
        ORDER BY totalRevenue DESC
    """;
    
    return new ArrayList<>(neo4jClient.query(cypher).fetch().all());
}

@Override
public List<Map<String, Object>> getCrossRegionPatterns(Integer limit) {
    String cypher = """
        MATCH (u:User)-[:LIVES_IN]->(userRegion:Region)
MATCH (u)-[:OWNS]->(:Card)-[t:TRANSACTED_WITH]->(m:Merchant)-[:IN_REGION]->(merchantRegion:Region)
MATCH (m)-[:IN_CATEGORY]->(cat:Category)
WHERE userRegion.city <> merchantRegion.city AND t.status = 'SUCCESS'
WITH userRegion.city AS fromCity, userRegion.country AS fromCountry,
     merchantRegion.city AS toCity, merchantRegion.country AS toCountry,
     collect(DISTINCT cat.name) AS categories,
     count(t) AS txnCount, sum(t.amount) AS totalRevenue
RETURN fromCity, fromCountry, toCity, toCountry, txnCount, totalRevenue, 
       categories[0] AS topCategory
ORDER BY totalRevenue DESC
LIMIT $limit
    """;
    
    return new ArrayList<>(neo4jClient.query(cypher)
        .bind(limit).to("limit")
        .fetch().all());
}

@Override
public List<Map<String, Object>> getAllUsersWithStats() {
    String cypher = """
        MATCH (u:User)
        OPTIONAL MATCH (u)-[:LIVES_IN]->(region:Region)
        OPTIONAL MATCH (u)-[:OWNS]->(c:Card)
        OPTIONAL MATCH (c)-[t:TRANSACTED_WITH]->(m:Merchant)
        WITH u, 
            coalesce(region.city, 'N/A') AS city,
            coalesce(region.country, 'N/A') AS country,
            count(DISTINCT c) AS cardCount,
            count(t) AS totalTransactions,
            coalesce(sum(CASE WHEN t.status = 'SUCCESS' THEN t.amount ELSE 0 END), 0.0) AS totalSpent,
            sum(CASE WHEN t.status = 'SUCCESS' THEN 1 ELSE 0 END) AS successfulTransactions,
            sum(CASE WHEN t.status = 'FAILED' THEN 1 ELSE 0 END) AS declinedTransactions,
            max(t.timestamp) AS lastTransactionDate
        RETURN u.externalId AS userId,
            u.email AS email,
            u.fullName AS fullName,
            city,
            country,
            cardCount,
            totalTransactions,
            totalSpent,
            successfulTransactions,
            declinedTransactions,
            lastTransactionDate
        ORDER BY totalSpent DESC
    """;
    try {
        List<Map<String, Object>> result = new ArrayList<>();
        neo4jClient.query(cypher).fetch().all().forEach(row -> {
            Map<String, Object> mapped = new java.util.HashMap<>();
            mapped.put("userId", row.getOrDefault("userId", "N/A"));
            mapped.put("email", row.getOrDefault("email", "N/A"));
            mapped.put("fullName", row.getOrDefault("fullName", "N/A"));
            mapped.put("city", row.getOrDefault("city", "N/A"));
            mapped.put("country", row.getOrDefault("country", "N/A"));
            mapped.put("cardCount", row.getOrDefault("cardCount", 0));
            mapped.put("totalTransactions", row.getOrDefault("totalTransactions", 0));
            mapped.put("totalSpent", row.getOrDefault("totalSpent", 0.0));
            mapped.put("successfulTransactions", row.getOrDefault("successfulTransactions", 0));
            mapped.put("declinedTransactions", row.getOrDefault("declinedTransactions", 0));
            mapped.put("lastTransactionDate", row.getOrDefault("lastTransactionDate", null));
            result.add(mapped);
        });
        return result;
    } catch (Exception e) {
        return new ArrayList<>();
    }
}

@Override
public Map<String, Object> updateMissingUserData() {
    String updateFullNameCypher = """
        MATCH (u:User)
        WHERE u.fullName IS NULL OR u.fullName = ''
        WITH u, split(split(u.email, '@')[0], '.') AS nameParts
        SET u.fullName = 
          CASE 
            WHEN size(nameParts) >= 2 THEN 
              toUpper(substring(nameParts[0], 0, 1)) + substring(nameParts[0], 1) + ' ' + 
              toUpper(substring(nameParts[1], 0, 1)) + substring(nameParts[1], 1)
            ELSE 
              toUpper(substring(nameParts[0], 0, 1)) + substring(nameParts[0], 1)
          END
        RETURN count(u) AS updated
    """;
    
    var fullNameResult = neo4jClient.query(updateFullNameCypher)
        .fetch().one();
    
    Long usersWithNameUpdated = fullNameResult
        .map(m -> ((Number) m.get("updated")).longValue())
        .orElse(0L);
    
    String createLivesInCypher = """
        MATCH (u:User)
        WHERE NOT (u)-[:LIVES_IN]->(:Region)
        WITH u
        MATCH (r:Region)
        WITH u, r, rand() AS random
        ORDER BY random
        LIMIT 1
        WITH u, collect(r)[0] AS selectedRegion
        WHERE selectedRegion IS NOT NULL
        MERGE (u)-[:LIVES_IN]->(selectedRegion)
        RETURN count(u) AS created
    """;
    
    String countUsersCypher = """
        MATCH (u:User)
        WHERE NOT (u)-[:LIVES_IN]->(:Region)
        RETURN count(u) AS count
    """;
    
    var countResult = neo4jClient.query(countUsersCypher).fetch().one();
    Long usersWithoutLocation = countResult
        .map(m -> ((Number) m.get("count")).longValue())
        .orElse(0L);
    
    String batchCreateCypher = """
        MATCH (u:User)
        WHERE NOT (u)-[:LIVES_IN]->(:Region)
        WITH u LIMIT 100
        MATCH (r:Region)
        WITH u, r
        ORDER BY rand()
        WITH u, collect(r)[0] AS selectedRegion
        WHERE selectedRegion IS NOT NULL
        MERGE (u)-[:LIVES_IN]->(selectedRegion)
        RETURN count(u) AS created
    """;
    
    Long totalCreated = 0L;
    for (int i = 0; i < (usersWithoutLocation / 100) + 1; i++) {
        var batchResult = neo4jClient.query(batchCreateCypher).fetch().one();
        Long created = batchResult
            .map(m -> ((Number) m.get("created")).longValue())
            .orElse(0L);
        totalCreated += created;
        if (created == 0) break; 
    }
    
    return Map.of(
        "usersWithNameUpdated", usersWithNameUpdated,
        "usersWithLocationCreated", totalCreated,
        "message", "User data updated successfully"
    );

}
@Override
public List<SpendByGroupDTO> getAdminOptimalPurchaseTime() {
    return neo4jClient.query("""
        MATCH ()-[t:TRANSACTED_WITH]->()
        WITH apoc.date.format(t.timestamp, 'ms', 'HH') AS hour
        RETURN hour,
               sum(t.amount) AS totalAmount,
               count(t) AS transactionCount,
               avg(t.amount) AS avgAmount
        ORDER BY totalAmount DESC, transactionCount DESC
    """)
    .fetch()
    .all()
    .stream()
    .map(record -> {
        SpendByGroupDTO dto = new SpendByGroupDTO();
        dto.setGroupKey((String) record.get("hour")); // hour kao groupKey
        dto.setTotalAmount(record.get("totalAmount") != null ? ((Number) record.get("totalAmount")).doubleValue() : 0.0);
        dto.setTxnCount(record.get("transactionCount") != null ? ((Number) record.get("transactionCount")).longValue() : 0L);
        return dto;
    })
    .toList();
}
}

