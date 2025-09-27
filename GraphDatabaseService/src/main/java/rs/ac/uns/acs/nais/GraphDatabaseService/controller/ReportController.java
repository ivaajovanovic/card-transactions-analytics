package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.report.ReportGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportGenerator reportGenerator;
    
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
}