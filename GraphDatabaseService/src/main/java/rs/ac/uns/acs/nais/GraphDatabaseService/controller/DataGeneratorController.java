package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.impl.DataGeneratorService;

@RestController
@RequestMapping("/api/neo4j-generator")
@RequiredArgsConstructor
public class DataGeneratorController {

    private final DataGeneratorService generator;

    @PostMapping("/reset")
    public ResponseEntity<String> reset() {
        long c = generator.resetDatabase();
        return ResponseEntity.ok("Database cleared.");
    }

    @PostMapping("/reference")
    public ResponseEntity<String> reference(@RequestParam(defaultValue = "30") int merchants) {
        generator.generateReferenceData(merchants);
        return ResponseEntity.ok("Reference data generated (merchants="+merchants+").");
    }

    @PostMapping("/users/{count}")
    public ResponseEntity<String> users(@PathVariable int count,
                                        @RequestParam(defaultValue = "3") int maxCardsPerUser) {
        if (count <= 0) return ResponseEntity.badRequest().body("count must be > 0");
        generator.generateUsers(count, maxCardsPerUser);
        return ResponseEntity.ok("Users generated: "+count+" (maxCards="+maxCardsPerUser+")");
    }

    @PostMapping("/transactions/{count}")
    public ResponseEntity<String> tx(@PathVariable int count,
                                     @RequestParam(defaultValue = "90") int daysBack,
                                     @RequestParam(defaultValue = "0.15") double suspiciousPosRate,
                                     @RequestParam(defaultValue = "0.20") double crossChannelRate) {
        if (count <= 0) return ResponseEntity.badRequest().body("count must be > 0");
        generator.generateTransactions(count, daysBack, suspiciousPosRate, crossChannelRate);
        return ResponseEntity.ok("Transactions generated: "+count+" (+ cross-channel extras).");
    }

    @PostMapping("/sample")
    public ResponseEntity<String> sample(@RequestParam(defaultValue = "40") int merchants,
                                         @RequestParam(defaultValue = "200") int users,
                                         @RequestParam(defaultValue = "3") int maxCardsPerUser,
                                         @RequestParam(defaultValue = "5000") int tx,
                                         @RequestParam(defaultValue = "90") int daysBack) {
        generator.generateSampleData(merchants, users, maxCardsPerUser, tx, daysBack);
        return ResponseEntity.ok("Sample data generated.");
    }
}
