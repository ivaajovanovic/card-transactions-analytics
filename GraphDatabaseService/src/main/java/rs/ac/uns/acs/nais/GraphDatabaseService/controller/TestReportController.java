package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.report.ReportGenerator;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestReportController {
    
    private final ReportGenerator reportGenerator;
    
    /**
     * Test endpoint da vidimo da li report generator radi
     */
    @GetMapping("/report")
    public ResponseEntity<String> testReportGeneration(@RequestParam(required = false) String userId) {
        try {
            byte[] pdfBytes = reportGenerator.generateCompleteAnalyticsReport(userId);
            
            return ResponseEntity.ok("✅ PDF Report generated successfully! Size: " + pdfBytes.length + " bytes");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error generating report: " + e.getMessage());
        }
    }
}