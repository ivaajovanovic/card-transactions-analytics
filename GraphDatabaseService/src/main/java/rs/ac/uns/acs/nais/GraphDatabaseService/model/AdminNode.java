package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Node("Admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNode {
    @Id
    @GeneratedValue
    private Long id;

    private String fullName;
    private String email;
    @JsonIgnore
    private String password; // demo-only: plain text password
}
