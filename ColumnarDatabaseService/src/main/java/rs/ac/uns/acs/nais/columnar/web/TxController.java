package rs.ac.uns.acs.nais.columnar.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.datastax.oss.driver.api.core.cql.Row;

import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByCategory;
import rs.ac.uns.acs.nais.columnar.model.TxByMerchant;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.repo.*;
import rs.ac.uns.acs.nais.columnar.service.CompensationService;
import rs.ac.uns.acs.nais.columnar.service.TxIngestService;
import rs.ac.uns.acs.nais.columnar.service.BatchProcessingService;
import rs.ac.uns.acs.nais.columnar.service.ElasticsearchIntegrationService;

@RestController
@RequestMapping("/api/tx")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TxController {

    private final TxIngestService ingest;
    private final ElasticsearchIntegrationService elasticsearchService;
    private final CompensationService compensation;
    private final BatchProcessingService batchService;
    private final TxByUserRepo userRepo;
    private final TxByMerchantRepo merchantRepo;
    private final TxByCategoryRepo categoryRepo;
    private final CqlTemplate cql;

    @PostMapping
    public void create(@Valid @RequestBody TransactionDTO dto) {
        System.out.println("=== ENTRY POINT === TxController.create() called with txId: " + (dto != null ? dto.getTxId() : "NULL"));
        System.err.println("=== ENTRY POINT === TxController.create() called with txId: " + (dto != null ? dto.getTxId() : "NULL"));
        System.out.println("=== ENTRY POINT === TransactionDTO details: merchantId=" + (dto != null ? dto.getMerchantId() : "NULL") + ", categoryId=" + (dto != null ? dto.getCategoryId() : "NULL"));
        System.out.flush();
        System.err.flush();
        log.error("=== ENTRY POINT === TxController.create() called with txId: {}", (dto != null ? dto.getTxId() : "NULL"));
        
        try {
            System.out.println("=== ENTRY POINT === About to call ingest.ingest()");
            System.out.println("=== ENTRY POINT === TxIngestService instance: " + ingest.getClass().getName());
            System.out.println("=== ENTRY POINT === TxIngestService hash: " + ingest.hashCode());
            System.out.println("=== ENTRY POINT === Ingest null check: " + (ingest == null ? "NULL!!!" : "NOT NULL"));
            log.error("=== ENTRY POINT === About to call ingest.ingest(), ingest is null: {}", (ingest == null));
            System.out.flush();
            
            // Call the normal transaction processing pipeline
            log.error("=== ENTRY POINT === CALLING ingest.ingest() NOW");
            ingest.ingest(dto);
            log.error("=== ENTRY POINT === ingest.ingest() COMPLETED");
            
            // MAIN PIPELINE: Direct Elasticsearch forwarding (TxIngestService has Spring proxy issues)
            log.error("=== PIPELINE === CALLING elasticsearchService.forwardTransactionToElasticsearch() as main pipeline");
            try {
                elasticsearchService.forwardTransactionToElasticsearch(dto);
                log.error("=== PIPELINE === elasticsearchService.forwardTransactionToElasticsearch() COMPLETED - END-TO-END SUCCESS");
            } catch (Exception esEx) {
                log.error("=== PIPELINE === elasticsearchService.forwardTransactionToElasticsearch() FAILED: {}", esEx.getMessage(), esEx);
                throw new RuntimeException("Elasticsearch forwarding failed", esEx);
            }
        } catch (Exception e) {
            System.out.println("=== ENTRY POINT === EXCEPTION in ingest.ingest(): " + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.err.println("=== ENTRY POINT === EXCEPTION in ingest.ingest(): " + e.getClass().getSimpleName() + " - " + e.getMessage());
            log.error("=== ENTRY POINT === EXCEPTION in ingest.ingest(): {} - {}", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
            throw e; // re-throw to maintain HTTP error response
        }
    }

    // READ - Get transaction by ID
    @GetMapping("/{txId}")
    public TransactionDTO getTransaction(@PathVariable UUID txId) {
        log.info("=== READ === Getting transaction: {}", txId);
        
        try {
            // Search across all transactions (this is a simple implementation)
            List<TxByUser> transactions = userRepo.findAll();
            
            for (TxByUser tx : transactions) {
                if (tx.getTxId().equals(txId)) {
                    log.info("=== READ === Found transaction: {}", txId);
                    
                    // Convert to DTO
                    return TransactionDTO.builder()
                        .txId(tx.getTxId())
                        .userId(tx.getKey().getUserId())
                        .cardId(tx.getCardId())
                        .merchantId(tx.getMerchantId())
                        .categoryId(tx.getCategoryId())
                        .amountCents(tx.getAmountCents())
                        .currency(tx.getCurrency())
                        .status(tx.getStatus())
                        .occurredAt(null) // Not stored in this table
                        .build();
                }
            }
            
            log.warn("=== READ === Transaction not found: {}", txId);
            throw new RuntimeException("Transaction not found: " + txId);
            
        } catch (Exception e) {
            log.error("=== READ === Error getting transaction {}: {}", txId, e.getMessage());
            throw new RuntimeException("Error getting transaction: " + e.getMessage(), e);
        }
    }

    // COMPLETELY NEW UPDATE ENDPOINT - Change transaction status directly
    @PutMapping("/{txId}/change-status")
    public ResponseEntity<String> changeTransactionStatus(@PathVariable UUID txId, @RequestParam String status) {
        log.info("=== CHANGE STATUS === Changing transaction {} status to: {}", txId, status);
        
        try {
            // Find the transaction using direct CQL
            String findCql = "SELECT user_id, tx_date, tx_time FROM transactions_by_user WHERE tx_id = ? ALLOW FILTERING";
            log.info("=== CHANGE STATUS === Finding transaction with CQL: {}", findCql);
            
            // Execute query directly with CQL
            List<Map<String, Object>> txResults = cql.query(findCql, 
                (row, rowNum) -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("user_id", row.getUuid("user_id"));
                    result.put("tx_date", row.getLocalDate("tx_date"));
                    result.put("tx_time", row.getUuid("tx_time"));
                    return result;
                },
                txId);
            
            if (txResults.isEmpty()) {
                log.warn("=== CHANGE STATUS === Transaction not found for ID: {}", txId);
                return ResponseEntity.notFound().build();
            }
            
            // Get key components
            Map<String, Object> txData = txResults.get(0);
            UUID foundUserId = (UUID) txData.get("user_id");
            java.time.LocalDate foundTxDate = (java.time.LocalDate) txData.get("tx_date");
            UUID foundTxTime = (UUID) txData.get("tx_time");
            
            log.info("=== CHANGE STATUS === Found transaction - userId: {}, txDate: {}, txTime: {}", foundUserId, foundTxDate, foundTxTime);
            
            // Update transaction status
            String updateCql = "UPDATE transactions_by_user SET status = ? WHERE user_id = ? AND tx_date = ? AND tx_time = ?";
            log.info("=== CHANGE STATUS === Executing update: {}", updateCql);
            
            cql.execute(updateCql, status, foundUserId, foundTxDate, foundTxTime);
            
            log.info("=== CHANGE STATUS === Successfully updated transaction {} status to: {}", txId, status);
            
            // Forward to Elasticsearch
            try {
                TransactionDTO elasticUpdate = TransactionDTO.builder()
                    .txId(txId)
                    .userId(foundUserId)
                    .status(status)
                    .build();
                
                elasticsearchService.forwardTransactionToElasticsearch(elasticUpdate);
                log.info("=== CHANGE STATUS === Updated transaction in Elasticsearch: {}", txId);
            } catch (Exception esEx) {
                log.warn("=== CHANGE STATUS === Failed to update Elasticsearch: {}", esEx.getMessage());
            }
            
            return ResponseEntity.ok("Transaction status changed successfully");
            
        } catch (Exception e) {
            log.error("=== CHANGE STATUS === Error changing transaction {}: {}", txId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error changing transaction status: " + e.getMessage());
        }
    }

    // DELETE - Delete transaction (soft delete by updating status)
    @DeleteMapping("/{txId}")
    public void deleteTransaction(@PathVariable UUID txId) {
        log.info("=== DELETE === Deleting transaction: {}", txId);
        
        try {
            // Soft delete by updating status
            changeTransactionStatus(txId, "DELETED");
            log.info("=== DELETE === Successfully deleted transaction: {}", txId);
            
        } catch (Exception e) {
            log.error("=== DELETE === Error deleting transaction {}: {}", txId, e.getMessage());
            throw new RuntimeException("Error deleting transaction: " + e.getMessage(), e);
        }
    }

    // READ ALL - Get all transactions for a user
    @GetMapping("/user/{userId}")
    public List<TransactionDTO> getUserTransactions(@PathVariable UUID userId) {
        log.info("=== READ ALL === Getting transactions for user: {}", userId);
        
        try {
            // Use CQL to find by user_id
            String query = "SELECT * FROM transactions_by_user WHERE user_id = ?";
            List<TxByUser> transactions = cql.query(query, 
                (row, rowNum) -> TxByUser.builder()
                    .key(rs.ac.uns.acs.nais.columnar.model.TxByUserKey.builder()
                        .userId(row.getUuid("user_id"))
                        .txDate(row.getLocalDate("tx_date"))
                        .txTime(row.getUuid("tx_time"))
                        .build())
                    .txId(row.getUuid("tx_id"))
                    .merchantId(row.getUuid("merchant_id"))
                    .cardId(row.getUuid("card_id"))
                    .categoryId(row.getUuid("category_id"))
                    .amountCents(row.getLong("amount_cents"))
                    .currency(row.getString("currency"))
                    .status(row.getString("status"))
                    .build(),
                userId);
            
            return transactions.stream()
                .map(tx -> TransactionDTO.builder()
                    .txId(tx.getTxId())
                    .userId(tx.getKey().getUserId())
                    .cardId(tx.getCardId())
                    .merchantId(tx.getMerchantId())
                    .categoryId(tx.getCategoryId())
                    .amountCents(tx.getAmountCents())
                    .currency(tx.getCurrency())
                    .status(tx.getStatus())
                    .occurredAt(null)
                    .build())
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            log.error("=== READ ALL === Error getting transactions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error getting user transactions: " + e.getMessage(), e);
        }
    }

    @GetMapping("/test-forward-last")
    public String testForwardLast() {
        System.out.println("=== TEST FORWARD === Direct ElasticsearchIntegrationService test for last transaction");
        System.err.println("=== TEST FORWARD === Direct ElasticsearchIntegrationService test for last transaction");
        log.error("=== TEST FORWARD === Direct ElasticsearchIntegrationService test for last transaction");
        
        try {
            // Create the exact transaction we just sent
            TransactionDTO testTx = TransactionDTO.builder()
                .txId(UUID.fromString("222e4567-e89b-12d3-a456-426614174000"))
                .userId(UUID.fromString("222e4567-e89b-12d3-a456-426614174001"))
                .cardId(UUID.fromString("222e4567-e89b-12d3-a456-426614174002"))
                .merchantId(UUID.fromString("222e4567-e89b-12d3-a456-426614174003"))
                .categoryId(UUID.fromString("222e4567-e89b-12d3-a456-426614174004"))
                .amountCents(99000L)
                .currency("EUR")
                .status("COMPLETED")
                .occurredAt(java.time.Instant.parse("2024-09-29T21:40:00Z"))
                .build();
                
            System.out.println("=== TEST FORWARD === About to call elasticsearchService.forwardTransactionToElasticsearch() for 222e4567");
            System.err.println("=== TEST FORWARD === About to call elasticsearchService.forwardTransactionToElasticsearch() for 222e4567");
            
            // Direct call to elasticsearch service
            elasticsearchService.forwardTransactionToElasticsearch(testTx);
            
            System.out.println("=== TEST FORWARD === ElasticsearchIntegrationService call completed successfully");
            System.err.println("=== TEST FORWARD === ElasticsearchIntegrationService call completed successfully");
            log.error("=== TEST FORWARD === ElasticsearchIntegrationService call completed successfully");
            
            return "SUCCESS: Direct ElasticsearchIntegrationService forwarding test completed for 222e4567";
        } catch (Exception e) {
            System.out.println("=== TEST FORWARD === EXCEPTION: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.err.println("=== TEST FORWARD === EXCEPTION: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            log.error("=== TEST FORWARD === EXCEPTION: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/test-elasticsearch")
    public String testElasticsearch() {
        System.out.println("=== TEST === Direct Elasticsearch test called");
        System.err.println("=== TEST === Direct Elasticsearch test called");
        log.error("=== TEST === Direct Elasticsearch test called");
        
        try {
            // Create a test transaction
            TransactionDTO testTx = TransactionDTO.builder()
                .txId(UUID.fromString("333e4567-e89b-12d3-a456-426614174999"))
                .userId(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"))
                .cardId(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"))
                .merchantId(UUID.fromString("123e4567-e89b-12d3-a456-426614174003"))
                .categoryId(UUID.fromString("123e4567-e89b-12d3-a456-426614174004"))
                .amountCents(25000L)
                .currency("RSD")
                .status("APPROVED")
                .occurredAt(java.time.Instant.parse("2025-01-01T12:00:00Z"))
                .build();
                
            System.out.println("=== TEST === About to call elasticsearchService.forwardTransactionToElasticsearch()");
            System.err.println("=== TEST === About to call elasticsearchService.forwardTransactionToElasticsearch()");
            
            elasticsearchService.forwardTransactionToElasticsearch(testTx);
            
            System.out.println("=== TEST === Elasticsearch forwarding completed successfully");
            System.err.println("=== TEST === Elasticsearch forwarding completed successfully");
            return "SUCCESS: Elasticsearch forwarding test completed";
            
        } catch (Exception e) {
            System.out.println("=== TEST === Exception during Elasticsearch test: " + e.getMessage());
            System.err.println("=== TEST === Exception during Elasticsearch test: " + e.getMessage());
            log.error("=== TEST === Exception during Elasticsearch test", e);
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/test-debug")
    public String testDebug() {
        System.out.println("=== TEST DEBUG === GET /test-debug called!");
        System.err.println("=== TEST DEBUG === GET /test-debug called!");
        log.error("=== TEST DEBUG === GET /test-debug called!");
        return "DEBUG TEST SUCCESSFUL";
    }

    @PostMapping("/batch")
    public void createBatch(@Valid @RequestBody List<TransactionDTO> transactions) {
        // Asinhrona obrada batch-a
        batchService.processBatchTransactions(transactions)
            .thenAccept(result -> {
                System.out.println("Batch processing completed: " + 
                    result.getSuccessfulTransactions() + "/" + result.getTotalTransactions() + 
                    " transactions processed successfully");
            });
    }

    @DeleteMapping("/users/{userId}/date/{date}/time/{timeuuid}")
    public boolean deleteByUserKey(@PathVariable UUID userId,
                                   @PathVariable LocalDate date,
                                   @PathVariable("timeuuid") UUID timeUuid) {
        return compensation.deleteByUserKey(userId, date, timeUuid);
    }

    @GetMapping("/api/tx/users/{userId}")
    public List<TxByUser> listUserDay(@PathVariable UUID userId,
                                    @RequestParam LocalDate date,
                                    @RequestParam(required = false, name = "before") UUID before,
                                    @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit) {
        if (before == null) {
            return userRepo.findDay(userId, date, limit);
        } else {
            return userRepo.findDayBefore(userId, date, before, limit);
        }
    }


    @GetMapping("/merchants/{merchantId}")
    public List<TxByMerchant> listMerchantDay(@PathVariable UUID merchantId,
                                              @RequestParam LocalDate date,
                                              @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit) {
        return merchantRepo.findDay(merchantId, date, limit);
    }

    @GetMapping("/categories/{categoryId}")
    public List<TxByCategory> listCategoryDay(@PathVariable UUID categoryId,
                                              @RequestParam LocalDate date,
                                              @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit) {
        return categoryRepo.findDay(categoryId, date, limit);
    }


    @DeleteMapping("/merchants/{merchantId}/date/{date}/time/{timeuuid}")
    public boolean deleteByMerchantKey(@PathVariable UUID merchantId,
                                    @PathVariable LocalDate date,
                                    @PathVariable("timeuuid") UUID timeUuid) {
        var rows = merchantRepo.findExact(merchantId, date, timeUuid);
        if (rows.isEmpty()) return false;
        var row = rows.get(0);
        return compensation.deleteByUserKey(row.getUserId(), date, timeUuid);
    }

    @DeleteMapping("/categories/{categoryId}/date/{date}/time/{timeuuid}")
    public boolean deleteByCategoryKey(@PathVariable UUID categoryId,
                                    @PathVariable LocalDate date,
                                    @PathVariable("timeuuid") UUID timeUuid) {
        var rows = categoryRepo.findExact(categoryId, date, timeUuid);
        if (rows.isEmpty()) return false;
        var row = rows.get(0);
        return compensation.deleteByUserKey(row.getUserId(), date, timeUuid);
    }

    @GetMapping("/users/{userId}/page")
    public List<TxByUser> pageUserDay(@PathVariable UUID userId,
                                    @RequestParam LocalDate date,
                                    @RequestParam(required=false) UUID before,
                                    @RequestParam(defaultValue="50") @Min(1) @Max(10000) int limit) {
        return before == null ? userRepo.findDay(userId, date, limit)
                            : userRepo.findDayBefore(userId, date, before, limit);
    }

    @GetMapping("/merchants/{merchantId}/page")
    public List<TxByMerchant> pageMerchantDay(@PathVariable UUID merchantId,
                                            @RequestParam LocalDate date,
                                            @RequestParam(required=false) UUID before,
                                            @RequestParam(defaultValue="50") @Min(1) @Max(10000) int limit) {
        return before == null ? merchantRepo.findDay(merchantId, date, limit)
                            : merchantRepo.findDayBefore(merchantId, date, before, limit);
    }

    @GetMapping("/categories/{categoryId}/page")
    public List<TxByCategory> pageCategoryDay(@PathVariable UUID categoryId,
                                            @RequestParam LocalDate date,
                                            @RequestParam(required=false) UUID before,
                                            @RequestParam(defaultValue="50") @Min(1) @Max(10000) int limit) {
        return before == null ? categoryRepo.findDay(categoryId, date, limit)
                            : categoryRepo.findDayBefore(categoryId, date, before, limit);
    }

}

