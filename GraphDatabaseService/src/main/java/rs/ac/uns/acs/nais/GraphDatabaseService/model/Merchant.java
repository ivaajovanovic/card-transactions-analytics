package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Node("Merchant")
public class Merchant {
  @Id
  private String id;         // "m1"
  private String name;
  private String location;   // "Beograd"

  // (:Merchant)-[:BELONGS_TO]->(:Category)
  @ToString.Exclude
  @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
  private Category category;
}
