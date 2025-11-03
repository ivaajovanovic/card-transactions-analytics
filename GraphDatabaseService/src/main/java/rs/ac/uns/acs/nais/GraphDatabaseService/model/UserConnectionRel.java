package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.ConnectionType;
import lombok.*;
import java.time.Instant;

@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserConnectionRel {
    @Id
    @GeneratedValue
    private Long id;
    
    // Connection metadata
    private ConnectionType connectionType; // FAMILY, FRIEND, COLLEAGUE, HOUSEHOLD_MEMBER
    private Instant connectedSince; // when connection was established
    
    // Interaction metrics
    private Integer sharedMerchantCount; // merchants both users visit
    private Double spendingCorrelation; // correlation in spending patterns (-1 to 1)
    private Integer commonTransactionCount; // transactions at same merchants
    
    // Connection strength
    private Double connectionStrength; // calculated score (0-1) for recommendations
    
    @TargetNode
    private UserNode connectedUser;
}
