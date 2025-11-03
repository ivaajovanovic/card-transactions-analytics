package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.PriceRange;
import lombok.*;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Node("Merchant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantNode {
    @Id
    @GeneratedValue
    private Long id;
    
    private String merchantId; // external merchant id
    private String name;
    private String mcc; // MCC code
    private String brand; // brand chain
    private String email; // merchant contact/login email
    @JsonIgnore
    private String password; // demo-only: plain text password (do not use in production)

    // Recommendation & Analytics Fields
    private Set<String> tags; // e.g., "fast-food", "organic", "luxury"
    private Double avgTicketSize; // average transaction amount
    private PriceRange priceRange; // BUDGET, MODERATE, UPSCALE, LUXURY
    private Double popularityScore; // calculated metric for recommendations

    @Relationship(type = "IN_CATEGORY")
    private CategoryNode category;

    @Relationship(type = "IN_REGION")
    private RegionNode region;

    @Relationship(type = "ACCEPTS")
    private Set<CardAcceptance> accepts; // Relationship to accepted pairs
    
    @Relationship(type = "FOLLOWED_BY")
    private Set<MerchantSequenceRel> sequences; // Merchant chain patterns
}
