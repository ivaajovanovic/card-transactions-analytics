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
    private Long id;
    
    @Property("externalId")
    private String externalId; // for mapping to auth system
    
    private String fullName;
    private String email;
    @JsonIgnore
    private String password; // demo-only: plain text password (do not use in production)
    
    // Demographics
    private Integer age;
    private String occupation;
    private IncomeLevel incomeLevel;
    private Lifestage lifestage;
    
    // Behavioral
    private String segment; // segment/cluster label (e.g., "Budget Traveler")
    private Boolean isFrequentTraveler;
    private Instant memberSince;
    
    // Location
    private String homeCity;
    private String homeCountry;
    
    // Relationships
    @Relationship(type = "HAS_PREFERENCES")
    private UserPreferenceNode preferences;
    
    @Relationship(type = "CONNECTED_TO")
    private Set<UserConnectionRel> connections;
}
