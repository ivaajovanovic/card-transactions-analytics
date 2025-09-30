package rs.ac.uns.acs.nais.columnar.dto;

import lombok.*;
import java.util.List;

/**
 * DTO za fraud analysis response koji vraća Elasticsearch
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisResult {
    private double fraudScore;           // 0.0 - 1.0 (0 = safe, 1 = high fraud risk)
    private String riskLevel;            // LOW, MEDIUM, HIGH, CRITICAL
    private List<String> riskFactors;    // Lista razloga zašto je sumnjiva
    private String recommendation;       // APPROVE, REVIEW, BLOCK
    private AnalysisDetails details;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisDetails {
        private Double amountDeviationScore;      // Koliko se iznos razlikuje od uobičajenog
        private Double velocityScore;             // Broj transakcija u kratkom vremenu
        private Double locationScore;             // Neobična lokacija
        private Double merchantScore;             // Neobičan merchant
        private Double timePatternScore;          // Neobično vreme transakcije
        private Integer analysisTimeMs;           // Vreme potrebno za analizu
    }
}