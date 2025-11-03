package rs.ac.uns.acs.nais.GraphDatabaseService.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card Graph Analytics API")
                        .version("v1")
                        .description("Graph analytics service for card transactions using Neo4j"));
    }
}
