package rs.ac.uns.acs.nais.columnar.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.columnar.dto.ReportRequestDTO;
import rs.ac.uns.acs.nais.columnar.dto.ReportResponseDTO;
import rs.ac.uns.acs.nais.columnar.service.PDFGeneratorService;
import rs.ac.uns.acs.nais.columnar.service.ReportGeneratorService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller za generisanje i preuzimanje izveštaja
 * Podržava JSON i PDF format izveštaja
 */
@RestController
@RequestMapping("/api/reports")
@Slf4j
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportGeneratorService reportGeneratorService;
    
    @Autowired
    private PDFGeneratorService pdfGeneratorService;

    /**
     * Generiše izveštaj u JSON formatu
     * POST /api/reports/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDTO> generateReport(@RequestBody ReportRequestDTO request) {
        log.info("=== REPORT CONTROLLER === Received report request: type={}, format={}", 
                request.getReportType(), request.getFormat());
        
        try {
            ReportResponseDTO response = reportGeneratorService.generateReport(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("=== REPORT CONTROLLER === Error generating report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to generate report: " + e.getMessage()));
        }
    }

    /**
     * Generiše i preuzima izveštaj u PDF formatu
     * POST /api/reports/pdf
     */
    @PostMapping("/pdf")
    public ResponseEntity<byte[]> generatePDFReport(@RequestBody ReportRequestDTO request) {
        log.info("=== REPORT CONTROLLER === Received PDF report request: type={}", 
                request.getReportType());
        
        try {
            // Prvo generiši izveštaj
            ReportResponseDTO report = reportGeneratorService.generateReport(request);
            
            // Zatim konvertuj u PDF
            byte[] pdfBytes;
            String filename;
            
            if ("simple".equals(request.getReportType())) {
                pdfBytes = pdfGeneratorService.generateSimpleReportPDF(report.getTopProducts(), request);
                filename = "simple_report_" + getCurrentTimestamp() + ".html";
            } else if ("complex".equals(request.getReportType())) {
                pdfBytes = pdfGeneratorService.generateComplexReportPDF(
                    report.getTransactions(), 
                    report.getAggregatedData(), 
                    request
                );
                filename = "complex_report_" + getCurrentTimestamp() + ".html";
            } else if ("graphical".equals(request.getReportType())) {
                pdfBytes = pdfGeneratorService.generateGraphicalReportPDF(report.getChartData(), request);
                filename = "graphical_report_" + getCurrentTimestamp() + ".html";
            } else {
                throw new IllegalArgumentException("Unsupported report type: " + request.getReportType());
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("=== REPORT CONTROLLER === Error generating PDF report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Preuzima osnovne informacije o mogućnostima izveštaja
     * GET /api/reports/info
     */
    @GetMapping("/info")
    public ResponseEntity<Object> getReportInfo() {
        log.info("=== REPORT CONTROLLER === Getting report info");
        
        try {
            var info = new java.util.HashMap<String, Object>();
            info.put("supportedTypes", java.util.Arrays.asList("simple", "complex", "graphical"));
            info.put("supportedFormats", java.util.Arrays.asList("json", "pdf"));
            info.put("supportedGrouping", java.util.Arrays.asList("day", "week", "month", "category", "merchant"));
            info.put("supportedThemes", java.util.Arrays.asList("light", "dark", "corporate"));
            info.put("maxDateRange", 365); // days
            info.put("version", "1.0");
            info.put("description", "Advanced Card Transaction Analytics Report Generator");
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            log.error("=== REPORT CONTROLLER === Error getting report info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Test endpoint za proveru dostupnosti
     * GET /api/reports/test
     */
    @GetMapping("/test")
    public ResponseEntity<Object> testReportService() {
        log.info("=== REPORT CONTROLLER === Test endpoint called");
        
        try {
            var testResponse = new java.util.HashMap<String, Object>();
            testResponse.put("status", "OK");
            testResponse.put("timestamp", LocalDateTime.now().toString());
            testResponse.put("service", "ReportController");
            testResponse.put("database", "Cassandra");
            testResponse.put("message", "Report service is running and ready");
            
            return ResponseEntity.ok(testResponse);
            
        } catch (Exception e) {
            log.error("=== REPORT CONTROLLER === Error in test endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint za preuzimanje mock podataka za testiranje
     * GET /api/reports/mock-data
     */
    @GetMapping("/mock-data")
    public ResponseEntity<Object> getMockData() {
        log.info("=== REPORT CONTROLLER === Getting mock data");
        
        try {
            var mockData = new java.util.HashMap<String, Object>();
            
            // Sample request
            ReportRequestDTO sampleRequest = new ReportRequestDTO();
            sampleRequest.setReportType("simple");
            sampleRequest.setFormat("json");
            sampleRequest.setStartDate(java.time.LocalDate.now().minusDays(30));
            sampleRequest.setEndDate(java.time.LocalDate.now());
            sampleRequest.setGroupBy("category");
            sampleRequest.setTheme("light");
            
            mockData.put("sampleRequest", sampleRequest);
            mockData.put("note", "Use this sample request to test the /generate endpoint");
            mockData.put("endpoints", java.util.Arrays.asList(
                "POST /api/reports/generate - Generate JSON report",
                "POST /api/reports/pdf - Generate PDF report",
                "GET /api/reports/info - Get service info",
                "GET /api/reports/test - Test service status"
            ));
            
            return ResponseEntity.ok(mockData);
            
        } catch (Exception e) {
            log.error("=== REPORT CONTROLLER === Error getting mock data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods
    
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
    
    private ReportResponseDTO createErrorResponse(String errorMessage) {
        ReportResponseDTO errorResponse = new ReportResponseDTO();
        var errorData = new java.util.HashMap<String, Object>();
        errorData.put("error", errorMessage);
        errorData.put("timestamp", LocalDateTime.now().toString());
        errorResponse.setAggregatedData(errorData);
        return errorResponse;
    }
}