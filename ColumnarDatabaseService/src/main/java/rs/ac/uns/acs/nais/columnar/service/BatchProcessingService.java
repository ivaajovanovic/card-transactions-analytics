package rs.ac.uns.acs.nais.columnar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servis za batch obradu transakcija
 * Realizuje efikasnu obradu velikog broja transakcija
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchProcessingService {

    private final TxIngestService ingestService;
    private final ElasticsearchIntegrationService elasticsearchService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * Batch processing transakcija sa asinhronu obradom
     */
    @Transactional
    public CompletableFuture<BatchProcessingResult> processBatchTransactions(
            List<TransactionDTO> transactions) {
        
        return CompletableFuture.supplyAsync(() -> {
            BatchProcessingResult result = new BatchProcessingResult();
            
            int successCount = 0;
            int errorCount = 0;
            
            log.info("Starting batch processing of {} transactions", transactions.size());
            
            for (TransactionDTO transaction : transactions) {
                try {
                    // Obrada u Cassandra bazi
                    ingestService.ingest(transaction);
                    successCount++;
                    
                    // Logovanje svakih 100 obrađenih transakcija
                    if (successCount % 100 == 0) {
                        log.info("Processed {} transactions so far", successCount);
                    }
                    
                } catch (Exception e) {
                    log.error("Error processing transaction {}: {}", 
                             transaction.getTxId(), e.getMessage());
                    errorCount++;
                }
            }
            
            // Batch prenos u Elasticsearch
            try {
                elasticsearchService.forwardBatchTransactionsToElasticsearch(transactions);
                log.info("Batch forwarded to Elasticsearch successfully");
            } catch (Exception e) {
                log.error("Error forwarding batch to Elasticsearch: {}", e.getMessage());
            }
            
            result.setTotalTransactions(transactions.size());
            result.setSuccessfulTransactions(successCount);
            result.setFailedTransactions(errorCount);
            result.setProcessingCompleted(true);
            
            log.info("Batch processing completed: {} successful, {} failed out of {} total", 
                    successCount, errorCount, transactions.size());
            
            return result;
            
        }, executorService);
    }

    /**
     * Rezultat batch obrade
     */
    public static class BatchProcessingResult {
        private int totalTransactions;
        private int successfulTransactions;
        private int failedTransactions;
        private boolean processingCompleted;

        // Getteri i setteri
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

        public int getSuccessfulTransactions() { return successfulTransactions; }
        public void setSuccessfulTransactions(int successfulTransactions) { this.successfulTransactions = successfulTransactions; }

        public int getFailedTransactions() { return failedTransactions; }
        public void setFailedTransactions(int failedTransactions) { this.failedTransactions = failedTransactions; }

        public boolean isProcessingCompleted() { return processingCompleted; }
        public void setProcessingCompleted(boolean processingCompleted) { this.processingCompleted = processingCompleted; }

        public double getSuccessRate() {
            return totalTransactions > 0 ? (double) successfulTransactions / totalTransactions * 100 : 0;
        }
    }
}