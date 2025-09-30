package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.report.ReportGenerator;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.AnalyticsService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final ReportGenerator reportGenerator;
    private final AnalyticsService analyticsService;
    
    /**
     * Generiše kompletan PDF izveštaj sa analitikama transakcija iz ElasticSearch baze
     * 
     * @return PDF izveštaj kao byte array
     */
    @GetMapping(value = "/analytics", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateAnalyticsReport() {
        
        try {
            log.info("Generating ElasticSearch analytics report...");
            
            byte[] pdfBytes = reportGenerator.generateCompleteAnalyticsReport();
            
            // Kreiranje filename-a sa timestamp-om
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "elasticsearch_transactions_analytics_report_" + timestamp + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            log.info("ElasticSearch analytics report generated successfully: {} bytes", pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error generating ElasticSearch analytics report: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }
    
    /**
     * Generiše PDF izveštaj i čuva ga u output/ folderu
     */
    @PostMapping("/analytics/save")
    public ResponseEntity<String> generateAndSaveReport() {
        
        try {
            log.info("Generating and saving ElasticSearch analytics report...");
            
            byte[] pdfBytes = reportGenerator.generateCompleteAnalyticsReport();
            
            // Kreiraj output folder ako ne postoji (Docker path)
            File outputDir = new File("/app/output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Kreiraj filename sa timestamp-om
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "elasticsearch_transactions_analytics_" + timestamp + ".pdf";
            String filePath = "/app/output/" + filename;
            
            // Sačuvaj fajl
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }
            
            log.info("ElasticSearch analytics report saved successfully: {}", filePath);
            
            return ResponseEntity.ok("✅ PDF Report generated and saved successfully!\n" +
                    "📁 File: " + filePath + "\n" +
                    "📊 Size: " + pdfBytes.length + " bytes\n" +
                    "💾 Location: output/ folder (ignored by Git)\n" +
                    "🔍 Report includes:\n" +
                    "   • Simple sections: Transactions list, User activities\n" +
                    "   • Complex sections: Top users, Top merchants, Category analysis\n" +
                    "   • High-value transactions, Daily activity, System statistics");
            
        } catch (IOException e) {
            log.error("Error saving ElasticSearch analytics report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error saving report: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error generating ElasticSearch analytics report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error generating report: " + e.getMessage());
        }
    }
    
    /**
     * Grafana-friendly JSON endpoint za analytics podatke
     */
    @GetMapping("/analytics/json")
    public ResponseEntity<?> getAnalyticsJson() {
        try {
            log.info("Getting ElasticSearch analytics data in JSON format...");
            
            // Umesto reportGenerator-a, koristimo direktno AnalyticsService
            var analytics = new java.util.HashMap<String, Object>();
            
            // Dodaj različite metruke za Grafana dashboard
            analytics.put("recentTransactions", analyticsService.getRecentTransactions());
            analytics.put("weeklySpendingTotals", analyticsService.getWeeklySpendingTotals());
            analytics.put("topKeywordsBySpending", analyticsService.getTopKeywordsBySpending());
            
            log.info("ElasticSearch analytics JSON data retrieved successfully");
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("Error getting ElasticSearch analytics JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint za report servis
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "ElasticSearch Report Generator",
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                "features", Map.of(
                    "pdfGeneration", true,
                    "analyticsService", true,
                    "reportSaving", true,
                    "jsonExport", true
                )
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }
}
