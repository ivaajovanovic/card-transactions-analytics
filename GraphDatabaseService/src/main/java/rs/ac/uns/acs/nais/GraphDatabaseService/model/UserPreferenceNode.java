package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.LoyaltyTier;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.TimeOfDay;
import lombok.*;
import java.util.Set;

@Node("UserPreference")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceNode {
    @Id
    @GeneratedValue
    private Long id;
    
    // User reference
    private String userExternalId; // links to UserNode.externalId
    
    // Category Preferences
    private Set<String> preferredCategories; // MCC categories
    
    // Time Preferences
    private Set<TimeOfDay> favoriteTimeSlots; // when user typically transacts
    
    // Spending Behavior
    private Double avgMonthlySpend; // average monthly transaction total
    private Integer txnFrequency; // transactions per month
    
    // Loyalty & Engagement
    private LoyaltyTier loyaltyTier; // BRONZE, SILVER, GOLD, PLATINUM
    private Integer loyaltyPoints; // accumulated points
    
    // Merchant Preferences
    private Set<String> favoriteMerchantIds; // frequently visited merchants
    private Set<String> avoidedMerchantIds; // merchants user avoids
    
    // Channel Preferences
    private Set<String> preferredChannels; // e.g., ONLINE, IN_STORE, MOBILE
    
    // Relationship to user
    @Relationship(type = "HAS_PREFERENCES", direction = Relationship.Direction.INCOMING)
    private UserNode user;
}
