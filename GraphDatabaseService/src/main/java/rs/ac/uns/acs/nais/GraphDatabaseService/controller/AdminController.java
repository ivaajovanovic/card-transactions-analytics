package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.util.SampleDataGenerator;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.AdminRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.AnalyticsService;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.AdminNode;
import org.springframework.http.ResponseEntity;

import org.springframework.data.neo4j.core.Neo4jClient;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final SampleDataGenerator dataGenerator;
    private final AdminRepository adminRepository;
    private final AnalyticsService analyticsService;

    private final Neo4jClient neo4jClient;
    
    @PostMapping("/populate-data")
    public String populateData() {
        try {
            log.info("Manual data population requested via REST endpoint");
            dataGenerator.run();
            return "Data population completed successfully!";
        } catch (Exception e) {
            log.error("Error populating data", e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/login")
    public ResponseEntity<AdminNode> login(@RequestParam String email, @RequestParam String password) {
        return adminRepository.findByEmailAndPassword(email, password)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return analyticsService.getAllUsersWithStats();
    }

    @PostMapping("/users/update-missing-data")
    public Map<String, Object> updateMissingUserData() {
        return analyticsService.updateMissingUserData();
    }

    @GetMapping("/anomalies/suspicious-transactions")
    public List<Map<String, Object>> getSuspiciousTransactions(@RequestParam(defaultValue = "2.5") Double minMultiplier) {
        return analyticsService.getSuspiciousTransactions(minMultiplier);
    }


    @GetMapping("/predictions/purchase-probability/{userId}")
    public List<Map<String, Object>> getPurchaseProbability(@PathVariable String userId, @RequestParam(defaultValue = "10") Integer limit) {
    return analyticsService.predictPurchaseProbability(userId, limit);
}

@GetMapping("/predictions/merchant-purchase-probability/{merchantId}")
public List<Map<String, Object>> getMerchantPurchaseProbability(
    @PathVariable String merchantId,
    @RequestParam(defaultValue = "20") Integer limit) {
    return analyticsService.predictMerchantPurchaseProbability(merchantId, limit);
}

@GetMapping("/analytics/regional-performance")
public List<Map<String, Object>> getRegionalPerformance() {
    return analyticsService.getRegionalPerformance();
}

@GetMapping("/analytics/shopping-affinity")
public List<Map<String, Object>> getShoppingAffinity() {
    return analyticsService.getShoppingAffinity();
}

@GetMapping("/analytics/cross-region-patterns")
public List<Map<String, Object>> getCrossRegionPatterns(@RequestParam(defaultValue = "10") Integer limit) {
    return analyticsService.getCrossRegionPatterns(limit);
}

@PostMapping("/wipe-data")
public Map<String, String> wipeAllData() {
    neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    return Map.of("message", "All data wiped successfully");
}
}
