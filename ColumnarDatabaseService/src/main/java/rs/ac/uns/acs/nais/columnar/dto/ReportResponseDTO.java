package rs.ac.uns.acs.nais.columnar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private String reportId;
    private String reportType;
    private LocalDateTime generatedAt;
    private String period; // Period za koji je generisan izveštaj
    private String format;
    private String status; // "COMPLETED", "FAILED", "PROCESSING"
    
    // Jednostavan izveštaj - top proizvodi
    private List<ProductSummary> topProducts;
    
    // Složen izveštaj - kompleksni upiti
    private Map<String, Object> aggregatedData;
    private Long totalRecords;
    private List<TransactionSummary> transactions;
    
    // Grafikon podaci
    private List<ChartData> chartData;
    
    // PDF/Grafana podaci
    private String downloadUrl;
    private byte[] pdfContent;
    private String summary; // Kratak opis izveštaja
    
    @Data
    @Builder
    public static class ProductSummary {
        private String categoryId;
        private String categoryName;
        private Long transactionCount;
        private Double totalAmount;
        private Double averageAmount;
    }
    
    @Data
    @Builder
    public static class TransactionSummary {
        private String transactionId; // Umesto txId
        private String userId;
        private String merchantId;
        private String merchantName; // Dodano za merchant name
        private String categoryId;
        private Double amount;
        private String currency;
        private String status;
        private String timestamp; // Umesto LocalDateTime
    }
    
    @Data
    @Builder
    public static class ChartData {
        private String label;
        private Double value;
        private String category;
        private LocalDateTime date;
    }
}