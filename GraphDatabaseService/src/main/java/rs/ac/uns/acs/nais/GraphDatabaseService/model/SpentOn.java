package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@RelationshipProperties
public class SpentOn {

  @RelationshipId @GeneratedValue
  private Long id;                 // ID relacije (ovo je mesto za @RelationshipId)

  @TargetNode
  private Transaction transaction; // krajnji čvor

  private Channel channel;         // POS/ECOM
  private Boolean cardPresent;     // true/false
}
