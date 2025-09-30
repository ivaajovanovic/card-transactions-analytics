package rs.ac.uns.acs.nais.columnar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.columnar.dto.ReportRequestDTO;
import rs.ac.uns.acs.nais.columnar.dto.ReportResponseDTO;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGeneratorService {

    public ReportResponseDTO generateReport(ReportRequestDTO request) {
        log.info("Generating report: type={}", request.getReportType());
        
        if ("simple".equals(request.getReportType())) {
            return generateSimpleReport(request);
        } else {
            return generateComplexReport(request);
        }
    }

    private ReportResponseDTO generateSimpleReport(ReportRequestDTO request) {
        List<ReportResponseDTO.ProductSummary> topProducts = new ArrayList<>();
        
        topProducts.add(ReportResponseDTO.ProductSummary.builder()
                .categoryId("FOOD")
                .categoryName("Food & Restaurants")
                .transactionCount(150L)
                .totalAmount(3250.75)
                .averageAmount(21.67)
                .build());
        
        Map<String, Object> aggregatedData = new HashMap<>();
        aggregatedData.put("totalProducts", topProducts.size());
        
        return ReportResponseDTO.builder()
                .reportType("simple")
                .generatedAt(LocalDateTime.now())
                .period(request.getStartDate() + " to " + request.getEndDate())
                .topProducts(topProducts)
                .aggregatedData(aggregatedData)
                .build();
    }

    private ReportResponseDTO generateComplexReport(ReportRequestDTO request) {
        List<ReportResponseDTO.TransactionSummary> transactions = new ArrayList<>();
        
        transactions.add(ReportResponseDTO.TransactionSummary.builder()
                .txId("tx-001")
                .userId("user-123")
                .merchantId("LIDL")
                .categoryId("FOOD")
                .amount(45.50)
                .currency("EUR")
                .status("COMPLETED")
                .occurredAt(LocalDateTime.now().minusDays(1))
                .build());
        
        Map<String, Object> aggregatedData = new HashMap<>();
        aggregatedData.put("totalAmount", 45.50);
        aggregatedData.put("transactionCount", 1);
        
        return ReportResponseDTO.builder()
                .reportType("complex")
                .generatedAt(LocalDateTime.now())
                .period(request.getStartDate() + " to " + request.getEndDate())
                .transactions(transactions)
                .aggregatedData(aggregatedData)
                .build();
    }
}


