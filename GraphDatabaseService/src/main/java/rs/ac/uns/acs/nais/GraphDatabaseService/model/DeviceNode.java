package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

@Node("Device")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceNode {
    @Id
    @GeneratedValue
    private Long id;
    
    private String deviceId;
    private String os; // iOS/Android/Web
    private String model; // device model or browser UA family
}
