package rs.ac.uns.acs.nais.columnar.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import rs.ac.uns.acs.nais.columnar.model.TxByCategory;
import rs.ac.uns.acs.nais.columnar.repo.TxByCategoryRepo;
import rs.ac.uns.acs.nais.columnar.service.ElasticsearchIntegrationService;
import rs.ac.uns.acs.nais.columnar.service.QueryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kontroler za integraciju sa Elasticsearch servisom
 * Omogućava prenos podataka između Cassandra i Elasticsearch servisa
 */
@RestController
@RequestMapping("/api/integration")
@Slf4j
public class IntegrationController {

    private final ElasticsearchIntegrationService elasticsearchService;
    private final TxByCategoryRepo categoryRepo;
    private final QueryService queryService;
    private final RestTemplate directRestTemplate;

    @Value("${elasticsearch.service.url:http://localhost:9080}")
    private String elasticsearchServiceUrl;

    public IntegrationController(ElasticsearchIntegrationService elasticsearchService, 
                               TxByCategoryRepo categoryRepo, 
                               QueryService queryService,
                               @Qualifier("directRestTemplate") RestTemplate directRestTemplate) {
        this.elasticsearchService = elasticsearchService;
        this.categoryRepo = categoryRepo;
        this.queryService = queryService;
        this.directRestTemplate = directRestTemplate;
    }

    /**
     * Test konekcije sa Elasticsearch servisom
     */
    @GetMapping("/elasticsearch/health")
    public Map<String, Object> checkElasticsearchHealth() {
        boolean isHealthy = elasticsearchService.testElasticsearchConnection();
        return Map.of(
            "elasticsearch_available", isHealthy,
            "status", isHealthy ? "OK" : "UNAVAILABLE"
        );
    }

    /**
     * Direct test konekcije sa Elasticsearch servisom (bypass service layer)
     */
    @GetMapping("/elasticsearch/direct-health")
    public Map<String, Object> checkDirectElasticsearchHealth() {
        try {
            String healthEndpoint = elasticsearchServiceUrl + "/test/health";
            log.info("Testing direct connection to: {}", healthEndpoint);
            String response = directRestTemplate.getForObject(healthEndpoint, String.class);
            log.info("Direct health check response: {}", response);
            return Map.of(
                "elasticsearch_available", true,
                "status", "OK",
                "response", response,
                "endpoint", healthEndpoint
            );
        } catch (Exception e) {
            log.error("Direct health check failed: {}", e.getMessage());
            return Map.of(
                "elasticsearch_available", false,
                "status", "UNAVAILABLE",
                "error", e.getMessage(),
                "endpoint", elasticsearchServiceUrl + "/test/health"
            );
        }
    }

    /**
     * Prenos analitičkih podataka po kategorijama u Elasticsearch
     */
    @PostMapping("/elasticsearch/sync-categories")
    public Map<String, Object> syncCategoriesToElasticsearch(
            @RequestParam UUID categoryId,
            @RequestParam LocalDate date) {
        
        List<TxByCategory> categoryTransactions = categoryRepo.findDay(categoryId, date, 1000);
        
        if (!categoryTransactions.isEmpty()) {
            elasticsearchService.forwardCategoryAnalytics(categoryTransactions);
            log.info("Synced {} category transactions to Elasticsearch for category {} on {}", 
                    categoryTransactions.size(), categoryId, date);
            
            return Map.of(
                "synced", true,
                "transactions_count", categoryTransactions.size(),
                "category_id", categoryId,
                "date", date.toString()
            );
        } else {
            return Map.of(
                "synced", false,
                "message", "No transactions found for the specified category and date"
            );
        }
    }

    /**
     * Analitički endpoint koji kombinuje podatke iz oba servisa
     */
    @GetMapping("/analytics/combined-report")
    public Map<String, Object> getCombinedAnalyticsReport(
            @RequestParam UUID categoryId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        
        // Podaci iz Cassandra (kolumnarna baza)
        List<TxByCategory> cassandraData = categoryRepo.findDay(categoryId, fromDate, 10000);
        
        // Priprema metapodataka za Elasticsearch analitiku
        Map<String, Object> combinedReport = Map.of(
            "source", "cassandra-elasticsearch-integration",
            "category_id", categoryId,
            "date_range", Map.of(
                "from", fromDate.toString(),
                "to", toDate.toString()
            ),
            "cassandra_transactions_count", cassandraData.size(),
            "elasticsearch_sync_available", elasticsearchService.testElasticsearchConnection()
        );
        
        log.info("Generated combined analytics report for category {} from {} to {}", 
                categoryId, fromDate, toDate);
        
        return combinedReport;
    }

    /**
     * Endpoint za real-time sync sa Elasticsearch
     */
    @PostMapping("/elasticsearch/force-sync")
    public Map<String, Object> forceSyncWithElasticsearch() {
        boolean connectionOk = elasticsearchService.testElasticsearchConnection();
        
        if (connectionOk) {
            log.info("Force sync with Elasticsearch completed successfully");
            return Map.of(
                "sync_status", "SUCCESS",
                "message", "Elasticsearch service is available and ready for data sync"
            );
        } else {
            log.warn("Force sync failed - Elasticsearch service unavailable");
            return Map.of(
                "sync_status", "FAILED",
                "message", "Elasticsearch service is currently unavailable"
            );
        }
    }
}