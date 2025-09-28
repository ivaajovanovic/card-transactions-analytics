package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.SagaTransactionResult;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.SuspiciousTransactionRequest;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuspiciousTransactionSagaService {

    private final Neo4jClient neo4j;
    private final WebClient elasticsearchServiceClient;

    /**
     * SAGA Orchestrator - detektuje i označava sumnjive transakcije
     * Implementira Orchestration pattern za distributed transaction
     */
    public SagaTransactionResult processSuspiciousTransactions() {
        log.info("Starting SAGA: Suspicious Transaction Detection and Flagging");
        
        try {
            // Korak 1: Detektuj sumnjive transakcije u Neo4j
            List<SuspiciousTransactionRequest> suspiciousTransactions = detectSuspiciousTransactions();
            
            if (suspiciousTransactions.isEmpty()) {
                return new SagaTransactionResult("SAGA_001", true, "DETECTION", "No suspicious transactions found", null);
            }

            log.info("Found {} suspicious transactions", suspiciousTransactions.size());

            // Korak 2: Označi kao sumnjive u Neo4j
            long flaggedInNeo4j = flagTransactionsInNeo4j(suspiciousTransactions);
            
            if (flaggedInNeo4j == 0) {
                return new SagaTransactionResult("SAGA_002", false, "NEO4J_FLAGGING", "Failed to flag transactions in Neo4j", null);
            }

            // Korak 3: Ažuriraj UserActivity u ElasticSearch
            boolean elasticsearchUpdated = updateUserActivityInElasticsearch(suspiciousTransactions);
            
            if (!elasticsearchUpdated) {
                // Rollback: Ukloni flag-ove iz Neo4j
                log.error("ElasticSearch update failed, rolling back Neo4j changes");
                rollbackNeo4jFlags(suspiciousTransactions);
                return new SagaTransactionResult("SAGA_003", false, "ELASTICSEARCH_UPDATE", "Failed to update ElasticSearch, rolled back Neo4j changes", null);
            }

            log.info("SAGA completed successfully: {} transactions processed", flaggedInNeo4j);
            return new SagaTransactionResult("SAGA_004", true, "COMPLETED", 
                String.format("Successfully processed %d suspicious transactions", flaggedInNeo4j), 
                Map.of("processedCount", flaggedInNeo4j));

        } catch (Exception e) {
            log.error("SAGA failed with exception: {}", e.getMessage(), e);
            return new SagaTransactionResult("SAGA_ERROR", false, "EXCEPTION", e.getMessage(), null);
        }
    }

    /**
     * Korak 1: Detektuje sumnjive transakcije na osnovu business logike
     */
    private List<SuspiciousTransactionRequest> detectSuspiciousTransactions() {
        // Prvo proveravamo već označene sumnjive transakcije
        String flaggedQuery = """
            MATCH (t:Transaction)-[:MADE_WITH]->(c:Card)<-[:OWNS]-(u:User)
            WHERE t.suspicious = true
            RETURN t.id AS transactionId, u.id AS userId, c.id AS cardId, t.amount AS amount,
                   'Already Flagged as Suspicious' AS reason
            LIMIT 20
            """;

        List<SuspiciousTransactionRequest> flaggedTransactions = neo4j.query(flaggedQuery)
                .fetchAs(SuspiciousTransactionRequest.class)
                .mappedBy((typeSystem, record) -> 
                    new SuspiciousTransactionRequest(
                        record.get("transactionId").asString(),
                        record.get("userId").asString(),
                        record.get("cardId").asString(),
                        record.get("amount").asDouble(),
                        record.get("reason").asString()
                    )
                )
                .all()
                .stream()
                .toList();

        if (!flaggedTransactions.isEmpty()) {
            return flaggedTransactions;
        }

        // Ako nema već označenih, traži nove sumnjive transakcije
        String query = """
            MATCH (t:Transaction)-[:MADE_WITH]->(c:Card)<-[:OWNS]-(u:User)
            WHERE (t.suspicious IS NULL OR t.suspicious = false)
            AND (t.amount > 5000 OR t.amount < 10) // Kriterijum: previše visoka ili suspiciously niska suma
            RETURN t.id AS transactionId, u.id AS userId, c.id AS cardId, t.amount AS amount,
                   CASE 
                     WHEN t.amount > 5000 THEN 'High Amount (>5000)'
                     WHEN t.amount < 10 THEN 'Suspiciously Low Amount (<10)'
                     ELSE 'Other Criteria'
                   END AS reason
            LIMIT 10
            """;

        return neo4j.query(query)
                .fetchAs(SuspiciousTransactionRequest.class)
                .mappedBy((typeSystem, record) -> 
                    new SuspiciousTransactionRequest(
                        record.get("transactionId").asString(),
                        record.get("userId").asString(),
                        record.get("cardId").asString(),
                        record.get("amount").asDouble(),
                        record.get("reason").asString()
                    )
                )
                .all()
                .stream()
                .toList();
    }

    /**
     * Korak 2: Označava transakcije kao sumnjive u Neo4j
     */
    private long flagTransactionsInNeo4j(List<SuspiciousTransactionRequest> suspiciousTransactions) {
        List<String> transactionIds = suspiciousTransactions.stream()
                .map(SuspiciousTransactionRequest::getTransactionId)
                .toList();

        String query = """
            UNWIND $transactionIds AS txId
            MATCH (t:Transaction {id: txId})
            SET t.suspicious = true, t.flaggedAt = datetime()
            RETURN count(t) AS flaggedCount
            """;

        return neo4j.query(query)
                .bind(transactionIds).to("transactionIds")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }

    /**
     * Korak 3: Ažurira UserActivity u ElasticSearch servisu
     */
    private boolean updateUserActivityInElasticsearch(List<SuspiciousTransactionRequest> suspiciousTransactions) {
        try {
            // Grupišemo po userId-u da znamo koliko sumljivih transakcija ima svaki korisnik
            Map<String, Long> suspiciousCountByUser = suspiciousTransactions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        SuspiciousTransactionRequest::getUserId,
                        java.util.stream.Collectors.counting()
                    ));

            for (Map.Entry<String, Long> entry : suspiciousCountByUser.entrySet()) {
                String userId = entry.getKey();
                Long suspiciousCount = entry.getValue();

                // HTTP poziv ka ElasticSearch servisu
                Boolean result = elasticsearchServiceClient
                        .post()
                        .uri("/api/user-activities/update-suspicious")
                        .bodyValue(Map.of(
                            "userId", userId,
                            "suspiciousTransactionCount", suspiciousCount
                        ))
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block(); // Blokira jer je ovo deo transakcije

                if (result == null || !result) {
                    log.error("Failed to update ElasticSearch for user: {}", userId);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Error updating ElasticSearch: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Rollback: Uklanja flag-ove za sumnjive transakcije iz Neo4j
     */
    private void rollbackNeo4jFlags(List<SuspiciousTransactionRequest> suspiciousTransactions) {
        List<String> transactionIds = suspiciousTransactions.stream()
                .map(SuspiciousTransactionRequest::getTransactionId)
                .toList();

        String rollbackQuery = """
            UNWIND $transactionIds AS txId
            MATCH (t:Transaction {id: txId})
            REMOVE t.suspicious, t.flaggedAt
            RETURN count(t) AS rolledBackCount
            """;

        Long rolledBack = neo4j.query(rollbackQuery)
                .bind(transactionIds).to("transactionIds")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);

        log.info("Rolled back {} transaction flags in Neo4j", rolledBack);
    }
}