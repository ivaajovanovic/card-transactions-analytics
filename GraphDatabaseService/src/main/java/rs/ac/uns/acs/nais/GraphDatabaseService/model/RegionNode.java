package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

@Node("Region")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionNode {
    @Id
    @GeneratedValue
    private Long id;
    
    private String city;
    private String country;
    private String name; 
    private Double lat;
    private Double lon;
}
