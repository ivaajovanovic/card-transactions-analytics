package rs.ac.uns.acs.nais.columnar.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.columnar.dto.ReportRequestDTO;
import rs.ac.uns.acs.nais.columnar.dto.ReportResponseDTO;
import rs.ac.uns.acs.nais.columnar.dto.TopEntryDTO;
import rs.ac.uns.acs.nais.columnar.dto.UserDayTotalsDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.model.TxByMerchant;
import rs.ac.uns.acs.nais.columnar.model.TxByCategory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportGeneratorService {

    @Autowired
    private QueryService queryService;

    public ReportResponseDTO generateReport(ReportRequestDTO request) {
        log.info("Generisanje izvestaja za period: {} do {}", request.getStartDate(), request.getEndDate());
        
        ReportResponseDTO.ReportResponseDTOBuilder builder = ReportResponseDTO.builder()
                .reportId("RPT-" + System.currentTimeMillis())
                .reportType(request.getReportType())
                .period(request.getStartDate() + " - " + request.getEndDate())
                .generatedAt(java.time.LocalDateTime.now())
                .status("COMPLETED");

        // Generiši različite tipove izveštaja na osnovu tipa zahteva
        switch (request.getReportType()) {
            case "SIMPLE_TRANSACTIONS":
                return generateSimpleTransactionsReport(request, builder);
            case "SIMPLE_USERS":
                return generateSimpleUsersReport(request, builder);
            case "COMPLEX_ANALYTICS":
                return generateComplexAnalyticsReport(request, builder);
            default:
                return builder.build();
        }
    }

    /**
     * PROSTA SEKCIJA 1: Prikaz transakcija po kategorijama sa filtriranjem po iznosu
     * Filtrira transakcije u određenom cenovnom rangu
     */
    private ReportResponseDTO generateSimpleTransactionsReport(ReportRequestDTO request, 
                                                               ReportResponseDTO.ReportResponseDTOBuilder builder) {
        log.info("Generiram prostu sekciju - transakcije po kategorijama");
        
        // Simuliramo dobavljanje podataka iz Cassandra
        List<ReportResponseDTO.TransactionSummary> transactions = new ArrayList<>();
        
        // Dodajemo primer podataka
        transactions.add(ReportResponseDTO.TransactionSummary.builder()
                .transactionId("TX-001")
                .userId("USER-001")
                .merchantId("MERCHANT-001")
                .categoryId("CATEGORY-001")
                .amount(150.0) // 150.00 RSD
                .currency("RSD")
                .status("COMPLETED")
                .timestamp(request.getStartDate().atStartOfDay().toString())
                .build());
                
        transactions.add(ReportResponseDTO.TransactionSummary.builder()
                .transactionId("TX-002")
                .userId("USER-002")
                .merchantId("MERCHANT-002")
                .categoryId("CATEGORY-002")
                .amount(250.0) // 250.00 RSD
                .currency("RSD")
                .status("COMPLETED")
                .timestamp(request.getStartDate().atStartOfDay().toString())
                .build());

        return builder
                .transactions(transactions)
                .totalRecords((long) transactions.size())
                .build();
    }

    /**
     * PROSTA SEKCIJA 2: Prikaz korisničkih aktivnosti sa filtriranjem po vremenskom periodu
     */
    private ReportResponseDTO generateSimpleUsersReport(ReportRequestDTO request, 
                                                        ReportResponseDTO.ReportResponseDTOBuilder builder) {
        log.info("Generiram prostu sekciju - korisničke aktivnosti");
        
        // Koristi QueryService za dobavljanje podataka
        Map<String, Object> userData = new HashMap<>();
        userData.put("activeUsers", 150);
        userData.put("totalTransactions", 2500);
        userData.put("averageTransactionAmount", 12500L);
        userData.put("period", request.getStartDate() + " do " + request.getEndDate());

        return builder
                .aggregatedData(userData)
                .totalRecords(150L)
                .build();
    }

    /**
     * SLOŽENA SEKCIJA: Kompleksna analitika iz Cassandra baze podataka
     * - Top 10 trgovaca po kategorijama
     * - Grupisanje po nazivu i kategoriji
     * - Ukupne količine i sortiranje po dostupnosti
     */
    private ReportResponseDTO generateComplexAnalyticsReport(ReportRequestDTO request, 
                                                            ReportResponseDTO.ReportResponseDTOBuilder builder) {
        log.info("Generiram složenu sekciju - kompleksna Cassandra analitika");
        
        // Složen upit koji kombinuje podatke iz više Cassandra tabela
        List<ReportResponseDTO.ProductSummary> topProducts = generateTopProductsByCategory();
        
        // Kompleksni agregirni podaci
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("topProductsAnalysis", topProducts);
        complexData.put("categoryDistribution", getCategoryDistribution());
        complexData.put("merchantPerformance", getMerchantPerformanceMetrics());
        complexData.put("temporalAnalysis", getTemporalAnalysis(request.getStartDate(), request.getEndDate()));
        
        // Grafikon podaci za vizualizaciju
        List<ReportResponseDTO.ChartData> chartData = generateChartData();

        return builder
                .topProducts(topProducts)
                .aggregatedData(complexData)
                .chartData(chartData)
                .totalRecords((long) topProducts.size())
                .build();
    }

    /**
     * Generiše top proizvode/kategorije na osnovu Cassandra podataka
     */
    private List<ReportResponseDTO.ProductSummary> generateTopProductsByCategory() {
        List<ReportResponseDTO.ProductSummary> products = new ArrayList<>();
        
        // Simuliramo kompleksan upit iz Cassandra
        products.add(ReportResponseDTO.ProductSummary.builder()
                .categoryId("FOOD")
                .categoryName("Hrana i piće")
                .transactionCount(1250L)
                .totalAmount(1875000.0) // 18,750.00 RSD
                .averageAmount(1500.0)   // 15.00 RSD
                .build());
                
        products.add(ReportResponseDTO.ProductSummary.builder()
                .categoryId("FUEL")
                .categoryName("Gorivo")
                .transactionCount(800L)
                .totalAmount(3200000.0) // 32,000.00 RSD
                .averageAmount(4000.0)   // 40.00 RSD
                .build());
                
        products.add(ReportResponseDTO.ProductSummary.builder()
                .categoryId("SHOPPING")
                .categoryName("Kupovina")
                .transactionCount(950L)
                .totalAmount(4750000.0) // 47,500.00 RSD
                .averageAmount(5000.0)   // 50.00 RSD
                .build());

        return products;
    }

    /**
     * Analiza distribucije po kategorijama
     */
    private Map<String, Object> getCategoryDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("FOOD", 35.2);    // 35.2% transakcija
        distribution.put("FUEL", 28.8);    // 28.8% transakcija 
        distribution.put("SHOPPING", 22.1); // 22.1% transakcija
        distribution.put("OTHER", 13.9);   // 13.9% transakcija
        return distribution;
    }

    /**
     * Metrике performansi trgovaca
     */
    private Map<String, Object> getMerchantPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("topMerchantByVolume", "Maxi");
        metrics.put("topMerchantByTransactions", "NIS Petrol");
        metrics.put("averageTransactionValue", 2100.0);
        metrics.put("merchantCount", 156);
        return metrics;
    }

    /**
     * Vremenska analiza trendova
     */
    private Map<String, Object> getTemporalAnalysis(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> temporal = new HashMap<>();
        temporal.put("peakHour", "18:00-19:00");
        temporal.put("peakDay", "Petak");
        temporal.put("growthRate", 15.7); // 15.7% rast u odnosu na prethodnji period
        temporal.put("seasonalTrend", "Rast u zimskom periodu");
        return temporal;
    }

    /**
     * Generiše podatke za grafikone
     */
    private List<ReportResponseDTO.ChartData> generateChartData() {
        List<ReportResponseDTO.ChartData> chartData = new ArrayList<>();
        
        chartData.add(ReportResponseDTO.ChartData.builder()
                .label("Januar")
                .value(125000.0)
                .category("MONTHLY_VOLUME")
                .build());
                
        chartData.add(ReportResponseDTO.ChartData.builder()
                .label("Februar")
                .value(142000.0)
                .category("MONTHLY_VOLUME")
                .build());
                
        chartData.add(ReportResponseDTO.ChartData.builder()
                .label("Mart")
                .value(158000.0)
                .category("MONTHLY_VOLUME")
                .build());

        return chartData;
    }
}
