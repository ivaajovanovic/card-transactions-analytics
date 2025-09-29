package rs.ac.uns.acs.nais.columnar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.model.TxByCategory;
import rs.ac.uns.acs.nais.columnar.model.TxByMerchant;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Servis za integraciju sa Elasticsearch servisom za analitiku transakcija
 * Omogućava forwarding transakcija za naprednu analitiku i pretragu
 */
@Service
@Slf4j
public class ElasticsearchIntegrationService {

    private final RestTemplate directRestTemplate;

    @Value("${elasticsearch.service.url:http://localhost:9080}")
    private String elasticsearchServiceUrl;

    public ElasticsearchIntegrationService(@Qualifier("directRestTemplate") RestTemplate directRestTemplate) {
        this.directRestTemplate = directRestTemplate;
    }

    /**
     * Prenos transakcije u Elasticsearch za analitiku
     */
    public void forwardTransactionToElasticsearch(TransactionDTO transaction) {
        try {
            System.out.println("�🚀🚀 PIPELINE FORWARD: ElasticsearchIntegrationService called for txId: " + transaction.getTxId());
            System.err.println("🚀🚀🚀 PIPELINE FORWARD: ElasticsearchIntegrationService called for txId: " + transaction.getTxId());
            System.out.flush();
            System.err.flush();
            
            System.out.println("�🔥🔥🔥 ELASTICSEARCH FORWARD CALLED! TxId: " + transaction.getTxId());
            String endpoint = elasticsearchServiceUrl + "/api/transactions";
            log.info("DEBUG: Forwarding transaction {} to: {}", transaction.getTxId(), endpoint);
            System.err.println("🔥🔥🔥 ELASTICSEARCH FORWARD ENDPOINT: " + endpoint);
            
            // Mapiranje u format koji Elasticsearch servis očekuje
            Map<String, Object> elasticTransaction = mapToElasticsearchFormat(transaction);
            log.info("DEBUG: Transaction data: {}", convertToJson(elasticTransaction));
            
            // Koristi plain HTTP connection da zaobiđe LoadBalancer
            java.net.URI uri = java.net.URI.create(endpoint);
            java.net.URL url = uri.toURL();
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // Konvertuj u JSON i pošalji
            String jsonData = convertToJson(elasticTransaction);
            log.info("DEBUG: Sending JSON to Elasticsearch: {}", jsonData);
            
            try (java.io.OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(jsonData.getBytes("UTF-8"));
                outputStream.flush();
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("🚀🚀🚀 PIPELINE SUCCESS: Transaction " + transaction.getTxId() + " forwarded to Elasticsearch (HTTP " + responseCode + ")");
                System.err.println("🚀🚀🚀 PIPELINE SUCCESS: Transaction " + transaction.getTxId() + " forwarded to Elasticsearch (HTTP " + responseCode + ")");
                log.info("Transaction {} forwarded to Elasticsearch successfully (HTTP {})", 
                        transaction.getTxId(), responseCode);
            } else {
                // Čitaj error response za bolji debugging
                String errorResponse = "";
                try (java.io.InputStream errorStream = connection.getErrorStream()) {
                    if (errorStream != null) {
                        errorResponse = new String(errorStream.readAllBytes(), "UTF-8");
                    }
                } catch (Exception e) {
                    log.debug("Could not read error response: {}", e.getMessage());
                }
                log.warn("Failed to forward transaction {} to Elasticsearch: HTTP {} - {}", 
                        transaction.getTxId(), responseCode, errorResponse);
            }
            
        } catch (Exception e) {
            log.error("Failed to forward transaction {} to Elasticsearch: {}", 
                     transaction.getTxId(), e.getMessage());
            // Ne prekidamo glavnu operaciju ako Elasticsearch nije dostupan
        }
    }

    /**
     * Poboljšana JSON konverzija sa boljim rukovanje tipovima
     */
    private String convertToJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number) {
                json.append(value.toString());
            } else if (value instanceof Boolean) {
                json.append(value.toString());
            } else {
                // Za sve ostale tipove, konvertuj u string
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Escape JSON karaktere
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * Batch prenos transakcija za bolju performansu
     */
    public void forwardBatchTransactionsToElasticsearch(List<TransactionDTO> transactions) {
        try {
            String endpoint = elasticsearchServiceUrl + "/api/transactions/batch";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            List<Map<String, Object>> elasticTransactions = transactions.stream()
                .map(this::mapToElasticsearchFormat)
                .toList();
            
            HttpEntity<List<Map<String, Object>>> requestEntity = 
                new HttpEntity<>(elasticTransactions, headers);
            
            directRestTemplate.postForObject(endpoint, requestEntity, String.class);
            log.info("Batch of {} transactions forwarded to Elasticsearch", transactions.size());
            
        } catch (Exception e) {
            log.error("Failed to forward batch transactions to Elasticsearch: {}", e.getMessage());
        }
    }

    /**
     * Prenos analitičkih podataka po kategorijama
     */
    public void forwardCategoryAnalytics(List<TxByCategory> categoryTransactions) {
        try {
            String endpoint = elasticsearchServiceUrl + "/api/analytics/categories";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<List<TxByCategory>> requestEntity = 
                new HttpEntity<>(categoryTransactions, headers);
            
            directRestTemplate.postForObject(endpoint, requestEntity, String.class);
            log.info("Category analytics forwarded to Elasticsearch");
            
        } catch (Exception e) {
            log.error("Failed to forward category analytics to Elasticsearch: {}", e.getMessage());
        }
    }

    /**
     * Mapiranje transakcije u format koji Elasticsearch servis očekuje
     */
    private Map<String, Object> mapToElasticsearchFormat(TransactionDTO transaction) {
        Map<String, Object> elasticTransaction = new HashMap<>();
        
        // Mapiranje na Elasticsearch Transaction model
        elasticTransaction.put("id", transaction.getTxId().toString());
        elasticTransaction.put("cardId", transaction.getCardId().toString());
        elasticTransaction.put("merchantId", transaction.getMerchantId().toString());
        
        // Konvertuj amountCents u amount (cents -> dollars/currency amount)
        double amount = transaction.getAmountCents() / 100.0;
        elasticTransaction.put("amount", amount);
        
        // Konvertuj occurredAt u date format
        elasticTransaction.put("date", transaction.getOccurredAt().toString());
        
        // Kreiraj opis na osnovu dostupnih podataka
        String description = String.format("Transaction %s - %s %s - Status: %s - Category: %s - User: %s", 
                                          transaction.getTxId(), 
                                          amount, 
                                          transaction.getCurrency(),
                                          transaction.getStatus(),
                                          transaction.getCategoryId(),
                                          transaction.getUserId());
        elasticTransaction.put("description", description);
        
        return elasticTransaction;
    }

    /**
     * Test konekcije sa Elasticsearch servisom
     */
    public boolean testElasticsearchConnection() {
        try {
            String healthEndpoint = elasticsearchServiceUrl + "/test/health";
            log.info("Testing connection to: {}", healthEndpoint);
            
            java.net.URI uri = java.net.URI.create(healthEndpoint);
            java.net.URL url = uri.toURL();
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            log.info("Response code: {}", responseCode);
            if (responseCode == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream()));
                String response = reader.readLine();
                reader.close();
                log.info("Elasticsearch service health check successful: {}", response);
                return true;
            } else {
                log.warn("Elasticsearch service health check failed with code: {}", responseCode);
                return false;
            }
        } catch (Exception e) {
            log.warn("Elasticsearch service not available: {}", e.getMessage());
            e.printStackTrace(); // Додај за детаљније логове
            return false;
        }
    }
}