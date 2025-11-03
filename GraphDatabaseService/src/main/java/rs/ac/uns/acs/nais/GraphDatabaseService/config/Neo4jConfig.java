package rs.ac.uns.acs.nais.GraphDatabaseService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "rs.ac.uns.acs.nais.GraphDatabaseService.repository")
public class Neo4jConfig {
    // Additional Neo4j configuration can be added here if needed
}
