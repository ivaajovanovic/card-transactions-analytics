package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

@Node("Category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryNode {
    @Id
    @GeneratedValue
    private Long id;
    
    private String code; // e.g., MCC
    private String name;
    private String parentCode; 
}
