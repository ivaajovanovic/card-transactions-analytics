package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

@Node("Terminal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalNode {
    @Id
    @GeneratedValue
    private Long id;
    
    private String terminalId;
    private Boolean contactlessSupported;
}
