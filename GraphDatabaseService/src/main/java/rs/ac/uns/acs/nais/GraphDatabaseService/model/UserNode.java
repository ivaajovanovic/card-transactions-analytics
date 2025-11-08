package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.*;
import java.time.Instant;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Node("User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNode {
    @Id
    @GeneratedValue
    private Long id; // za spring data neo4j operacije...u endpointima
    
    @Property("externalId")
    private String externalId; // za pretrage
    
    private String fullName;
    private String email;
    @JsonIgnore
    private String password; // dodati validacije
    
    private Integer age;
    private String occupation;
    private IncomeLevel incomeLevel;
    private Lifestage lifestage;
    
    // izbaciti
    private String segment; // Budget Traveler, Luxury Shopper
    private Boolean isFrequentTraveler;
    private Instant memberSince;
    
    @Relationship(type = "LIVES_IN")
    private RegionNode homeRegion;
    
    @Relationship(type = "HAS_PREFERENCES")
    private UserPreferenceNode preferences;
    
    @Relationship(type = "CONNECTED_TO")
    private Set<UserConnectionRel> connections;
}
