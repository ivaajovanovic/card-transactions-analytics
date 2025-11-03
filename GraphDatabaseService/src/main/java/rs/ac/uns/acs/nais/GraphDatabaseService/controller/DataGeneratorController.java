package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.acs.nais.GraphDatabaseService.util.SampleDataGenerator;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataGeneratorController {
    
    private final SampleDataGenerator dataGenerator;
    
    @PostMapping("/generate")
    public String generateData() {
        try {
            dataGenerator.run();
            return "Sample data generation completed successfully!";
        } catch (Exception e) {
            log.error("Error generating sample data", e);
            return "Error: " + e.getMessage();
        }
    }
}
