package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.*;

@Node("Acceptance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceptanceNode {
    @Id
    @GeneratedValue
    private Long id;
    
    private CardNetwork network;
    private CardType type;
}
