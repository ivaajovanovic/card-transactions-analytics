package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.UserNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.UserRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.TransactionRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.SpendByGroupDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.RecurringExpenseDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.CollaborativeRecommendationDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.OptimalPurchaseTimeDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.CardBenefitDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.util.SampleDataGenerator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository repo;
    private final TransactionRepository transactionRepo;
    private final SampleDataGenerator sampleDataGenerator;
    private final Neo4jClient neo4jClient;

    @GetMapping
    public List<UserNode> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public UserNode get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }
    
    @GetMapping("/external/{externalId}")
    public ResponseEntity<UserNode> getByExternalId(@PathVariable String externalId) {
        return repo.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
   
       @GetMapping("/login")
       public ResponseEntity<UserNode> login(@RequestParam String email, @RequestParam String password) {
           return repo.findByEmailAndPassword(email, password)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.status(401).build());
       }
    
    @GetMapping("/external/{externalId}/with-connections")
    public ResponseEntity<UserNode> getWithConnections(@PathVariable String externalId) {
        return repo.findByExternalIdWithConnections(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/external/{externalId}/with-preferences")
    public ResponseEntity<UserNode> getWithPreferences(@PathVariable String externalId) {
        return repo.findByExternalIdWithPreferences(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Best card benefits for a given category (by user email)
    @GetMapping("/by-email/{email}/best-card-benefits/{categoryCode}")
    public ResponseEntity<List<CardBenefitDTO>> getBestCardBenefits(
            @PathVariable String email,
            @PathVariable String categoryCode) {
        List<CardBenefitDTO> res = transactionRepo.bestCardBenefitsForCategory(email, categoryCode);
        return ResponseEntity.ok(res);
    }
    
    @GetMapping("/external/{externalId}/connected-users")
    public ResponseEntity<List<Object>> getConnectedUsers(@PathVariable String externalId) {
        return ResponseEntity.ok(repo.findConnectedUsers(externalId));
    }
    
    @GetMapping("/segment/{segment}")
    public ResponseEntity<List<UserNode>> getBySegment(@PathVariable String segment) {
        return ResponseEntity.ok(repo.findBySegment(segment));
    }
    
    @GetMapping("/income-level/{incomeLevel}")
    public ResponseEntity<List<UserNode>> getByIncomeLevel(@PathVariable String incomeLevel) {
        return ResponseEntity.ok(repo.findByIncomeLevel(incomeLevel));
    }
    
    @GetMapping("/lifestage/{lifestage}")
    public ResponseEntity<List<UserNode>> getByLifestage(@PathVariable String lifestage) {
        return ResponseEntity.ok(repo.findByLifestage(lifestage));
    }
    
    // Location-based endpoints
    @GetMapping("/location/city/{city}")
    public ResponseEntity<List<UserNode>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(repo.findByHomeCity(city));
    }
    
    @GetMapping("/location/country/{country}")
    public ResponseEntity<List<UserNode>> getByCountry(@PathVariable String country) {
        return ResponseEntity.ok(repo.findByHomeCountry(country));
    }
    
    @GetMapping("/location")
    public ResponseEntity<List<UserNode>> getByLocation(
            @RequestParam String city,
            @RequestParam String country) {
        return ResponseEntity.ok(repo.findByLocation(city, country));
    }
    
    @GetMapping("/external/{externalId}/travel-pattern")
    public ResponseEntity<Object> getTravelPattern(@PathVariable String externalId) {
        return ResponseEntity.ok(repo.getUserTravelPattern(externalId));
    }
    
    @GetMapping("/frequent-travelers")
    public ResponseEntity<List<UserNode>> getFrequentTravelers(
            @RequestParam(defaultValue = "3") Integer minCities) {
        return ResponseEntity.ok(repo.findFrequentTravelers(minCities));
    }
    
    // User Analytics Endpoints
    
    @GetMapping("/{userId}/analytics/spending-by-category")
    public ResponseEntity<List<Map<String, Object>>> getUserSpendingByCategory(@PathVariable Long userId) {
        return ResponseEntity.ok(repo.getUserSpendingByCategory(userId));
    }
    
    @GetMapping("/by-email/{email}/analytics/spending-by-category")
    public ResponseEntity<List<Map<String, Object>>> getUserSpendingByCategoryByEmail(@PathVariable String email) {
        String query = """
            MATCH (u:User {email: $email})<-[:OWNS]-(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)-[:IN_CATEGORY]->(cat:Category)
            RETURN cat.code AS categoryCode, 
                   cat.name AS categoryName, 
                   COUNT(t) AS transactionCount, 
                   SUM(t.amount) AS totalAmount,
                   AVG(t.amount) AS avgAmount
            ORDER BY totalAmount DESC
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/{userId}/analytics/top-merchants")
    public ResponseEntity<List<Map<String, Object>>> getUserTopMerchants(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(repo.getUserTopMerchants(userId, limit));
    }
    
    @GetMapping("/by-email/{email}/analytics/top-merchants")
    public ResponseEntity<List<Map<String, Object>>> getUserTopMerchantsByEmail(
            @PathVariable String email,
            @RequestParam(defaultValue = "10") Integer limit) {
        String query = """
            MATCH (u:User {email: $email})<-[:OWNS]-(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
            OPTIONAL MATCH (m)-[:IN_CATEGORY]->(cat:Category)
            RETURN m.merchantId AS merchantId,
                   m.name AS merchantName,
                   cat.name AS categoryName,
                   COUNT(t) AS transactionCount,
                   SUM(t.amount) AS totalSpent,
                   AVG(t.amount) AS avgAmount
            ORDER BY transactionCount DESC
            LIMIT $limit
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .bind(limit).to("limit")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/{userId}/analytics/card-usage")
    public ResponseEntity<List<Map<String, Object>>> getUserCardUsage(@PathVariable Long userId) {
        return ResponseEntity.ok(repo.getUserCardUsage(userId));
    }
    
    @GetMapping("/by-email/{email}/analytics/card-usage")
    public ResponseEntity<List<Map<String, Object>>> getUserCardUsageByEmail(@PathVariable String email) {
        String query = """
            MATCH (u:User {email: $email})<-[:OWNS]-(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
            RETURN c.panHash AS cardId,
                   c.network AS network,
                   c.type AS type,
                   COUNT(t) AS transactionCount,
                   SUM(t.amount) AS totalSpent,
                   SUM(CASE WHEN t.contactless = true THEN 1 ELSE 0 END) AS contactlessCount
            ORDER BY transactionCount DESC
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/{userId}/analytics/monthly-spending")
    public ResponseEntity<List<Map<String, Object>>> getUserMonthlySpending(@PathVariable Long userId) {
        return ResponseEntity.ok(repo.getUserMonthlySpending(userId));
    }
    
    @GetMapping("/by-email/{email}/analytics/monthly-spending")
    public ResponseEntity<List<Map<String, Object>>> getUserMonthlySpendingByEmail(@PathVariable String email) {
        String query = """
            MATCH (u:User {email: $email})<-[:OWNS]-(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
            WITH u, t, m,
                 toString(datetime(t.timestamp).year) + '-' + 
                 right('0' + toString(datetime(t.timestamp).month), 2) AS yearMonth
            RETURN yearMonth,
                   COUNT(t) AS transactionCount,
                   SUM(t.amount) AS totalAmount,
                   AVG(t.amount) AS avgAmount,
                   COUNT(DISTINCT m) AS uniqueMerchants
            ORDER BY yearMonth DESC
            LIMIT 12
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/by-email/{email}/analytics/recurring-expenses")
    public ResponseEntity<List<RecurringExpenseDTO>> getRecurringExpensesByEmail(@PathVariable String email) {
        // First, get user's externalId from email
        String findUserQuery = "MATCH (u:User {email: $email}) RETURN u.externalId AS externalId";
        var userResult = neo4jClient.query(findUserQuery)
            .bind(email).to("email")
            .fetch()
            .one();
        
        if (userResult.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String externalId = (String) userResult.get().get("externalId");
        List<RecurringExpenseDTO> recurringExpenses = transactionRepo.recurringMonthlyExpenses(externalId);
        
        return ResponseEntity.ok(recurringExpenses);
    }
    
    @GetMapping("/by-email/{email}/recommendations/collaborative")
    public ResponseEntity<List<CollaborativeRecommendationDTO>> getCollaborativeRecommendationsByEmail(@PathVariable String email) {
        // Get user's externalId from email
        String findUserQuery = "MATCH (u:User {email: $email}) RETURN u.externalId AS externalId";
        var userResult = neo4jClient.query(findUserQuery)
            .bind(email).to("email")
            .fetch()
            .one();
        
        if (userResult.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String externalId = (String) userResult.get().get("externalId");
        List<CollaborativeRecommendationDTO> recommendations = transactionRepo.recommendedMerchantsCF(externalId);
        
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/by-email/{email}/analytics/optimal-purchase-time")
    public ResponseEntity<List<OptimalPurchaseTimeDTO>> getOptimalPurchaseTimeByEmail(@PathVariable String email) {
        // Get user's externalId from email
        String findUserQuery = "MATCH (u:User {email: $email}) RETURN u.externalId AS externalId";
        var userResult = neo4jClient.query(findUserQuery)
            .bind(email).to("email")
            .fetch()
            .one();
        
        if (userResult.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String externalId = (String) userResult.get().get("externalId");
        List<OptimalPurchaseTimeDTO> optimalTimes = transactionRepo.optimalPurchaseTime(externalId);
        
        return ResponseEntity.ok(optimalTimes);
    }
    
    // Seed transactions for a user
    @PostMapping("/seed-transactions")
    public ResponseEntity<Map<String, String>> seedTransactions(
            @RequestParam String email,
            @RequestParam(defaultValue = "2") Integer cardCount,
            @RequestParam(defaultValue = "30") Integer transactionsPerCard) {
        try {
            sampleDataGenerator.addTransactionsForUser(email, cardCount, transactionsPerCard);
            return ResponseEntity.ok(Map.of(
                "message", "Successfully added " + cardCount + " cards with " + transactionsPerCard + " transactions each for user: " + email,
                "email", email,
                "cards", cardCount.toString(),
                "transactionsPerCard", transactionsPerCard.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public UserNode create(@RequestBody UserNode user) { return repo.save(user); }

    @PutMapping("/{id}")
    public UserNode update(@PathVariable Long id, @RequestBody UserNode user) { user.setId(id); return repo.save(user); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
