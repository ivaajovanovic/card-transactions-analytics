package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardAcceptance {
    @Id
    @GeneratedValue
    private Long id;

    private Double minAmount;
    private Boolean contactless;
    private Boolean installmentsAllowed;

    @TargetNode
    private AcceptanceNode acceptance; // (network,type)
}
