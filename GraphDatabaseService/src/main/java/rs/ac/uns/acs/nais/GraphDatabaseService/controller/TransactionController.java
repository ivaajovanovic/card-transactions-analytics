package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Transaction;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.ITransactionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final ITransactionService txService;

  @PostMapping
  public ResponseEntity<Transaction> create(@RequestBody Transaction t) {
    Transaction saved = txService.create(t);
    return ResponseEntity.created(URI.create("/api/transactions/" + saved.getId())).body(saved);
  }

  @GetMapping
  public ResponseEntity<List<Transaction>> findAll() {
    return ResponseEntity.ok(txService.list());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Transaction> findById(@PathVariable String id) {
    return txService.get(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Transaction> update(@PathVariable String id, @RequestBody Transaction t) {
    t.setId(id);
    return ResponseEntity.ok(txService.update(t));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    txService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // --- RELACIJE: PROCESSED_AT (Transaction -> Merchant) ---
  @PostMapping("/{txId}/processed-at/{merchantId}")
  public ResponseEntity<Void> processedAt(@PathVariable String txId, @PathVariable String merchantId) {
    txService.processedAt(txId, merchantId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{txId}/processed-at")
  public ResponseEntity<Void> unsetProcessedAt(@PathVariable String txId) {
    txService.unsetProcessedAt(txId);
    return ResponseEntity.noContent().build();
  }
}
