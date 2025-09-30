package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.UserActivity;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository.UserActivityRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityService {
    
    private final UserActivityRepository userActivityRepository;
    
    public UserActivity saveUserActivity(UserActivity userActivity) {
        log.info("Saving user activity for user ID: {}", userActivity.getUserId());
        return userActivityRepository.save(userActivity);
    }
    
    public List<UserActivity> saveAllUserActivities(List<UserActivity> userActivities) {
        log.info("Saving {} user activities", userActivities.size());
        return (List<UserActivity>) userActivityRepository.saveAll(userActivities);
    }
    
    public Optional<UserActivity> findById(String userId) {
        return userActivityRepository.findById(userId);
    }
    
    public List<UserActivity> findAll() {
        return (List<UserActivity>) userActivityRepository.findAll();
    }
    
    public List<UserActivity> findByTransactionCountRange(Integer minCount, Integer maxCount) {
        log.info("Finding users with transaction count between {} and {}", minCount, maxCount);
        return userActivityRepository.findByTxCountBetween(minCount, maxCount);
    }
    
    public List<UserActivity> findByTotalSpentRange(Double minSpent, Double maxSpent) {
        log.info("Finding users with total spent between {} and {}", minSpent, maxSpent);
        return userActivityRepository.findByTotalSpentBetween(minSpent, maxSpent);
    }
    
    public List<UserActivity> findByLastTransactionDateRange(String startDate, String endDate) {
        log.info("Finding users with last transaction between {} and {}", startDate, endDate);
        return userActivityRepository.findByLastTxDateBetween(startDate, endDate);
    }
    
    public List<UserActivity> getTopSpenders() {
        log.info("Finding top 10 spenders");
        return userActivityRepository.findTop10ByOrderByTotalSpentDesc();
    }
    
    public List<UserActivity> getMostActiveUsers() {
        log.info("Finding top 10 most active users");
        return userActivityRepository.findTop10ByOrderByTxCountDesc();
    }
    
    public List<UserActivity> getUsersWithRecentActivity() {
        log.info("Finding users with recent activity (last 7 days)");
        return userActivityRepository.findUsersWithRecentActivity();
    }
    
    public List<UserActivity> getInactiveUsers() {
        log.info("Finding inactive users (no transactions in last 30 days)");
        return userActivityRepository.findInactiveUsers();
    }
    
    public Page<UserActivity> findActiveUsersWithPagination(Integer minTransactionCount, Pageable pageable) {
        log.info("Finding active users with more than {} transactions", minTransactionCount);
        return userActivityRepository.findByTxCountGreaterThan(minTransactionCount, pageable);
    }
    
    public long countUsersBySpendingRange(Double minSpent, Double maxSpent) {
        return userActivityRepository.countByTotalSpentBetween(minSpent, maxSpent);
    }
    
    public void deleteUserActivity(String userId) {
        log.info("Deleting user activity for user ID: {}", userId);
        userActivityRepository.deleteById(userId);
    }
    
    public void deleteAllUserActivities() {
        log.info("Deleting all user activities");
        userActivityRepository.deleteAll();
    }
    
    public boolean existsById(String userId) {
        return userActivityRepository.existsById(userId);
    }
    
    public long count() {
        return userActivityRepository.count();
    }
    
    /**
     * SAGA Method: Ažurira UserActivity sa informacijama o sumnjivim transakcijama
     * Deo distributed transaction-a sa Graph DB servisom
     */
    public boolean updateSuspiciousActivity(String userId, Long suspiciousCount) {
        try {
            log.info("Updating suspicious activity for user {} with {} suspicious transactions", userId, suspiciousCount);
            
            Optional<UserActivity> existingActivity = findById(userId);
            UserActivity activity;
            
            if (existingActivity.isPresent()) {
                activity = existingActivity.get();
            } else {
                // Kreiraj novi ako ne postoji
                activity = new UserActivity();
                activity.setUserId(userId);
                activity.setTxCount(0);
                activity.setTotalSpent(0.0);
            }
            
            // Ažuriraj suspicious activity fields
            activity.setSuspiciousTransactionCount(suspiciousCount.intValue());
            activity.setHasSuspiciousActivity(suspiciousCount > 0);
            activity.setLastSuspiciousActivityDate(java.time.Instant.now().toString());
            
            userActivityRepository.save(activity);
            log.info("Successfully updated suspicious activity for user {}", userId);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to update suspicious activity for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * SAGA Rollback Method: Uklanja suspicious activity informacije
     */
    public boolean rollbackSuspiciousActivity(String userId) {
        try {
            log.info("Rolling back suspicious activity for user {}", userId);
            
            Optional<UserActivity> existingActivity = findById(userId);
            if (existingActivity.isPresent()) {
                UserActivity activity = existingActivity.get();
                activity.setSuspiciousTransactionCount(0);
                activity.setHasSuspiciousActivity(false);
                activity.setLastSuspiciousActivityDate(null);
                
                userActivityRepository.save(activity);
                log.info("Successfully rolled back suspicious activity for user {}", userId);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to rollback suspicious activity for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
}