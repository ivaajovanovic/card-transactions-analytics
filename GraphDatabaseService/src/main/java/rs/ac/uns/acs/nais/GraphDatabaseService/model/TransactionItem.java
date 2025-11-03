package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

@Node("TransactionItem")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItem {
    @Id
    @GeneratedValue
    private Long id;
    
    private String sku;
    private String name;
    private Double unitPrice;
    private Integer qty;

    @Relationship(type = "IN_CATEGORY")
    private CategoryNode category;
}
