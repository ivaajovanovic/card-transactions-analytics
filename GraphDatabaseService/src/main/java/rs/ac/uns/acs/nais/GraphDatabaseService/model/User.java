package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Node("User")
public class User {
  @Id
  private String id;         // "u1"
  private String name;
  private String email;

  @ToString.Exclude
  @Relationship(type = "OWNS", direction = Relationship.Direction.OUTGOING)
  private Set<Card> cards = new HashSet<>();
}
