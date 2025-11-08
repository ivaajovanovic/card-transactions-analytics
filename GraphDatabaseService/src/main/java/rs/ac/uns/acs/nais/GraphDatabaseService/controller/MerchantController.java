package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.MerchantNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.MerchantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {
    private final MerchantRepository repo;
    private final Neo4jClient neo4jClient;

    @GetMapping
    public List<MerchantNode> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public MerchantNode get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }
    
    @GetMapping("/merchant-id/{merchantId}")
    public ResponseEntity<MerchantNode> getByMerchantId(@PathVariable String merchantId) {
        return repo.findByMerchantId(merchantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/merchant-id/{merchantId}/with-sequences")
    public ResponseEntity<MerchantNode> getWithSequences(@PathVariable String merchantId) {
        return repo.findByMerchantIdWithSequences(merchantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/merchant-id/{merchantId}/sequences")
    public ResponseEntity<List<Object>> getMerchantSequences(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(repo.findMerchantSequences(merchantId, limit));
    }
    
    @GetMapping("/price-range/{priceRange}")
    public ResponseEntity<List<MerchantNode>> getByPriceRange(@PathVariable String priceRange) {
        return ResponseEntity.ok(repo.findByPriceRange(priceRange));
    }
    
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<MerchantNode>> getByTag(@PathVariable String tag) {
        return ResponseEntity.ok(repo.findByTag(tag));
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<MerchantNode>> getPopular(
            @RequestParam(defaultValue = "0.5") Double minScore,
            @RequestParam(defaultValue = "20") Integer limit) {
        return ResponseEntity.ok(repo.findPopularMerchants(minScore, limit));
    }
    
    @GetMapping("/ticket-size-range")
    public ResponseEntity<List<MerchantNode>> getByTicketSizeRange(
            @RequestParam Double minTicket,
            @RequestParam Double maxTicket) {
        return ResponseEntity.ok(repo.findByAvgTicketSizeRange(minTicket, maxTicket));
    }
    
    @GetMapping("/recommend-by-sequences/{userExternalId}")
    public ResponseEntity<List<Object>> recommendBySequences(
            @PathVariable String userExternalId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(repo.recommendMerchantsBasedOnSequences(userExternalId, limit));
    }
    
    // Location-based endpoints
    @GetMapping("/location/city/{city}")
    public ResponseEntity<List<MerchantNode>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(repo.findByCity(city));
    }
    
    @GetMapping("/location/country/{country}")
    public ResponseEntity<List<MerchantNode>> getByCountry(@PathVariable String country) {
        return ResponseEntity.ok(repo.findByCountry(country));
    }
    
    @GetMapping("/location")
    public ResponseEntity<List<MerchantNode>> getByLocation(
            @RequestParam String city,
            @RequestParam String country) {
        return ResponseEntity.ok(repo.findByLocation(city, country));
    }
    
    @GetMapping("/popular-in-user-city/{userExternalId}")
    public ResponseEntity<List<MerchantNode>> getPopularInUserCity(
            @PathVariable String userExternalId,
            @RequestParam(defaultValue = "0.5") Double minScore,
            @RequestParam(defaultValue = "20") Integer limit) {
        return ResponseEntity.ok(repo.findPopularInUserCity(userExternalId, minScore, limit));
    }
    
    @GetMapping("/cross-region")
    public ResponseEntity<List<Object>> getCrossRegionMerchants(
            @RequestParam(defaultValue = "2") Integer minCountries,
            @RequestParam(defaultValue = "5") Integer minCities) {
        return ResponseEntity.ok(repo.findCrossRegionMerchants(minCountries, minCities));
    }
    
    @GetMapping("/nearby/{userExternalId}")
    public ResponseEntity<List<MerchantNode>> getNearbyMerchants(@PathVariable String userExternalId) {
        return ResponseEntity.ok(repo.findNearbyMerchants(userExternalId));
    }

    @GetMapping("/login")
    public ResponseEntity<MerchantNode> login(@RequestParam String email, @RequestParam String password) {
        return repo.findByEmailAndPassword(email, password)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping
    public MerchantNode create(@RequestBody MerchantNode merchant) { return repo.save(merchant); }

    @PutMapping("/{id}")
    public MerchantNode update(@PathVariable Long id, @RequestBody MerchantNode merchant) { merchant.setId(id); return repo.save(merchant); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }

    // Merchant Analytics Endpoints (email-based)
    
    @GetMapping("/by-email/{email}/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactions(
            @PathVariable String email) {
        String query = """
            MATCH (m:Merchant {email: $email})<-[t:TRANSACTED_WITH]-(c:Card)
            OPTIONAL MATCH (c)<-[:OWNS]-(u:User)
            RETURN t.transactionId AS transactionId,
                   t.amount AS amount,
                   t.timestamp AS timestamp,
                   t.paymentType AS paymentType,
                   t.channel AS channel,
                   t.contactless AS contactless,
                   t.status AS status,
                   c.cardNumber AS cardNumber,
                   c.cardBrand AS cardNetwork,
                   u.fullName AS userName,
                   u.email AS userEmail
            ORDER BY t.timestamp DESC
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/by-email/{email}/analytics/overview")
    public ResponseEntity<Map<String, Object>> getMerchantOverview(@PathVariable String email) {
        String query = """
            MATCH ()-[t:TRANSACTED_WITH]->()
            RETURN 
                COUNT(t) AS totalTransactions,
                SUM(t.amount) AS totalRevenue,
                AVG(t.amount) AS avgTransactionAmount,
                MIN(t.amount) AS minAmount,
                MAX(t.amount) AS maxAmount
        """;
        var results = neo4jClient.query(query)
            .fetch()
            .one();
        return ResponseEntity.ok(results.orElse(Map.of()));
    }
    
    @GetMapping("/by-email/{email}/analytics/card-networks")
    public ResponseEntity<List<Map<String, Object>>> getTransactionsByCardNetwork(@PathVariable String email) {
        String query = """
            MATCH (m:Merchant {email: $email})<-[t:TRANSACTED_WITH]-(c:Card)
            RETURN c.cardBrand AS cardBrand,
                   COUNT(t) AS count,
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
    
    @GetMapping("/by-email/{email}/analytics/payment-types")
    public ResponseEntity<List<Map<String, Object>>> getTransactionsByPaymentType(@PathVariable String email) {
        String query = """
            MATCH (m:Merchant {email: $email})<-[t:TRANSACTED_WITH]-(c:Card)
            RETURN t.paymentType AS paymentType,
                   COUNT(t) AS count,
                   SUM(t.amount) AS totalAmount,
                   SUM(CASE WHEN t.contactless = true THEN 1 ELSE 0 END) AS contactlessCount
            ORDER BY count DESC
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/by-email/{email}/analytics/monthly-revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue(@PathVariable String email) {
        String query = """
            MATCH (m:Merchant {email: $email})<-[t:TRANSACTED_WITH]-(c:Card)
            WITH t,
                 toString(datetime(t.timestamp).year) + '-' + 
                 right('0' + toString(datetime(t.timestamp).month), 2) AS month
            RETURN month,
                   COUNT(t) AS transactionCount,
                   SUM(t.amount) AS totalRevenue,
                   AVG(t.amount) AS avgAmount
            ORDER BY month DESC
            LIMIT 12
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/by-email/{email}/analytics/top-customers")
    public ResponseEntity<List<Map<String, Object>>> getTopCustomers(
            @PathVariable String email,
            @RequestParam(defaultValue = "10") Integer limit) {
        String query = """
            MATCH (m:Merchant {email: $email})<-[t:TRANSACTED_WITH]-(c:Card)<-[:OWNS]-(u:User)
            RETURN u.fullName AS userName,
                   u.email AS userEmail,
                   COUNT(t) AS transactionCount,
                   SUM(t.amount) AS totalSpent,
                   AVG(t.amount) AS avgAmount
            ORDER BY totalSpent DESC
            LIMIT $limit
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .bind(limit).to("limit")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/by-email/{email}/analytics/transaction-channels")
    public ResponseEntity<List<Map<String, Object>>> getTransactionsByChannel(@PathVariable String email) {
        String query = """
            MATCH (m:Merchant {email: $email})<-[t:TRANSACTED_WITH]-(c:Card)
            RETURN t.channel AS channel,
                   COUNT(t) AS count,
                   SUM(t.amount) AS totalAmount
            ORDER BY count DESC
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }
    
    @GetMapping("/by-email/{email}/analytics/hourly-distribution")
    public ResponseEntity<List<Map<String, Object>>> getHourlyDistribution(@PathVariable String email) {
        String query = """
            MATCH (m:Merchant {email: $email})<-[t:TRANSACTED_WITH]-(c:Card)
            WITH datetime(t.timestamp).hour AS hour,
                 COUNT(t) AS count,
                 SUM(t.amount) AS totalAmount
            RETURN hour,
                   count,
                   totalAmount,
                   AVG(totalAmount) AS avgAmount
            ORDER BY hour
        """;
        var results = neo4jClient.query(query)
            .bind(email).to("email")
            .fetch()
            .all();
        return ResponseEntity.ok(results.stream().collect(Collectors.toList()));
    }

    @GetMapping("/all")
    public List<Map<String, Object>> getAllMerchantsWithAvgTicketSize() {
    String query = """
        MATCH (m:Merchant)
        OPTIONAL MATCH (m)<-[t:TRANSACTED_WITH]-()
        WITH m, AVG(t.amount) AS avgTicketSize
        RETURN 
            m.merchantId AS merchantId,
            m.name AS name,
            m.email AS email,
            m.id AS id,
            m.priceRange AS priceRange,
            m.popularityScore AS popularityScore,
            avgTicketSize
        ORDER BY m.name
    """;
return new ArrayList<>(neo4jClient.query(query).fetch().all());}
}
