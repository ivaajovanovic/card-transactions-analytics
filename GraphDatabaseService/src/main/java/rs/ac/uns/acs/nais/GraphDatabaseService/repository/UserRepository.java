package rs.ac.uns.acs.nais.GraphDatabaseService.repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Repository
public interface UserRepository extends Neo4jRepository<UserNode, Long> {
    @Query("""
        MATCH (u:User)-[:LIVES_IN]->(r:Region)
        RETURN u.userId AS userId, u.email AS email, u.firstName AS firstName, u.lastName AS lastName, r.city AS city, r.country AS country
    """)
    List<Map<String, Object>> findAllWithRegion();
    
    Optional<UserNode> findByExternalId(String externalId);
        Optional<UserNode> findByEmail(String email);
    @Query("MATCH (u:User) WHERE u.email = $email AND u.password = $password RETURN u LIMIT 1")
    Optional<UserNode> findByEmailAndPassword(@Param("email") String email, @Param("password") String password);
    
    List<UserNode> findBySegment(String segment);
    
    @Query("""
        MATCH (u:User {externalId: $userExternalId})-[c:CONNECTED_TO]->(connected:User)
        RETURN u, collect(c), collect(connected)
    """)
    Optional<UserNode> findByExternalIdWithConnections(@Param("userExternalId") String userExternalId);
    
    @Query("""
        MATCH (u:User {externalId: $userExternalId})-[r:HAS_PREFERENCES]->(p:UserPreference)
        RETURN u, collect(r), collect(p)
    """)
    Optional<UserNode> findByExternalIdWithPreferences(@Param("userExternalId") String userExternalId);
    
    @Query("""
        MATCH (u:User {externalId: $userExternalId})-[c:CONNECTED_TO]->(connected:User)
        RETURN connected, c
        ORDER BY c.connectionStrength DESC
    """)
    List<Object> findConnectedUsers(@Param("userExternalId") String userExternalId);
    
    @Query("MATCH (u:User) WHERE u.incomeLevel = $incomeLevel RETURN u")
    List<UserNode> findByIncomeLevel(@Param("incomeLevel") String incomeLevel);
    
    @Query("MATCH (u:User) WHERE u.lifestage = $lifestage RETURN u")
    List<UserNode> findByLifestage(@Param("lifestage") String lifestage);
    
    @Query("MATCH (u:User) WHERE u.homeCity = $city RETURN u")
    List<UserNode> findByHomeCity(@Param("city") String city);
    
    @Query("MATCH (u:User) WHERE u.homeCountry = $country RETURN u")
    List<UserNode> findByHomeCountry(@Param("country") String country);
    
    @Query("MATCH (u:User) WHERE u.homeCity = $city AND u.homeCountry = $country RETURN u")
    List<UserNode> findByLocation(@Param("city") String city, @Param("country") String country);
    
    @Query("""
        MATCH (u:User {externalId: $userExternalId})-[:OWNS]->(c:Card)-[:TRANSACTED_WITH]->(m:Merchant)-[:IN_REGION]->(r:Region)
        WHERE r.city <> u.homeCity OR r.country <> u.homeCountry
        WITH u, COUNT(DISTINCT r.city) as citiesVisited, COUNT(*) as foreignTransactions
        RETURN u, citiesVisited, foreignTransactions
    """)
    Object getUserTravelPattern(@Param("userExternalId") String userExternalId);
    
    @Query("""
        MATCH (u:User)-[:OWNS]->(c:Card)-[:TRANSACTED_WITH]->(m:Merchant)-[:IN_REGION]->(r:Region)
        WHERE r.city <> u.homeCity
        WITH u, COUNT(DISTINCT r.city) as citiesCount
        WHERE citiesCount >= $minCities
        RETURN u
        ORDER BY citiesCount DESC
    """)
    List<UserNode> findFrequentTravelers(@Param("minCities") Integer minCities);
    
    @Query("""
        MATCH (u:User)-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)-[:IN_CATEGORY]->(cat:Category)
        WHERE id(u) = $userId
        RETURN cat.categoryId AS categoryCode, 
               cat.name AS categoryName, 
               COUNT(t) AS transactionCount, 
               SUM(t.amount) AS totalAmount,
               AVG(t.amount) AS avgAmount
        ORDER BY totalAmount DESC
    """)
    List<Map<String, Object>> getUserSpendingByCategory(@Param("userId") Long userId);
    
    @Query("""
        MATCH (u:User {email: $email})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)-[:IN_CATEGORY]->(cat:Category)
        RETURN cat.categoryId AS categoryCode, 
               cat.name AS categoryName, 
               COUNT(t) AS transactionCount, 
               SUM(t.amount) AS totalAmount,
               AVG(t.amount) AS avgAmount
        ORDER BY totalAmount DESC
    """)
    List<Map<String, Object>> getUserSpendingByCategoryByEmail(@Param("email") String email);
    
    @Query("""
        MATCH (u:User)-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
        WHERE id(u) = $userId
        OPTIONAL MATCH (m)-[:IN_CATEGORY]->(cat:Category)
        RETURN m.merchantId AS merchantId,
               m.name AS merchantName,
               cat.name AS categoryName,
               COUNT(t) AS transactionCount,
               SUM(t.amount) AS totalSpent,
               AVG(t.amount) AS avgAmount
        ORDER BY transactionCount DESC
        LIMIT $limit
    """)
    List<Map<String, Object>> getUserTopMerchants(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    @Query("""
        MATCH (u:User {email: $email})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
        OPTIONAL MATCH (m)-[:IN_CATEGORY]->(cat:Category)
        RETURN m.merchantId AS merchantId,
               m.name AS merchantName,
               cat.name AS categoryName,
               COUNT(t) AS transactionCount,
               SUM(t.amount) AS totalSpent,
               AVG(t.amount) AS avgAmount
        ORDER BY transactionCount DESC
        LIMIT $limit
    """)
    List<Map<String, Object>> getUserTopMerchantsByEmail(@Param("email") String email, @Param("limit") Integer limit);
    
    @Query("""
        MATCH (u:User)-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
        WHERE id(u) = $userId
        RETURN c.panHash AS cardId,
               c.cardBrand AS network,
               c.cardType AS type,
               COUNT(t) AS transactionCount,
               SUM(t.amount) AS totalSpent,
               SUM(CASE WHEN t.contactless = TRUE THEN 1 ELSE 0 END) AS contactlessCount,
               0 AS installmentsCount
        ORDER BY transactionCount DESC
    """)
    List<Map<String, Object>> getUserCardUsage(@Param("userId") Long userId);
    
    @Query("""
        MATCH (u:User {email: $email})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
        RETURN c.panHash AS cardId,
               c.cardBrand AS network,
               c.cardType AS type,
               COUNT(t) AS transactionCount,
               SUM(t.amount) AS totalSpent,
               SUM(CASE WHEN t.contactless = TRUE THEN 1 ELSE 0 END) AS contactlessCount,
               0 AS installmentsCount
        ORDER BY transactionCount DESC
    """)
    List<Map<String, Object>> getUserCardUsageByEmail(@Param("email") String email);
    
    @Query("""
        MATCH (u:User)-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
        WHERE id(u) = $userId
        WITH u, t, m,
             toString(datetime(t.timestamp).year) + '-' + 
             substring('0' + toString(datetime(t.timestamp).month), -2) AS yearMonth
        RETURN yearMonth,
               COUNT(t) AS transactionCount,
               SUM(t.amount) AS totalAmount,
               AVG(t.amount) AS avgAmount,
               COUNT(DISTINCT m) AS uniqueMerchants
        ORDER BY yearMonth DESC
        LIMIT 12
    """)
    List<Map<String, Object>> getUserMonthlySpending(@Param("userId") Long userId);
    
    @Query("""
        MATCH (u:User {email: $email})-[:OWNS]->(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
        WITH u, t, m,
             toString(datetime(t.timestamp).year) + '-' + 
             substring('0' + toString(datetime(t.timestamp).month), -2) AS yearMonth
        RETURN yearMonth,
               COUNT(t) AS transactionCount,
               SUM(t.amount) AS totalAmount,
               AVG(t.amount) AS avgAmount,
               COUNT(DISTINCT m) AS uniqueMerchants
        ORDER BY yearMonth DESC
        LIMIT 12
    """)
    List<Map<String, Object>> getUserMonthlySpendingByEmail(@Param("email") String email);
}
