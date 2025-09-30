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
import rs.ac.uns.acs.nais.columnar.service.QueryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    
    @Autowired
    private QueryService queryService;

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

    /**
     * SLOŽENI UPIT 1: Najprodavaniji proizvodi po kategoriji
     * GET /api/reports/complex/top-products/{categoryId}?limit=10
     */
    @GetMapping("/complex/top-products/{categoryId}")
    public ResponseEntity<Map<String, Object>> getTopProductsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("=== SLOŽENI UPIT 1 === Analiza proizvoda za kategoriju: {}, limit: {}", categoryId, limit);
        
        try {
            UUID categoryUuid = UUID.fromString(categoryId);
            List<Map<String, Object>> products = queryService.getTopProductsByCategory(categoryUuid, limit);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("analysis_type", "top_products_by_category");
            response.put("category_id", categoryId);
            response.put("limit", limit);
            response.put("products_found", products.size());
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("products", products);
            
            log.info("=== SLOŽENI UPIT 1 === Vraćam {} proizvoda za kategoriju {}", products.size(), categoryId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("=== SLOŽENI UPIT 1 === Nevaljan UUID format: {}", categoryId);
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "Nevaljan UUID format za kategoriju");
            error.put("provided_category_id", categoryId);
            error.put("example", "f47ac10b-58cc-4372-a567-0e02b2c3d479");
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("=== SLOŽENI UPIT 1 === Greška: {}", e.getMessage(), e);
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", e.getMessage());
            error.put("category_id", categoryId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * SLOŽENI UPIT 2: Napredna analiza transakcija za period
     * GET /api/reports/complex/analytics?from=2024-01-01&to=2024-12-31
     */
    @GetMapping("/complex/analytics")
    public ResponseEntity<Map<String, Object>> getAdvancedTransactionAnalytics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        
                log.info("=== SLOŽENI UPIT 2 - NOVA VERZIJA === Analiza transakcija za period: null do null");
        
        try {
            // Default period ako nije specificiran
            LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().minusMonths(1);
            LocalDate toDate = to != null ? LocalDate.parse(to) : LocalDate.now();
            
            if (fromDate.isAfter(toDate)) {
                Map<String, Object> error = new java.util.HashMap<>();
                error.put("error", "Datum 'from' mora biti pre datuma 'to'");
                error.put("from_date", fromDate.toString());
                error.put("to_date", toDate.toString());
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> analytics = queryService.getAdvancedTransactionAnalysis(fromDate, toDate);
            
            // Dodaj meta-informacije
            analytics.put("analysis_type", "advanced_transaction_analytics");
            analytics.put("requested_from", fromDate.toString());
            analytics.put("requested_to", toDate.toString());
            analytics.put("analysis_timestamp", LocalDateTime.now().toString());
            
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
            log.info("=== SLOŽENI UPIT 2 === Analiza završena za {} dana", daysBetween);
            
            return ResponseEntity.ok(analytics);
            
        } catch (java.time.format.DateTimeParseException e) {
            log.error("=== SLOŽENI UPIT 2 === Nevaljan format datuma: {}", e.getMessage());
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "Nevaljan format datuma. Koristiti YYYY-MM-DD format");
            error.put("example_from", "2024-01-01");
            error.put("example_to", "2024-12-31");
            error.put("provided_from", from);
            error.put("provided_to", to);
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("=== SLOŽENI UPIT 2 === Greška: {}", e.getMessage(), e);
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", e.getMessage());
            error.put("from", from);
            error.put("to", to);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * ENHANCED ANALYTICS PDF: Generiše PDF izveštaj sa naprednom analizom (4 sekcije)
     * GET /api/reports/complex/analytics/pdf?from=2024-08-30&to=2025-09-30
     */
    @GetMapping("/complex/analytics/pdf")
    public ResponseEntity<byte[]> getEnhancedAnalyticsPDF(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        
        log.info("🔥🔥🔥 ENHANCED ANALYTICS PDF === Generisanje PDF izveštaja za period: {} do {}", from, to);
        
        try {
            // Default period ako nije specificiran
            LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().minusMonths(1);
            LocalDate toDate = to != null ? LocalDate.parse(to) : LocalDate.now();
            
            if (fromDate.isAfter(toDate)) {
                log.error("=== ENHANCED ANALYTICS PDF === Nevaljan period: {} > {}", fromDate, toDate);
                return ResponseEntity.badRequest().build();
            }
            
            // Dobij analytics podatke
            Map<String, Object> analytics = queryService.getAdvancedTransactionAnalysis(fromDate, toDate);
            
            // Dodaj meta-informacije za PDF
            analytics.put("analysis_type", "advanced_transaction_analytics");
            analytics.put("requested_from", fromDate.toString());
            analytics.put("requested_to", toDate.toString());
            analytics.put("analysis_period", fromDate.toString() + " do " + toDate.toString());
            analytics.put("analysis_timestamp", LocalDateTime.now().toString());
            
            // Generiši PDF
            byte[] pdfBytes = pdfGeneratorService.generateEnhancedAnalyticsPDF(analytics);
            
            // Pripremi response headers
            String filename = "enhanced_analytics_report_" + getCurrentTimestamp() + ".html";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            log.info("🔥🔥🔥 ENHANCED ANALYTICS PDF === PDF generisan uspešno, veličina: {} bytes", pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (java.time.format.DateTimeParseException e) {
            log.error("=== ENHANCED ANALYTICS PDF === Nevaljan format datuma: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            log.error("=== ENHANCED ANALYTICS PDF === Greška pri generisanju PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================
    // UPDATE ENDPOINTS FOR KEY TABLES  
    // ================================
    
    /**
     * Ažurira iznos transakcije
     * PUT /api/reports/transaction/amount?userId=uuid&date=2024-01-01&transactionId=uuid&amount=100.50&currency=RSD
     */
    @PutMapping("/transaction/amount")
    public ResponseEntity<Map<String, Object>> updateTransactionAmount(
            @RequestParam UUID userId,
            @RequestParam String date,
            @RequestParam UUID transactionId,
            @RequestParam double amount,
            @RequestParam String currency) {
        
        log.info("=== UPDATE TRANSACTION AMOUNT === tx_id: {}, new_amount: {} {}", transactionId, amount, currency);
        
        try {
            LocalDate transactionDate = LocalDate.parse(date);
            boolean success = queryService.updateTransactionAmount(userId, transactionDate, transactionId, amount, currency);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "update_transaction_amount");
            response.put("transaction_id", transactionId);
            response.put("user_id", userId);
            response.put("new_amount", amount);
            response.put("currency", currency);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating transaction amount: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Ažurira status transakcije u svim tabelama
     * PUT /api/reports/transaction/status?userId=uuid&merchantId=id&categoryId=id&date=2024-01-01&transactionId=uuid&status=COMPLETED
     */
    @PutMapping("/transaction/status")
    public ResponseEntity<Map<String, Object>> updateTransactionStatus(
            @RequestParam UUID userId,
            @RequestParam String merchantId,
            @RequestParam String categoryId,
            @RequestParam String date,
            @RequestParam UUID transactionId,
            @RequestParam String status) {
        
        log.info("=== UPDATE TRANSACTION STATUS === tx_id: {}, new_status: {}", transactionId, status);
        
        try {
            LocalDate transactionDate = LocalDate.parse(date);
            boolean success = queryService.updateTransactionStatus(userId, merchantId, categoryId, 
                                                                  transactionDate, transactionId, status);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "update_transaction_status");
            response.put("transaction_id", transactionId);
            response.put("user_id", userId);
            response.put("merchant_id", merchantId);
            response.put("category_id", categoryId);
            response.put("new_status", status);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating transaction status: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Ažurira merchant agregat
     * PUT /api/reports/merchant/{merchantId}/aggregate?totalTransactions=100&totalAmount=50000.0&avgAmount=500.0&lastUpdated=2024-01-01
     */
    @PutMapping("/merchant/{merchantId}/aggregate")
    public ResponseEntity<Map<String, Object>> updateMerchantAggregate(
            @PathVariable String merchantId,
            @RequestParam long totalTransactions,
            @RequestParam double totalAmount,
            @RequestParam double avgAmount,
            @RequestParam String lastUpdated) {
        
        log.info("=== UPDATE MERCHANT AGGREGATE === merchant_id: {}, total_tx: {}, total_amount: {}", 
                merchantId, totalTransactions, totalAmount);
        
        try {
            boolean success = queryService.updateMerchantAggregate(merchantId, totalTransactions, 
                                                                  totalAmount, avgAmount, lastUpdated);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "update_merchant_aggregate");
            response.put("merchant_id", merchantId);
            response.put("total_transactions", totalTransactions);
            response.put("total_amount", totalAmount);
            response.put("avg_amount", avgAmount);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating merchant aggregate: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Ažurira kompletnu transakciju u svim tabelama
     * PUT /api/reports/transaction/complete?transactionId=uuid&userId=uuid&merchantId=id&categoryId=id&date=2024-01-01&amount=100.0&currency=RSD&status=COMPLETED&description=Updated
     */
    @PutMapping("/transaction/complete")
    public ResponseEntity<Map<String, Object>> updateTransactionCompletely(
            @RequestParam UUID transactionId,
            @RequestParam UUID userId,
            @RequestParam String merchantId,
            @RequestParam String categoryId,
            @RequestParam String date,
            @RequestParam double amount,
            @RequestParam String currency,
            @RequestParam String status,
            @RequestParam String description) {
        
        log.info("=== UPDATE TRANSACTION COMPLETELY === tx_id: {}, amount: {}, status: {}", 
                transactionId, amount, status);
        
        try {
            LocalDate transactionDate = LocalDate.parse(date);
            boolean success = queryService.updateTransactionCompletely(transactionId, userId, merchantId, categoryId,
                                                                       transactionDate, amount, currency, status, description);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "update_transaction_completely");
            response.put("transaction_id", transactionId);
            response.put("user_id", userId);
            response.put("merchant_id", merchantId);
            response.put("category_id", categoryId);
            response.put("new_amount", amount);
            response.put("new_currency", currency);
            response.put("new_status", status);
            response.put("new_description", description);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating transaction completely: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ================================
    // DELETE ENDPOINTS FOR ALL TABLES
    // ================================
    
    /**
     * Briše transakciju iz transactions_by_user tabele
     * DELETE /api/reports/user/{userId}/transaction?date=2024-01-01&transactionId=uuid
     */
    @DeleteMapping("/user/{userId}/transaction")
    public ResponseEntity<Map<String, Object>> deleteUserTransaction(
            @PathVariable UUID userId,
            @RequestParam String date,
            @RequestParam UUID transactionId) {
        
        log.info("=== DELETE USER TRANSACTION === user_id: {}, date: {}, tx_id: {}", userId, date, transactionId);
        
        try {
            LocalDate transactionDate = LocalDate.parse(date);
            boolean success = queryService.deleteTransactionByUser(userId, transactionDate, transactionId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_user_transaction");
            response.put("user_id", userId);
            response.put("transaction_date", date);
            response.put("transaction_id", transactionId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting user transaction: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše sve transakcije za korisnika
     * DELETE /api/reports/user/{userId}/all
     */
    @DeleteMapping("/user/{userId}/all")
    public ResponseEntity<Map<String, Object>> deleteAllUserTransactions(@PathVariable UUID userId) {
        log.info("=== DELETE ALL USER TRANSACTIONS === user_id: {}", userId);
        
        try {
            boolean success = queryService.deleteAllTransactionsForUser(userId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_all_user_transactions");
            response.put("user_id", userId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting all user transactions: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše transakciju iz transactions_by_merchant tabele
     * DELETE /api/reports/merchant/{merchantId}/transaction?date=2024-01-01&transactionId=uuid
     */
    @DeleteMapping("/merchant/{merchantId}/transaction")
    public ResponseEntity<Map<String, Object>> deleteMerchantTransaction(
            @PathVariable String merchantId,
            @RequestParam String date,
            @RequestParam UUID transactionId) {
        
        log.info("=== DELETE MERCHANT TRANSACTION === merchant_id: {}, date: {}, tx_id: {}", merchantId, date, transactionId);
        
        try {
            LocalDate transactionDate = LocalDate.parse(date);
            boolean success = queryService.deleteTransactionByMerchant(merchantId, transactionDate, transactionId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_merchant_transaction");
            response.put("merchant_id", merchantId);
            response.put("transaction_date", date);
            response.put("transaction_id", transactionId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting merchant transaction: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše sve transakcije za merchant-a
     * DELETE /api/reports/merchant/{merchantId}/all
     */
    @DeleteMapping("/merchant/{merchantId}/all")
    public ResponseEntity<Map<String, Object>> deleteAllMerchantTransactions(@PathVariable String merchantId) {
        log.info("=== DELETE ALL MERCHANT TRANSACTIONS === merchant_id: {}", merchantId);
        
        try {
            boolean success = queryService.deleteAllTransactionsForMerchant(merchantId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_all_merchant_transactions");
            response.put("merchant_id", merchantId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting all merchant transactions: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše transakciju iz transactions_by_category tabele
     * DELETE /api/reports/category/{categoryId}/transaction?date=2024-01-01&transactionId=uuid
     */
    @DeleteMapping("/category/{categoryId}/transaction")
    public ResponseEntity<Map<String, Object>> deleteCategoryTransaction(
            @PathVariable String categoryId,
            @RequestParam String date,
            @RequestParam UUID transactionId) {
        
        log.info("=== DELETE CATEGORY TRANSACTION === category_id: {}, date: {}, tx_id: {}", categoryId, date, transactionId);
        
        try {
            LocalDate transactionDate = LocalDate.parse(date);
            boolean success = queryService.deleteTransactionByCategory(categoryId, transactionDate, transactionId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_category_transaction");
            response.put("category_id", categoryId);
            response.put("transaction_date", date);
            response.put("transaction_id", transactionId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting category transaction: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše sve transakcije za kategoriju
     * DELETE /api/reports/category/{categoryId}/all
     */
    @DeleteMapping("/category/{categoryId}/all")
    public ResponseEntity<Map<String, Object>> deleteAllCategoryTransactions(@PathVariable String categoryId) {
        log.info("=== DELETE ALL CATEGORY TRANSACTIONS === category_id: {}", categoryId);
        
        try {
            boolean success = queryService.deleteAllTransactionsForCategory(categoryId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_all_category_transactions");
            response.put("category_id", categoryId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting all category transactions: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše dnevne totale za korisnika
     * DELETE /api/reports/user/{userId}/daily-total?date=2024-01-01
     */
    @DeleteMapping("/user/{userId}/daily-total")
    public ResponseEntity<Map<String, Object>> deleteUserDailyTotal(
            @PathVariable UUID userId,
            @RequestParam String date) {
        
        log.info("=== DELETE USER DAILY TOTAL === user_id: {}, date: {}", userId, date);
        
        try {
            LocalDate transactionDate = LocalDate.parse(date);
            boolean success = queryService.deleteUserDailyTotal(userId, transactionDate);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_user_daily_total");
            response.put("user_id", userId);
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting user daily total: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše sve dnevne totale za korisnika
     * DELETE /api/reports/user/{userId}/daily-totals/all
     */
    @DeleteMapping("/user/{userId}/daily-totals/all")
    public ResponseEntity<Map<String, Object>> deleteAllUserDailyTotals(@PathVariable UUID userId) {
        log.info("=== DELETE ALL USER DAILY TOTALS === user_id: {}", userId);
        
        try {
            boolean success = queryService.deleteAllDailyTotalsForUser(userId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_all_user_daily_totals");
            response.put("user_id", userId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting all user daily totals: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše merchant agregat
     * DELETE /api/reports/merchant/{merchantId}/aggregate
     */
    @DeleteMapping("/merchant/{merchantId}/aggregate")
    public ResponseEntity<Map<String, Object>> deleteMerchantAggregate(@PathVariable String merchantId) {
        log.info("=== DELETE MERCHANT AGGREGATE === merchant_id: {}", merchantId);
        
        try {
            boolean success = queryService.deleteMerchantAggregate(merchantId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_merchant_aggregate");
            response.put("merchant_id", merchantId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting merchant aggregate: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše category agregat
     * DELETE /api/reports/category/{categoryId}/aggregate
     */
    @DeleteMapping("/category/{categoryId}/aggregate")
    public ResponseEntity<Map<String, Object>> deleteCategoryAggregate(@PathVariable String categoryId) {
        log.info("=== DELETE CATEGORY AGGREGATE === category_id: {}", categoryId);
        
        try {
            boolean success = queryService.deleteCategoryAggregate(categoryId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_category_aggregate");
            response.put("category_id", categoryId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting category aggregate: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Briše transakciju potpuno iz svih tabela
     * DELETE /api/reports/transaction/complete?transactionId=uuid&userId=uuid&merchantId=id&categoryId=id&date=2024-01-01
     */
    @DeleteMapping("/transaction/complete")
    public ResponseEntity<Map<String, Object>> deleteTransactionCompletely(
            @RequestParam UUID transactionId,
            @RequestParam UUID userId,
            @RequestParam String merchantId,
            @RequestParam String categoryId,
            @RequestParam String date) {
        
        log.info("=== DELETE TRANSACTION COMPLETELY === tx_id: {}, user: {}, merchant: {}, category: {}, date: {}", 
                transactionId, userId, merchantId, categoryId, date);
        
        try {
            LocalDate transactionDate = LocalDate.parse(date);
            boolean success = queryService.deleteTransactionCompletely(transactionId, userId, merchantId, categoryId, transactionDate);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_transaction_completely");
            response.put("transaction_id", transactionId);
            response.put("user_id", userId);
            response.put("merchant_id", merchantId);
            response.put("category_id", categoryId);
            response.put("date", date);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting transaction completely: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * DANGEROUS: Briše sve podatke iz svih tabela (za testiranje)
     * DELETE /api/reports/all-data?confirm=yes
     */
    @DeleteMapping("/all-data")
    public ResponseEntity<Map<String, Object>> deleteAllData(@RequestParam String confirm) {
        log.warn("=== DELETE ALL DATA === confirm: {}", confirm);
        
        if (!"yes".equals(confirm)) {
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", "Must confirm with 'confirm=yes' parameter");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            boolean success = queryService.deleteAllData();
            
            var response = new java.util.HashMap<String, Object>();
            response.put("success", success);
            response.put("operation", "delete_all_data");
            response.put("warning", "ALL DATA DELETED FROM ALL TABLES");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting all data: {}", e.getMessage(), e);
            var error = new java.util.HashMap<String, Object>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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