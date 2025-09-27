package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.UserActivity;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.UserActivityService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-activities")
@RequiredArgsConstructor
public class UserActivityController {
    
    private final UserActivityService userActivityService;
    
    @PostMapping
    public ResponseEntity<UserActivity> createUserActivity(@RequestBody UserActivity userActivity) {
        UserActivity savedUserActivity = userActivityService.saveUserActivity(userActivity);
        return new ResponseEntity<>(savedUserActivity, HttpStatus.CREATED);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<UserActivity>> createUserActivities(@RequestBody List<UserActivity> userActivities) {
        List<UserActivity> savedUserActivities = userActivityService.saveAllUserActivities(userActivities);
        return new ResponseEntity<>(savedUserActivities, HttpStatus.CREATED);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserActivity> getUserActivity(@PathVariable String userId) {
        Optional<UserActivity> userActivity = userActivityService.findById(userId);
        return userActivity.map(ua -> ResponseEntity.ok(ua))
                          .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<UserActivity>> getAllUserActivities() {
        List<UserActivity> userActivities = userActivityService.findAll();
        return ResponseEntity.ok(userActivities);
    }
    
    @GetMapping("/transaction-count-range")
    public ResponseEntity<List<UserActivity>> getUsersByTransactionCountRange(
            @RequestParam Integer minCount,
            @RequestParam Integer maxCount) {
        List<UserActivity> userActivities = userActivityService.findByTransactionCountRange(minCount, maxCount);
        return ResponseEntity.ok(userActivities);
    }
    
    @GetMapping("/spending-range")
    public ResponseEntity<List<UserActivity>> getUsersBySpendingRange(
            @RequestParam Double minSpent,
            @RequestParam Double maxSpent) {
        List<UserActivity> userActivities = userActivityService.findByTotalSpentRange(minSpent, maxSpent);
        return ResponseEntity.ok(userActivities);
    }
    
    @GetMapping("/last-transaction-range")
    public ResponseEntity<List<UserActivity>> getUsersByLastTransactionRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<UserActivity> userActivities = userActivityService.findByLastTransactionDateRange(startDate, endDate);
        return ResponseEntity.ok(userActivities);
    }
    
    @GetMapping("/top-spenders")
    public ResponseEntity<List<UserActivity>> getTopSpenders() {
        List<UserActivity> topSpenders = userActivityService.getTopSpenders();
        return ResponseEntity.ok(topSpenders);
    }
    
    @GetMapping("/most-active")
    public ResponseEntity<List<UserActivity>> getMostActiveUsers() {
        List<UserActivity> mostActive = userActivityService.getMostActiveUsers();
        return ResponseEntity.ok(mostActive);
    }
    
    @GetMapping("/recent-activity")
    public ResponseEntity<List<UserActivity>> getUsersWithRecentActivity() {
        List<UserActivity> recentUsers = userActivityService.getUsersWithRecentActivity();
        return ResponseEntity.ok(recentUsers);
    }
    
    @GetMapping("/inactive")
    public ResponseEntity<List<UserActivity>> getInactiveUsers() {
        List<UserActivity> inactiveUsers = userActivityService.getInactiveUsers();
        return ResponseEntity.ok(inactiveUsers);
    }
    
    @GetMapping("/active/paginated")
    public ResponseEntity<Page<UserActivity>> getActiveUsersWithPagination(
            @RequestParam(defaultValue = "1") Integer minTransactionCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivity> activeUsers = userActivityService.findActiveUsersWithPagination(minTransactionCount, pageable);
        return ResponseEntity.ok(activeUsers);
    }
    
    @GetMapping("/spending-range/count")
    public ResponseEntity<Long> getUserCountBySpendingRange(
            @RequestParam Double minSpent,
            @RequestParam Double maxSpent) {
        long count = userActivityService.countUsersBySpendingRange(minSpent, maxSpent);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalUserActivityCount() {
        long count = userActivityService.count();
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<UserActivity> updateUserActivity(@PathVariable String userId, @RequestBody UserActivity userActivity) {
        if (!userActivityService.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        userActivity.setUserId(userId);
        UserActivity updatedUserActivity = userActivityService.saveUserActivity(userActivity);
        return ResponseEntity.ok(updatedUserActivity);
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserActivity(@PathVariable String userId) {
        if (!userActivityService.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        userActivityService.deleteUserActivity(userId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping
    public ResponseEntity<Void> deleteAllUserActivities() {
        userActivityService.deleteAllUserActivities();
        return ResponseEntity.noContent().build();
    }
}