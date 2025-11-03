package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTopMerchant {
    private String merchantId;
    private String merchantName;
    private String categoryName;
    private Long transactionCount;
    private Double totalSpent;
    private Double avgAmount;
}
