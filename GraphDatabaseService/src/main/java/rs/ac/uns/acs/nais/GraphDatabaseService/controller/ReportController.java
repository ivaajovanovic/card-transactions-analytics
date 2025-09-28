package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.report.ReportGenerator;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.AnalyticsService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportGenerator reportGenerator;
    private final AnalyticsService analyticsService;
    
    /**
     * Generiše kompletan PDF izveštaj sa analitikama transakcija kartica
     * Podržava opcioni userId parametar za personalizovane analize
     * 
     * @param userId Opcioni ID korisnika za personalizovane analize (može biti null)
     * @return PDF izveštaj kao byte array
     */
    @GetMapping(value = "/analytics", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateAnalyticsReport(
            @RequestParam(required = false) String userId) {
        
        try {
            System.out.println("Generating analytics report for userId: " + (userId != null ? userId : "ALL"));
            
            byte[] pdfBytes = reportGenerator.generateCompleteAnalyticsReport(userId);
            
            // Kreiranje filename-a sa timestamp-om
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "card_transactions_analytics_report_" + timestamp + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }
    
    /**
     * Generiše personalizovani izveštaj za određenog korisnika
     */
    @GetMapping(value = "/analytics/user/{userId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateUserAnalyticsReport(@PathVariable String userId) {
        return generateAnalyticsReport(userId);
    }
    
    /**
     * Generiše opšti sistemski izveštaj (bez personalizovanih analiza)
     */
    @GetMapping(value = "/analytics/system", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateSystemAnalyticsReport() {
        return generateAnalyticsReport(null);
    }
    
    /**
     * Generiše PDF izveštaj i čuva ga u output/ folderu
     */
    @PostMapping("/analytics/save")
    public ResponseEntity<String> generateAndSaveReport(
            @RequestParam(required = false) String userId) {
        
        try {
            System.out.println("Generating and saving analytics report for userId: " + (userId != null ? userId : "ALL"));
            
            byte[] pdfBytes = reportGenerator.generateCompleteAnalyticsReport(userId);
            
            // Kreiraj output folder ako ne postoji
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Kreiraj filename sa timestamp-om
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String userPart = (userId != null && !userId.trim().isEmpty()) ? "_user_" + userId : "_system";
            String filename = "card_transactions_analytics" + userPart + "_" + timestamp + ".pdf";
            String filePath = "output/" + filename;
            
            // Sačuvaj fajl
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }
            
            System.out.println("Report saved successfully: " + filePath);
            
            return ResponseEntity.ok("✅ PDF Report generated and saved successfully!\n" +
                    "📁 File: " + filePath + "\n" +
                    "📊 Size: " + pdfBytes.length + " bytes\n" +
                    "💾 Location: output/ folder (ignored by Git)");
            
        } catch (IOException e) {
            System.out.println("Error saving report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error saving report: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error generating report: " + e.getMessage());
        }
    }
    
    /**
     * Grafana-friendly JSON endpoint za analytics podatke
     */
    @GetMapping("/analytics/json")
    public ResponseEntity<?> getAnalyticsJson(@RequestParam(required = false) String userId) {
        try {
            // Umesto reportGenerator-a, koristimo direktno AnalyticsService
            var analytics = new java.util.HashMap<String, Object>();
            
            // Dodaj različite metruke za Grafana dashboard
            if (userId != null && !userId.trim().isEmpty()) {
                // Personalizovani podaci
                analytics.put("topMerchants", analyticsService.getTopMerchantsByUser(userId, 5));
                analytics.put("categorySpending", analyticsService.getCategorySpendByDay(userId, 30));
            }
            
            // Sistemski podaci (uvek dostupni)
            analytics.put("suspiciousPos", analyticsService.getSuspiciousPos());
            analytics.put("crossChannelHits", analyticsService.getCrossChannelWithin7d());
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            System.out.println("Error getting analytics JSON: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}