package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Node("Card")
public class Card {
  @Id
  private String id;          // "c1"
  private CardType type;      // CREDIT/DEBIT
  private Double limit;       // opc.

  // (:Card)-[SPENT_ON {…}]->(:Transaction)
  @ToString.Exclude
  @Relationship(type = "SPENT_ON", direction = Relationship.Direction.OUTGOING)
  private Set<SpentOn> spends = new HashSet<>();
}
