package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Transaction;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.UserActivity;

@Configuration
@EnableElasticsearchRepositories(basePackages = "rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository")
@Slf4j
public class ElasticsearchConfig extends ElasticsearchConfiguration {
    
    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;
    
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUri.replace("http://", ""))
                .build();
    }
    
    @Bean
    public CommandLineRunner createIndicesAndMappings(ElasticsearchOperations elasticsearchOperations) {
        return args -> {
            try {
                log.info("Creating Elasticsearch indices and mappings...");
                
                // Create transactions index with proper settings
                if (!elasticsearchOperations.indexOps(Transaction.class).exists()) {
                    elasticsearchOperations.indexOps(Transaction.class).create();
                    elasticsearchOperations.indexOps(Transaction.class).putMapping();
                    log.info("Created 'transactions' index with proper mappings");
                } else {
                    log.info("'transactions' index already exists");
                }
                
                // Create user_activity index with proper settings
                if (!elasticsearchOperations.indexOps(UserActivity.class).exists()) {
                    elasticsearchOperations.indexOps(UserActivity.class).create();
                    elasticsearchOperations.indexOps(UserActivity.class).putMapping();
                    log.info("Created 'user_activity' index with proper mappings");
                } else {
                    log.info("'user_activity' index already exists");
                }
                
                log.info("Elasticsearch indices setup completed successfully!");
                
            } catch (Exception e) {
                log.error("Error setting up Elasticsearch indices: {}", e.getMessage(), e);
            }
        };
    }
}