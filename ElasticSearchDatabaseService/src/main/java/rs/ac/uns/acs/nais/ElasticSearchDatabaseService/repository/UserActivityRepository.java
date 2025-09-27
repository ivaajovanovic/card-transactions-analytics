package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.UserActivity;

import java.util.List;

@Repository
public interface UserActivityRepository extends ElasticsearchRepository<UserActivity, String> {
    
    // Find users by transaction count range
    List<UserActivity> findByTxCountBetween(Integer minCount, Integer maxCount);
    
    // Find users by total spent range
    List<UserActivity> findByTotalSpentBetween(Double minSpent, Double maxSpent);
    
    // Find users by last transaction date range
    List<UserActivity> findByLastTxDateBetween(String startDate, String endDate);
    
    // Find top spenders
    List<UserActivity> findTop10ByOrderByTotalSpentDesc();
    
    // Find most active users by transaction count
    List<UserActivity> findTop10ByOrderByTxCountDesc();
    
    // Find users with recent activity
    @Query("{\"range\": {\"lastTxDate\": {\"gte\": \"now-7d\"}}}")
    List<UserActivity> findUsersWithRecentActivity();
    
    // Find inactive users (no transactions in last 30 days)
    @Query("{\"range\": {\"lastTxDate\": {\"lte\": \"now-30d\"}}}")
    List<UserActivity> findInactiveUsers();
    
    // Search with pagination
    Page<UserActivity> findByTxCountGreaterThan(Integer count, Pageable pageable);
    
    // Count users by spending range
    long countByTotalSpentBetween(Double minSpent, Double maxSpent);
}