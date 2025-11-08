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
    
    private String merchantId; 
    private String name;
    private String mcc; //bespotreno
    private String brand; 
    private String email; 
    @JsonIgnore
    private String password; 

    private Set<String> tags; // posle za preporuke
    private Double avgTicketSize; 
    private PriceRange priceRange; // BUDGET, MODERATE, UPSCALE, LUXURY
    private Double popularityScore; // za prpeoruke

    @Relationship(type = "IN_CATEGORY")
    private CategoryNode category;

    @Relationship(type = "IN_REGION")
    private RegionNode region;

    @Relationship(type = "ACCEPTS")
    private Set<CardAcceptance> accepts; 
    
    @Relationship(type = "FOLLOWED_BY")
    private Set<MerchantSequenceRel> sequences;
}
