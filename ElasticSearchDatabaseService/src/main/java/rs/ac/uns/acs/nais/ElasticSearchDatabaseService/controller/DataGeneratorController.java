package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.DataGeneratorService;

@RestController
@RequestMapping("/api/data-generator")
@RequiredArgsConstructor
public class DataGeneratorController {
    
    private final DataGeneratorService dataGeneratorService;
    
    @PostMapping("/transactions/{count}")
    public ResponseEntity<String> generateTransactions(@PathVariable int count) {
        if (count <= 0 || count > 100000) {
            return ResponseEntity.badRequest()
                    .body("Count must be between 1 and 100000");
        }
        
        dataGeneratorService.generateSampleTransactions(count);
        return ResponseEntity.ok("Generated " + count + " sample transactions successfully");
    }
    
    @PostMapping("/user-activities/{count}")
    public ResponseEntity<String> generateUserActivities(@PathVariable int count) {
        if (count <= 0 || count > 5000) {
            return ResponseEntity.badRequest()
                    .body("Count must be between 1 and 5000");
        }
        
        dataGeneratorService.generateSampleUserActivities(count);
        return ResponseEntity.ok("Generated " + count + " sample user activities successfully");
    }
    
    @PostMapping("/sample-data")
    public ResponseEntity<String> generateSampleData() {
        dataGeneratorService.generateSampleData();
        return ResponseEntity.ok("Generated sample data (15000 transactions, 3000 user activities) successfully");
    }
}