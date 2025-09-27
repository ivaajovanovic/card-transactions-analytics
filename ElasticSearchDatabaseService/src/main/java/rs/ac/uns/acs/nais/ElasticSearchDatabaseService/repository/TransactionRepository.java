package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Transaction;

import java.util.List;

@Repository
public interface TransactionRepository extends ElasticsearchRepository<Transaction, String> {
    
    // Find transactions by card ID
    List<Transaction> findByCardId(String cardId);
    
    // Find transactions by merchant ID
    List<Transaction> findByMerchantId(String merchantId);
    
    // Find transactions by amount range
    List<Transaction> findByAmountBetween(Double minAmount, Double maxAmount);
    
    // Find transactions by date range
    List<Transaction> findByDateBetween(String startDate, String endDate);
    
    // Full-text search in description
    @Query("{\"match\": {\"description\": \"?0\"}}")
    List<Transaction> findByDescriptionContaining(String description);
    
    // Advanced search combining multiple criteria
    @Query("{\"bool\": {\"must\": [{\"range\": {\"amount\": {\"gte\": ?0, \"lte\": ?1}}}, {\"range\": {\"date\": {\"gte\": \"?2\", \"lte\": \"?3\"}}}]}}")
    List<Transaction> findByAmountRangeAndDateRange(Double minAmount, Double maxAmount, 
                                                   String startDate, String endDate);
    
    // Search transactions with pagination
    Page<Transaction> findByCardId(String cardId, Pageable pageable);
    
    // Count transactions by card ID
    long countByCardId(String cardId);
    
    // Find top transactions by amount (descending)
    List<Transaction> findTop10ByOrderByAmountDesc();
}