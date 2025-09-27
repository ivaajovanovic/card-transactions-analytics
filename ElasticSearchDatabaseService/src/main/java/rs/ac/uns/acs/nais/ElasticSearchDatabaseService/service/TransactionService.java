package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Transaction;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    public Transaction saveTransaction(Transaction transaction) {
        log.info("Saving transaction with ID: {}", transaction.getId());
        return transactionRepository.save(transaction);
    }
    
    public List<Transaction> saveAllTransactions(List<Transaction> transactions) {
        log.info("Saving {} transactions", transactions.size());
        return (List<Transaction>) transactionRepository.saveAll(transactions);
    }
    
    public Optional<Transaction> findById(String id) {
        return transactionRepository.findById(id);
    }
    
    public List<Transaction> findAll() {
        return (List<Transaction>) transactionRepository.findAll();
    }
    
    public List<Transaction> findByCardId(String cardId) {
        log.info("Finding transactions for card ID: {}", cardId);
        return transactionRepository.findByCardId(cardId);
    }
    
    public List<Transaction> findByMerchantId(String merchantId) {
        log.info("Finding transactions for merchant ID: {}", merchantId);
        return transactionRepository.findByMerchantId(merchantId);
    }
    
    public List<Transaction> findByAmountRange(Double minAmount, Double maxAmount) {
        log.info("Finding transactions with amount between {} and {}", minAmount, maxAmount);
        return transactionRepository.findByAmountBetween(minAmount, maxAmount);
    }
    
    public List<Transaction> findByDateRange(String startDate, String endDate) {
        log.info("Finding transactions between {} and {}", startDate, endDate);
        return transactionRepository.findByDateBetween(startDate, endDate);
    }
    
    public List<Transaction> searchByDescription(String searchText) {
        log.info("Searching transactions by description: {}", searchText);
        return transactionRepository.findByDescriptionContaining(searchText);
    }
    
    public Page<Transaction> findByCardIdWithPagination(String cardId, Pageable pageable) {
        log.info("Finding transactions for card ID: {} with pagination", cardId);
        return transactionRepository.findByCardId(cardId, pageable);
    }
    
    public long countByCardId(String cardId) {
        return transactionRepository.countByCardId(cardId);
    }
    
    public List<Transaction> getTopTransactionsByAmount() {
        log.info("Finding top 10 transactions by amount");
        return transactionRepository.findTop10ByOrderByAmountDesc();
    }
    
    public void deleteTransaction(String id) {
        log.info("Deleting transaction with ID: {}", id);
        transactionRepository.deleteById(id);
    }
    
    public void deleteAllTransactions() {
        log.info("Deleting all transactions");
        transactionRepository.deleteAll();
    }
    
    public boolean existsById(String id) {
        return transactionRepository.existsById(id);
    }
    
    public long count() {
        return transactionRepository.count();
    }
}