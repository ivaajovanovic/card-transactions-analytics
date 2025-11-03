package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.MerchantNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface MerchantRepository extends Neo4jRepository<MerchantNode, Long> {
    Optional<MerchantNode> findByMerchantId(String merchantId);
    @Query("MATCH (m:Merchant) WHERE m.email = $email RETURN m ORDER BY id(m) ASC LIMIT 1")
    Optional<MerchantNode> findByEmail(@Param("email") String email);
    @Query("MATCH (m:Merchant) WHERE m.email = $email AND m.password = $password RETURN m ORDER BY id(m) ASC LIMIT 1")
    Optional<MerchantNode> findByEmailAndPassword(@Param("email") String email, @Param("password") String password);
    
    // Find merchants with sequences
    @Query("""
        MATCH (m:Merchant {merchantId: $merchantId})-[s:FOLLOWED_BY]->(next:Merchant)
        RETURN m, collect(s), collect(next)
    """)
    Optional<MerchantNode> findByMerchantIdWithSequences(@Param("merchantId") String merchantId);
    
    // Find next merchants in sequence (chain analysis)
    @Query("""
        MATCH (m:Merchant {merchantId: $merchantId})-[s:FOLLOWED_BY]->(next:Merchant)
        RETURN next, s
        ORDER BY s.sequenceCount DESC
        LIMIT $limit
    """)
    List<Object> findMerchantSequences(@Param("merchantId") String merchantId, @Param("limit") Integer limit);
    
    // Find merchants by price range
    @Query("MATCH (m:Merchant) WHERE m.priceRange = $priceRange RETURN m")
    List<MerchantNode> findByPriceRange(@Param("priceRange") String priceRange);
    
    // Find merchants by tag
    @Query("MATCH (m:Merchant) WHERE $tag IN m.tags RETURN m ORDER BY m.popularityScore DESC")
    List<MerchantNode> findByTag(@Param("tag") String tag);
    
    // Find popular merchants
    @Query("MATCH (m:Merchant) WHERE m.popularityScore >= $minScore RETURN m ORDER BY m.popularityScore DESC LIMIT $limit")
    List<MerchantNode> findPopularMerchants(@Param("minScore") Double minScore, @Param("limit") Integer limit);
    
    // Find merchants by average ticket size range
    @Query("MATCH (m:Merchant) WHERE m.avgTicketSize >= $minTicket AND m.avgTicketSize <= $maxTicket RETURN m")
    List<MerchantNode> findByAvgTicketSizeRange(@Param("minTicket") Double minTicket, @Param("maxTicket") Double maxTicket);
    
    // Recommend merchants based on sequence patterns (if user visited merchant X, suggest Y)
        @Query("""
                MATCH (u:User {externalId: $userExternalId})<-[:OWNS]-(c:Card)-[:TRANSACTED_WITH]->(m:Merchant)
                WITH collect(DISTINCT m.merchantId) AS visitedMerchants
                MATCH (visited:Merchant)-[s:FOLLOWED_BY]->(recommended:Merchant)
                WHERE visited.merchantId IN visitedMerchants 
                    AND NOT recommended.merchantId IN visitedMerchants
                RETURN recommended, SUM(s.sequenceCount) AS totalCount, AVG(s.confidenceScore) AS avgConfidence
                ORDER BY totalCount DESC, avgConfidence DESC
                LIMIT $limit
        """)
    List<Object> recommendMerchantsBasedOnSequences(
        @Param("userExternalId") String userExternalId,
        @Param("limit") Integer limit
    );
    
    // Location-based queries
    @Query("MATCH (m:Merchant)-[:IN_REGION]->(r:Region) WHERE r.city = $city RETURN m")
    List<MerchantNode> findByCity(@Param("city") String city);
    
    @Query("MATCH (m:Merchant)-[:IN_REGION]->(r:Region) WHERE r.country = $country RETURN m")
    List<MerchantNode> findByCountry(@Param("country") String country);
    
    @Query("MATCH (m:Merchant)-[:IN_REGION]->(r:Region) WHERE r.city = $city AND r.country = $country RETURN m")
    List<MerchantNode> findByLocation(@Param("city") String city, @Param("country") String country);
    
    // Find merchants popular in user's home city
    @Query("""
        MATCH (u:User {externalId: $userExternalId})
        MATCH (m:Merchant)-[:IN_REGION]->(r:Region)
        WHERE r.city = u.homeCity AND r.country = u.homeCountry AND m.popularityScore >= $minScore
        RETURN m
        ORDER BY m.popularityScore DESC
        LIMIT $limit
    """)
    List<MerchantNode> findPopularInUserCity(
        @Param("userExternalId") String userExternalId,
        @Param("minScore") Double minScore,
        @Param("limit") Integer limit
    );
    
    // Cross-region transaction detection: merchants with customers from multiple regions
    @Query("""
        MATCH (u:User)<-[:OWNS]-(c:Card)-[:TRANSACTED_WITH]->(m:Merchant)
        WITH m, COUNT(DISTINCT u.homeCountry) as countryCount, COUNT(DISTINCT u.homeCity) as cityCount
        WHERE countryCount >= $minCountries OR cityCount >= $minCities
        RETURN m, countryCount, cityCount
        ORDER BY countryCount DESC, cityCount DESC
    """)
    List<Object> findCrossRegionMerchants(
        @Param("minCountries") Integer minCountries,
        @Param("minCities") Integer minCities
    );
    
    // Find nearby merchants (same city as user)
    @Query("""
        MATCH (u:User {externalId: $userExternalId})
        MATCH (m:Merchant)-[:IN_REGION]->(r:Region)
        WHERE r.city = u.homeCity AND r.country = u.homeCountry
        RETURN m
        ORDER BY m.popularityScore DESC
    """)
    List<MerchantNode> findNearbyMerchants(@Param("userExternalId") String userExternalId);
}
