package rs.ac.uns.acs.nais.columnar.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.columnar.saga.FraudDetectionSaga;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller za Saga pattern operacije
 * Omogućava pokretanje kompleksnih workflow-a koji span preko više mikroservisa
 */
@RestController
@RequestMapping("/api/saga")
@Slf4j
@RequiredArgsConstructor
public class SagaController {
    
    private final FraudDetectionSaga fraudDetectionSaga;
    
    /**
     * Pokretanje Fraud Detection Saga workflow-a
     * POST /api/saga/fraud-detection
     * 
     * Primer poziva:
     * {
     *   "transactionId": "tx12345",
     *   "userId": "user789"
     * }
     */
    @PostMapping("/fraud-detection")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> startFraudDetectionSaga(
            @RequestBody Map<String, String> request) {
        
        String transactionId = request.get("transactionId");
        String userId = request.get("userId");
        
        if (transactionId == null || userId == null) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Missing required fields",
                        "required", "transactionId, userId",
                        "provided", request
                    ))
            );
        }
        
        log.info("Starting Fraud Detection Saga for transaction: {} user: {}", transactionId, userId);
        
        return fraudDetectionSaga.executeFraudDetectionSaga(transactionId, userId)
            .thenApply(result -> {
                Map<String, Object> responseBody;
                if (result.startsWith("SAGA_SUCCESS")) {
                    responseBody = Map.of(
                        "status", "SUCCESS",
                        "message", result,
                        "transactionId", transactionId,
                        "userId", userId,
                        "timestamp", System.currentTimeMillis()
                    );
                    return ResponseEntity.ok(responseBody);
                } else if (result.startsWith("SAGA_COMPENSATED")) {
                    responseBody = Map.of(
                        "status", "COMPENSATED",
                        "message", result,
                        "transactionId", transactionId,
                        "userId", userId,
                        "timestamp", System.currentTimeMillis()
                    );
                    return ResponseEntity.status(206).body(responseBody);
                } else {
                    responseBody = Map.of(
                        "status", "FAILED",
                        "message", result,
                        "transactionId", transactionId,
                        "userId", userId,
                        "timestamp", System.currentTimeMillis()
                    );
                    return ResponseEntity.status(500).body(responseBody);
                }
            })
            .exceptionally(throwable -> {
                log.error("Saga execution failed", throwable);
                Map<String, Object> errorBody = Map.of(
                    "status", "ERROR",
                    "message", "Saga execution failed: " + throwable.getMessage(),
                    "transactionId", transactionId,
                    "userId", userId,
                    "timestamp", System.currentTimeMillis()
                );
                return ResponseEntity.status(500).body(errorBody);
            });
    }
    
    /**
     * Test endpoint za brzu proveru Saga funkcionalnosti
     * GET /api/saga/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSaga() {
        return ResponseEntity.ok(Map.of(
            "message", "Saga Controller is working",
            "availableEndpoints", Map.of(
                "fraudDetection", "POST /api/saga/fraud-detection",
                "test", "GET /api/saga/test"
            ),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Demonstracija Saga poziva za test transakciju
     * GET /api/saga/demo
     */
    @GetMapping("/demo")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> demonstrateSaga() {
        // Koristimo test podatke - moram koristiti postojeće UUID-jeve
        String demoTransactionId = "550e8400-e29b-41d4-a716-446655440000"; // Demo UUID
        String demoUserId = "123e4567-e89b-12d3-a456-426614174000"; // Demo UUID
        
        log.info("Starting demo Fraud Detection Saga");
        
        return fraudDetectionSaga.executeFraudDetectionSaga(demoTransactionId, demoUserId)
            .thenApply(result -> {
                Map<String, Object> demoResponse = Map.of(
                    "demo", true,
                    "result", result,
                    "transactionId", demoTransactionId,
                    "userId", demoUserId,
                    "timestamp", System.currentTimeMillis(),
                    "note", "This was a demonstration with test data"
                );
                return ResponseEntity.ok(demoResponse);
            });
    }
}