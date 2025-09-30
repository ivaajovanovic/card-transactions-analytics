package rs.ac.uns.acs.nais.GraphDatabaseService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient elasticsearchServiceClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://elastic-search-service:9080") // URL ElasticSearch servisa iz docker-compose
                .build();
    }
}