package rs.ac.uns.acs.nais.columnar.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

/**
 * DTO za fraud analysis request koji se šalje u Elasticsearch
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisRequest {
    private UUID userId;
    private UUID transactionId;
    private List<RecentTransactionDTO> recentTransactions;
    private UserBehaviorProfile userProfile;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTransactionDTO {
        private UUID txId;
        private Long amountCents;
        private String merchantId;
        private String category;
        private String timestamp;
        private String location;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBehaviorProfile {
        private Double avgDailySpending;
        private Double avgTransactionAmount;     // Dodaj ovo polje
        private Integer avgDailyTransactions;
        private List<String> frequentMerchants;
        private List<String> frequentCategories;
        private String primaryLocation;
        private List<String> preferredMerchants;  // Dodaj i ovo
        private List<String> usualLocations;      // I ovo
    }
}