package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.*;
import java.util.Set;

@Node("Card")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardNode {
    @Id
    @GeneratedValue
    private Long id;
    
    private String panHash; // hashed PAN
    private CardNetwork network;
    private CardType type;
    private String issuerBank;
    private String issuerCountry;
    private Double monthlyLimit; // per card
    private Double creditLine; // for credit utilization analytics

    @Relationship(type = "OWNS", direction = Relationship.Direction.INCOMING)
    private UserNode owner;

    // Outgoing relationship with properties to merchants
    // This models (Card)-[t:TRANSACTED_WITH]->(Merchant) where `t` is TransactionRel
    @Relationship(type = "TRANSACTED_WITH")
    private Set<TransactionRel> transactions;
}
