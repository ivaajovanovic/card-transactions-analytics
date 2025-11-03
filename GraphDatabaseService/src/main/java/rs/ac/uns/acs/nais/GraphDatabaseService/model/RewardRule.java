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
    private String categoryCode; // MCC or category
    private String merchantId; // optional, for merchant-specific offers
    private Double rewardRate; // e.g. 0.05 for 5% cashback
    private Double cap; // max reward per period
    private String conditions; // e.g. min spend, days, etc.
}
