package rs.ac.uns.acs.nais.columnar.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.columnar.web.TxController;
import rs.ac.uns.acs.nais.columnar.dto.FraudAnalysisRequest;
import rs.ac.uns.acs.nais.columnar.dto.FraudAnalysisResult;
import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.service.ElasticsearchIntegrationService;
import rs.ac.uns.acs.nais.columnar.service.QueryService;

import java.util.concurrent.CompletableFuture;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fraud Detection Saga Implementation
 * Koordinira proces detekcije prevare između Cassandra i Elasticsearch baza
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionSaga {
    
    private final TxController txController;
    private final ElasticsearchIntegrationService elasticsearchService;
    private final QueryService queryService;
    
    /**
     * Glavni Saga workflow za fraud detection
     * Koristi compensating actions ako dođe do greške
     */
    public CompletableFuture<String> executeFraudDetectionSaga(String transactionIdStr, String userIdStr) {
        log.info("Starting Fraud Detection Saga for transaction: {} user: {}", transactionIdStr, userIdStr);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Parse UUID-ove
                UUID transactionId = UUID.fromString(transactionIdStr);
                UUID userId = UUID.fromString(userIdStr);
                
                // STEP 1: Proveri da li transakcija postojи u Cassandra
                TransactionDTO transaction = txController.getTransaction(transactionId);
                if (transaction == null) {
                    log.error("Transaction not found in Cassandra: {}", transactionId);
                    return "SAGA_FAILED: Transaction not found";
                }
                
                String originalStatus = transaction.getStatus();
                log.info("Original transaction status: {}", originalStatus);
                
                // STEP 2: Ažuriraj status na UNDER_REVIEW u Cassandra
                log.info("Step 1: Updating transaction status to UNDER_REVIEW in Cassandra");
                var reviewResult = txController.changeTransactionStatus(transactionId, "UNDER_REVIEW");
                if (reviewResult.getStatusCode().isError()) {
                    log.error("Failed to update status to UNDER_REVIEW");
                    return "SAGA_FAILED: Could not set UNDER_REVIEW status";
                }
                
                // STEP 3: Pripremi fraud analysis podatke
                log.info("Step 2: Preparing fraud analysis data");
                List<rs.ac.uns.acs.nais.columnar.model.TxByUser> userTransactions = 
                    queryService.getUserTransactionsToday(userId, 50);
                
                var fraudRequest = FraudAnalysisRequest.builder()
                    .transactionId(transactionId)
                    .userId(userId)
                    .recentTransactions(userTransactions.stream()
                        .map(tx -> FraudAnalysisRequest.RecentTransactionDTO.builder()
                            .txId(tx.getTxId())
                            .amountCents(tx.getAmountCents())
                            .merchantId(tx.getMerchantId().toString())
                            .category(tx.getCategoryId().toString())
                            .timestamp(tx.getKey().getTxDate().toString())
                            .location("Unknown") // TxByUser nema location polje
                            .build())
                        .collect(Collectors.toList()))
                    .userProfile(FraudAnalysisRequest.UserBehaviorProfile.builder()
                        .avgDailyTransactions(calculateAverageDaily(userTransactions))
                        .avgTransactionAmount(calculateAverageAmount(userTransactions))
                        .preferredMerchants(extractPreferredMerchants(userTransactions))
                        .usualLocations(extractUsualLocations(userTransactions))
                        .build())
                    .build();
                
                // STEP 4: Pošalji na Elasticsearch fraud analysis
                log.info("Step 3: Sending to Elasticsearch for fraud analysis");
                FraudAnalysisResult fraudResult = elasticsearchService.performFraudAnalysis(fraudRequest);
                
                // STEP 5: Donesi odluku na osnovu fraud score
                String finalStatus = decideFinalStatus(fraudResult);
                log.info("Step 4: Fraud analysis complete. Decision: {}", finalStatus);
                
                // STEP 6: Ažuriraj finalni status u Cassandra
                log.info("Step 5: Updating final status to {} in Cassandra", finalStatus);
                var finalResult = txController.changeTransactionStatus(transactionId, finalStatus);
                
                if (finalResult.getStatusCode().isError()) {
                    // COMPENSATION: Vrati na prethodni status
                    log.error("Failed to set final status, performing compensation");
                    compensateTransaction(transactionId, originalStatus);
                    return "SAGA_COMPENSATED: Final status update failed";
                }
                
                log.info("Fraud Detection Saga completed successfully. Final status: {}", finalStatus);
                return String.format("SAGA_SUCCESS: Transaction %s status changed to %s (fraud score: %.2f)", 
                    transactionIdStr, finalStatus, fraudResult.getFraudScore());
                
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format", e);
                return "SAGA_FAILED: Invalid transaction or user ID format";
            } catch (Exception e) {
                log.error("Fraud Detection Saga failed with exception", e);
                // COMPENSATION: Pokušaj da vratiš na PENDING status
                try {
                    UUID transactionId = UUID.fromString(transactionIdStr);
                    compensateTransactionOnError(transactionId);
                } catch (Exception compEx) {
                    log.error("Compensation also failed", compEx);
                }
                return "SAGA_FAILED: " + e.getMessage();
            }
        });
    }
    
    /**
     * Odlučuje finalni status na osnovu fraud analysis rezultata
     */
    private String decideFinalStatus(FraudAnalysisResult result) {
        if (result.getFraudScore() >= 0.8) {
            return "BLOCKED";  // Visok rizik - blokiraj
        } else if (result.getFraudScore() >= 0.5) {
            return "PENDING_REVIEW";  // Srednji rizik - pregled
        } else {
            return "APPROVED";  // Nizak rizik - odobri
        }
    }
    
    /**
     * Compensating action - vraća transakciju na prethodni status
     */
    private void compensateTransaction(UUID transactionId, String originalStatus) {
        try {
            log.info("Compensating transaction {} to original status: {}", transactionId, originalStatus);
            txController.changeTransactionStatus(transactionId, originalStatus);
        } catch (Exception e) {
            log.error("Compensation failed for transaction: {}", transactionId, e);
        }
    }
    
    /**
     * Compensating action kada ne znamo originalni status
     */
    private void compensateTransactionOnError(UUID transactionId) {
        try {
            log.info("Compensating transaction {} to PENDING status due to error", transactionId);
            txController.changeTransactionStatus(transactionId, "PENDING");
        } catch (Exception e) {
            log.error("Error compensation failed for transaction: {}", transactionId, e);
        }
    }
    
    // Helper methods za analizu korisničkih podataka
    private Integer calculateAverageDaily(List<rs.ac.uns.acs.nais.columnar.model.TxByUser> transactions) {
        return transactions.size(); // Simplified for demo
    }
    
    private Double calculateAverageAmount(List<rs.ac.uns.acs.nais.columnar.model.TxByUser> transactions) {
        if (transactions.isEmpty()) return 0.0;
        return transactions.stream()
            .mapToDouble(tx -> tx.getAmountCents() / 100.0) // Convert cents to dollars
            .average()
            .orElse(0.0);
    }
    
    private List<String> extractPreferredMerchants(List<rs.ac.uns.acs.nais.columnar.model.TxByUser> transactions) {
        return transactions.stream()
            .map(tx -> tx.getMerchantId().toString())
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
    }
    
    private List<String> extractUsualLocations(List<rs.ac.uns.acs.nais.columnar.model.TxByUser> transactions) {
        // TxByUser model nema location polje, koristimo placeholder
        return List.of("Primary Location", "Secondary Location");
    }
}