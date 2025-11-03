package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;
import java.time.Instant;
import java.util.Set;

@Node("RewardProgram")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardProgram {
    @Id
    @GeneratedValue
    private Long id;
    private String issuerBank;
    private String name;
    private Instant startDate;
    private Instant endDate;
    @Relationship(type = "APPLIES_TO")
    private Set<CardNode> cards;
    @Relationship(type = "HAS_RULE")
    private Set<RewardRule> rules;
}
