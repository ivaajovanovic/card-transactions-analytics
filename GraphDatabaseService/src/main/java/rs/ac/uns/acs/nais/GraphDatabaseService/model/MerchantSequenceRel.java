package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantSequenceRel {
    @Id
    @GeneratedValue
    private Long id;
    
    // Sequence metrics
    private Integer sequenceCount; // how many times merchantA -> merchantB occurred
    private Long avgTimeDelta; // average time between transactions (seconds)
    private Long medianTimeDelta; // median time between transactions
    
    // User segment patterns
    private String commonUserSegment; // which user segment follows this pattern most
    private Double confidenceScore; // statistical confidence (0-1)
    
    // Temporal patterns
    private Integer dayOfWeekPattern; // most common day for sequence (1-7)
    private String seasonPattern; // most common season for sequence
    
    // Financial patterns
    private Double avgSpendAtSecond; // average spend at second merchant
    private Double conversionRate; // % of users visiting first that visit second
    
    @TargetNode
    private MerchantNode followedByMerchant;
}
