package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

@Node("RewardRule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardRule {
    @Id
    @GeneratedValue
    private Long id;
    private String categoryCode;
    private String merchantId; 
    private Double rewardRate; 
    private Double cap; 
    private String conditions; 
}
