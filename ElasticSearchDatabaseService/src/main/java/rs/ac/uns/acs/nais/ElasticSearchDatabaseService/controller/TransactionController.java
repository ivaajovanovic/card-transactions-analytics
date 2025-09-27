package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Transaction;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.TransactionService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        Transaction savedTransaction = transactionService.saveTransaction(transaction);
        return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<Transaction>> createTransactions(@RequestBody List<Transaction> transactions) {
        List<Transaction> savedTransactions = transactionService.saveAllTransactions(transactions);
        return new ResponseEntity<>(savedTransactions, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String id) {
        Optional<Transaction> transaction = transactionService.findById(id);
        return transaction.map(t -> ResponseEntity.ok(t))
                         .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.findAll();
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCardId(@PathVariable String cardId) {
        List<Transaction> transactions = transactionService.findByCardId(cardId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/card/{cardId}/paginated")
    public ResponseEntity<Page<Transaction>> getTransactionsByCardIdPaginated(
            @PathVariable String cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.findByCardIdWithPagination(cardId, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<Transaction>> getTransactionsByMerchantId(@PathVariable String merchantId) {
        List<Transaction> transactions = transactionService.findByMerchantId(merchantId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/amount-range")
    public ResponseEntity<List<Transaction>> getTransactionsByAmountRange(
            @RequestParam Double minAmount,
            @RequestParam Double maxAmount) {
        List<Transaction> transactions = transactionService.findByAmountRange(minAmount, maxAmount);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<Transaction> transactions = transactionService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Transaction>> searchTransactionsByDescription(
            @RequestParam String description) {
        List<Transaction> transactions = transactionService.searchByDescription(description);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/top-amounts")
    public ResponseEntity<List<Transaction>> getTopTransactionsByAmount() {
        List<Transaction> transactions = transactionService.getTopTransactionsByAmount();
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/card/{cardId}/count")
    public ResponseEntity<Long> getTransactionCountByCardId(@PathVariable String cardId) {
        long count = transactionService.countByCardId(cardId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalTransactionCount() {
        long count = transactionService.count();
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable String id, @RequestBody Transaction transaction) {
        if (!transactionService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        transaction.setId(id);
        Transaction updatedTransaction = transactionService.saveTransaction(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        if (!transactionService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping
    public ResponseEntity<Void> deleteAllTransactions() {
        transactionService.deleteAllTransactions();
        return ResponseEntity.noContent().build();
    }
}