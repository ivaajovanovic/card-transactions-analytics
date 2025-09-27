package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import java.time.OffsetDateTime;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Node("Transaction")
public class Transaction {
  @Id
  private String id;                // "tx1"
  private Double amount;
  private OffsetDateTime date;      // ISO-8601
  private String description;

  // (:Transaction)-[:PROCESSED_AT]->(:Merchant)
  @ToString.Exclude
  @Relationship(type = "PROCESSED_AT", direction = Relationship.Direction.OUTGOING)
  private Merchant merchant;
}
