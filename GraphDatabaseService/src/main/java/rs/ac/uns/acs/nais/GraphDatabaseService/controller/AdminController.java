package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.util.SampleDataGenerator;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.AdminRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.AdminNode;
import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final SampleDataGenerator dataGenerator;
    private final AdminRepository adminRepository;
    
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
}
