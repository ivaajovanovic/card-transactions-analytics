package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.SagaTransactionResult;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.SuspiciousTransactionSagaService;

@RestController
@RequestMapping("/api/saga")
@RequiredArgsConstructor
public class SagaController {

    private final SuspiciousTransactionSagaService sagaService;

    /**
     * Endpoint za pokretanje SAGA pattern-a za sumnjive transakcije
     * Demonstrira distributed transaction između Neo4j i ElasticSearch servisa
     */
    @PostMapping("/process-suspicious-transactions")
    public ResponseEntity<SagaTransactionResult> processSuspiciousTransactions() {
        SagaTransactionResult result = sagaService.processSuspiciousTransactions();
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}