package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString
@Node("Category")
public class Category {

  @Id
  private String id;   // npr. "cat1" ili "GRO"

  private String name; // npr. "Groceries"
}
