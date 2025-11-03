package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.UserPreferenceNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.LoyaltyTier;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends Neo4jRepository<UserPreferenceNode, Long> {
    
    // Find preferences by user external ID
    Optional<UserPreferenceNode> findByUserExternalId(String userExternalId);
    
    // Find users by loyalty tier
    List<UserPreferenceNode> findByLoyaltyTier(LoyaltyTier loyaltyTier);
    
    // Find high-value users (by spend threshold)
    @Query("MATCH (p:UserPreference) WHERE p.avgMonthlySpend >= $minSpend RETURN p ORDER BY p.avgMonthlySpend DESC")
    List<UserPreferenceNode> findHighValueUsers(@Param("minSpend") Double minSpend);
    
    // Find users who prefer specific category
    @Query("MATCH (p:UserPreference) WHERE $category IN p.preferredCategories RETURN p")
    List<UserPreferenceNode> findByPreferredCategory(@Param("category") String category);
    
    // Find users with similar preferences (collaborative filtering)
    @Query("""
        MATCH (p1:UserPreference {userExternalId: $userExternalId})
        MATCH (p2:UserPreference)
        WHERE p2.userExternalId <> $userExternalId
          AND ANY(cat IN p1.preferredCategories WHERE cat IN p2.preferredCategories)
        WITH p2, 
             SIZE([cat IN p1.preferredCategories WHERE cat IN p2.preferredCategories]) AS commonCategories,
             SIZE(p1.preferredCategories + p2.preferredCategories) AS totalCategories
        RETURN p2 
        ORDER BY toFloat(commonCategories) / totalCategories DESC 
        LIMIT $limit
    """)
    List<UserPreferenceNode> findSimilarUsers(
        @Param("userExternalId") String userExternalId, 
        @Param("limit") Integer limit
    );
    
    // Find users who favor specific merchant
    @Query("MATCH (p:UserPreference) WHERE $merchantId IN p.favoriteMerchantIds RETURN p")
    List<UserPreferenceNode> findByFavoriteMerchant(@Param("merchantId") String merchantId);
    
    // Find frequent transactors
    @Query("MATCH (p:UserPreference) WHERE p.txnFrequency >= $minFrequency RETURN p ORDER BY p.txnFrequency DESC")
    List<UserPreferenceNode> findFrequentTransactors(@Param("minFrequency") Integer minFrequency);
    
    // Get loyalty tier distribution
    @Query("MATCH (p:UserPreference) RETURN p.loyaltyTier AS tier, COUNT(p) AS count ORDER BY tier")
    List<Object> getLoyaltyTierDistribution();
    
    // Find users with specific channel preference
    @Query("MATCH (p:UserPreference) WHERE $channel IN p.preferredChannels RETURN p")
    List<UserPreferenceNode> findByPreferredChannel(@Param("channel") String channel);
}
