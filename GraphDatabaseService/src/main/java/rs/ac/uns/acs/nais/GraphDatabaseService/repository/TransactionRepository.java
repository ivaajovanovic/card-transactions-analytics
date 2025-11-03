
package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.TransactionRel;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import java.time.Instant;
import java.util.List;

@org.springframework.stereotype.Repository
public interface TransactionRepository extends Neo4jRepository<TransactionRel, Long> {

    // ==== USER analytics ====
    
    @Query("""
       MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status) = $status
    OPTIONAL MATCH (m)-[:IN_CATEGORY]->(cat:Category)
    WITH t, m, cat,
         (CASE $groupBy 
            WHEN 'purpose' THEN toString(t.purpose)
            WHEN 'paymentType' THEN toString(t.paymentType)
            WHEN 'category' THEN coalesce(cat.name,'UNKNOWN')
            WHEN 'merchant' THEN coalesce(m.name,m.merchantId)
         END) AS groupKey
    WITH date(datetime(t.timestamp)) AS d, groupKey, sum(t.amount) AS total
    RETURN d AS bucketDate, groupKey AS groupKey, total AS totalAmount
    ORDER BY bucketDate ASC
    """)
    List<AggregateAmountDTO> userSpendByGroup(String userId, Instant from, Instant to, String status, String groupBy);

    @Query("""
       MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='SUCCESS'
    OPTIONAL MATCH (m)-[:IN_CATEGORY]->(cat:Category)
    RETURN m.merchantId AS merchantId, m.name AS merchantName, coalesce(cat.name,'UNKNOWN') AS category,
           sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY totalAmount DESC
    LIMIT $limit
    """)
    List<TopMerchantDTO> topMerchants(String userId, Instant from, Instant to, long limit);

    @Query("""
       MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(:Merchant)
    WHERE t.timestamp >= $from AND t.timestamp < $to
    RETURN toString(t.channel) AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> channelMix(String userId, Instant from, Instant to);

    // ==== ADMIN KPI analytics ====
    
    @Query("""
    MATCH (u:User)
    RETURN count(u) AS totalUsers
    """)
    Long countTotalUsers();

    @Query("""
    MATCH (m:Merchant)
    RETURN count(m) AS totalMerchants
    """)
    Long countTotalMerchants();

    @Query("""
    MATCH ()-[t:TRANSACTED_WITH]->()
    RETURN count(t)
    """)
    Long countTotalTransactions();

    @Query("""
    MATCH ()-[t:TRANSACTED_WITH]->()
    RETURN sum(t.amount)
    """)
    Double sumTotalVolume();

    @Query("""
    MATCH (u:User)
    WITH count(u) AS total
    MATCH (u:User)
    WHERE u.memberSince >= $fromDate
       WITH total, count(u) AS part
       RETURN CASE WHEN total > 0 THEN toFloat(part) / total ELSE 0.0 END AS growth
    """)
    Double calculateUserGrowth(Instant fromDate);

    @Query("""
    MATCH (m:Merchant)
    WITH count(m) AS total
    MATCH (m:Merchant)
    WHERE m.createdAt >= $fromDate
       WITH total, count(m) AS part
       RETURN CASE WHEN total > 0 THEN toFloat(part) / total ELSE 0.0 END AS growth
    """)
    Double calculateMerchantGrowth(Instant fromDate);

    @Query("""
    MATCH ()-[t:TRANSACTED_WITH]->()
    WITH count(t) AS total
    MATCH ()-[t2:TRANSACTED_WITH]->()
    WHERE t2.timestamp >= $fromDate
    WITH total, count(t2) AS part
    RETURN CASE WHEN total > 0 THEN toFloat(part) / total ELSE 0.0 END AS growth
    """)
    Double calculateTransactionGrowth(Instant fromDate);

    @Query("""
    MATCH ()-[t:TRANSACTED_WITH]->()
    WITH sum(t.amount) AS total
    MATCH ()-[t2:TRANSACTED_WITH]->()
    WHERE t2.timestamp >= $fromDate
    WITH total, sum(t2.amount) AS part
    RETURN CASE WHEN total > 0 THEN part / total ELSE 0.0 END AS growth
    """)
    Double calculateVolumeGrowth(Instant fromDate);

    @Query("""
    MATCH (u:User)-[:OWNS]->(c:Card)
    WITH c, count(u) AS owners
    WHERE owners > 0
    RETURN 
        CASE 
            WHEN owners = 1 THEN 'Individual'
            ELSE 'Shared'
        END AS name,
        count(c) AS value
    ORDER BY value DESC
    """)
    List<UserSegmentDTO> getUserSegments();

    @Query("""
    MATCH ()-[t:TRANSACTED_WITH]->(m:Merchant)-[:IN_CATEGORY]->(cat:Category)
    WHERE toString(t.status) = 'SUCCESS'
    WITH cat.name AS category, count(t) AS txCount, sum(t.amount) AS amount
    RETURN category, txCount AS transactions, amount
    ORDER BY amount DESC
    LIMIT 10
    """)
    List<CategoryStatsDTO> getTopCategories();

    @Query("""
    MATCH (u:User)
    WHERE u.homeCity IS NOT NULL
    WITH u.homeCity + ', ' + coalesce(u.homeCountry, '') AS loc, count(u) AS userCount
       MATCH (u2:User {homeCity: split(loc, ',')[0]})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->()
    WHERE toString(t.status) = 'SUCCESS'
    WITH loc, userCount, count(t) AS txCount, sum(t.amount) AS vol
    RETURN loc AS location, userCount AS users, txCount AS transactions, vol AS volume
    ORDER BY users DESC
    LIMIT 10
    """)
    List<LocationStatsDTO> getTopLocations();

    @Query("""
    MATCH (c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
    WHERE t.timestamp IS NOT NULL
      AND ($fromDate IS NULL OR t.timestamp >= $fromDate)
      AND ($toDate IS NULL OR t.timestamp < $toDate)
    WITH datetime(t.timestamp) AS dt, t.amount AS amount
    WITH dt.year AS year, dt.month AS month, count(*) AS txCount, sum(amount) AS vol
    ORDER BY year, month
    RETURN toString(year) + '-' + (CASE WHEN month < 10 THEN '0' + toString(month) ELSE toString(month) END) AS month, txCount AS transactions, vol AS volume
    """)
    List<TransactionTrendDTO> getTransactionTrends(Instant fromDate, Instant toDate);

    @Query("""
       MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(:Merchant)
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='SUCCESS'
       WITH datetime(t.timestamp).hour AS hour, t.amount AS amt
       WITH toString(hour) AS groupKey, amt
       RETURN groupKey, sum(amt) AS totalAmount, count(*) AS txnCount
       ORDER BY toInteger(groupKey)
    """)
    List<SpendByGroupDTO> userTimeOfDay(String userId, Instant from, Instant to);

    @Query("""
       MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)
    OPTIONAL MATCH (c)-[t:TRANSACTED_WITH]->(:Merchant)
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='SUCCESS'
    WITH c, sum(coalesce(t.amount,0)) AS spent, coalesce(c.monthlyLimit,0) AS lim
    RETURN 'limitUtilization' AS groupKey, 
           CASE WHEN lim = 0 THEN 0.0 ELSE spent/lim END AS totalAmount, 
           spent AS txnCount
    """)
    List<SpendByGroupDTO> userLimitUtilizationRaw(String userId, Instant from, Instant to);

       @Query("""
       MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)-[:IN_CATEGORY]->(cat:Category)
       WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='SUCCESS'
       RETURN cat.name AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
       ORDER BY totalAmount DESC
       """)
       List<SpendByGroupDTO> basketByCategory(String userId, Instant from, Instant to);

    // ==== MERCHANT analytics ====
    
    @Query("""
    MATCH (:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='SUCCESS'
    RETURN toString(t.purpose) AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY totalAmount DESC
    """)
    List<SpendByGroupDTO> merchantSpendByPurpose(String merchantId, Instant from, Instant to);

    @Query("""
    MATCH (c:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})
    WHERE t.timestamp >= $from AND t.timestamp < $to
    RETURN toString(c.network)+':'+toString(c.type) AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> merchantCardNetworkShare(String merchantId, Instant from, Instant to);

    @Query("""
    MATCH (:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='FAILED'
    RETURN toString(t.declineReason) AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> merchantFailureReasons(String merchantId, Instant from, Instant to);

    @Query("""
    MATCH (:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='SUCCESS'
    WITH date(datetime(t.timestamp)) AS d, avg(t.amount) AS avgTicket
    RETURN d AS bucketDate, 'avgTicket' AS groupKey, avgTicket AS totalAmount
    ORDER BY bucketDate
    """)
    List<AggregateAmountDTO> merchantAvgTicketOverTime(String merchantId, Instant from, Instant to);

    @Query("""
       MATCH (u:User)-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})
    WHERE t.timestamp >= $from AND t.timestamp < $to
    WITH u, count(t) AS cnt, sum(t.amount) AS amt
    RETURN CASE WHEN cnt>1 THEN 'REPEAT' ELSE 'NEW' END AS groupKey, sum(amt) AS totalAmount, count(u) AS txnCount
    ORDER BY groupKey DESC
    """)
    List<SpendByGroupDTO> merchantRepeatCustomers(String merchantId, Instant from, Instant to);

    @Query("""
    MATCH (:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})-[:IN_CATEGORY]->(cat:Category)
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='SUCCESS'
    RETURN cat.name AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY totalAmount DESC
    """)
    List<SpendByGroupDTO> merchantBasketByCategory(String merchantId, Instant from, Instant to);

    @Query("""
    MATCH (:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})-[:IN_REGION]->(r:Region)
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='SUCCESS'
    RETURN coalesce(r.name,'UNKNOWN') AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> merchantRegionHeatmap(String merchantId, Instant from, Instant to);

    @Query("""
    MATCH (:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})
    WHERE t.timestamp >= $from AND t.timestamp < $to
    RETURN toString(t.channel) AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> merchantChannelMix(String merchantId, Instant from, Instant to);

    @Query("""
    MATCH (:Card)-[t:TRANSACTED_WITH]->(m:Merchant {merchantId:$merchantId})
    WHERE t.timestamp >= $from AND t.timestamp < $to
    RETURN CASE WHEN t.contactless = true THEN 'CONTACTLESS' ELSE 'OTHER' END AS groupKey,
           sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> contactlessShare(String merchantId, Instant from, Instant to);

    // ==== ADMIN analytics ====
    
    @Query("""
    MATCH (c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
    WHERE t.timestamp >= $from AND t.timestamp < $to
    RETURN toString(c.type) AS groupKey,
           toFloat(sum(CASE WHEN toString(t.status)='SUCCESS' THEN 1 ELSE 0 END)) / count(t) AS totalAmount,
           count(t) AS txnCount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> cardTypeFailureRates(Instant from, Instant to);

    @Query("""
    MATCH (m:Merchant)-[:ACCEPTS]->(a:Acceptance)
    RETURN toString(a.network)+':'+toString(a.type) AS groupKey, count(m) AS txnCount, 0.0 AS totalAmount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> acceptanceCoverage();

    @Query("""
    MATCH (c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='FAILED'
    RETURN m.merchantId AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY txnCount DESC
    LIMIT $limit
    """)
    List<SpendByGroupDTO> failedByMerchant(Instant from, Instant to, long limit);

    @Query("""
    MATCH (c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
    WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status)='FAILED'
    RETURN toString(t.declineReason) AS groupKey, sum(t.amount) AS totalAmount, count(t) AS txnCount
    ORDER BY txnCount DESC
    """)
    List<SpendByGroupDTO> failureReasons(Instant from, Instant to);

    @Query("""
    MATCH (m:Merchant)
    OPTIONAL MATCH (m)-[r:ACCEPTS]->(a:Acceptance)
    WITH m, collect(toString(a.network)+':'+toString(a.type)) AS accepts
    MATCH (c:Card)-[t:TRANSACTED_WITH]->(m)
    WHERE t.timestamp >= $from AND t.timestamp < $to
    WITH m, accepts, collect({net:toString(c.network), type:toString(c.type), status:toString(t.status)}) AS tx
    RETURN m.merchantId AS groupKey, size(accepts) AS totalAmount, 
           reduce(f=0, x IN tx | f + CASE WHEN x.status='FAILED' THEN 1 ELSE 0 END) AS txnCount
    ORDER BY txnCount DESC
    LIMIT $limit
    """)
    List<SpendByGroupDTO> acceptanceGaps(Instant from, Instant to, long limit);

    // Recurring monthly expenses per user
    @Query("""
    MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
    WITH m, date(datetime(t.timestamp)) AS d, t.amount AS amt
    WITH m, d.year AS y, d.month AS mo, avg(amt) AS avgAmt
    WITH m, collect(distinct y + '-' + mo) AS ym, avg(avgAmt) AS monthlyAvg, size(collect(distinct y + '-' + mo)) AS months
    WHERE months >= 3
    RETURN coalesce(m.name, m.merchantId) AS merchantName, monthlyAvg AS avgMonthlySpend, months
    ORDER BY months DESC, monthlyAvg DESC
    """)
    List<RecurringExpenseDTO> recurringMonthlyExpenses(String userId);

    // Collaborative filtering: merchants recommended by similar users
       @Query("""
   MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)-[:TRANSACTED_WITH]->(m:Merchant)
   WITH collect(distinct m) AS myMerchants
   MATCH (other:User)-[:OWNS]->(c2:Card)-[:TRANSACTED_WITH]->(m2:Merchant)
   WHERE other.externalId <> $userId AND m2 IN myMerchants
   WITH other, myMerchants, count(distinct m2) AS overlap
       WHERE overlap >= 2
       MATCH (other)-[:OWNS]->(c3:Card)-[:TRANSACTED_WITH]->(rec:Merchant)
       WHERE NOT rec IN myMerchants
       RETURN coalesce(rec.name, rec.merchantId) AS merchantName, count(*) AS score
       ORDER BY score DESC
       LIMIT 10
       """)
       List<CollaborativeRecommendationDTO> recommendedMerchantsCF(String userId);

       // Optimal time for purchase per user (day/hour with human-readable day name)
       @Query("""
       MATCH (u:User {externalId:$userId})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->()
       WITH datetime(t.timestamp) AS dt, t.amount AS amt
       WITH dt.dayOfWeek AS dow, dt.hour AS hour, sum(amt) AS total, count(*) AS cnt
       WITH dow, hour, total, cnt, total / cnt AS avgAmt,
            CASE dow
              WHEN 1 THEN 'Monday'
              WHEN 2 THEN 'Tuesday'
              WHEN 3 THEN 'Wednesday'
              WHEN 4 THEN 'Thursday'
              WHEN 5 THEN 'Friday'
              WHEN 6 THEN 'Saturday'
              WHEN 7 THEN 'Sunday'
              ELSE 'Day'
            END AS dayName
       RETURN dayName AS dayOfWeek, hour, total, cnt AS transactionCount, avgAmt AS avgAmount
       ORDER BY total DESC, cnt DESC
       """)
       List<OptimalPurchaseTimeDTO> optimalPurchaseTime(String userId);

   // Best card benefit for category for a specific user (uses RewardRule.categoryCode)
   @Query("""
   MATCH (u:User {email:$email})-[:OWNS]->(card:Card)
   MATCH (prog:RewardProgram)-[:APPLIES_TO]->(card)
   MATCH (prog)-[:HAS_RULE]->(rule:RewardRule)
   WHERE rule.categoryCode = $categoryCode
   RETURN card.panHash AS card, prog.name AS program, rule.rewardRate AS rate, rule.cap AS cap, rule.conditions AS conditions
   ORDER BY rate DESC, cap DESC
   LIMIT 5
   """)
   List<CardBenefitDTO> bestCardBenefitsForCategory(String email, String categoryCode);
}
