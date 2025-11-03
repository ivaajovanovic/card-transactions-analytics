package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMonthlySpending {
    private String yearMonth; // e.g. "2024-01"
    private Long transactionCount;
    private Double totalAmount;
    private Double avgAmount;
    private Integer uniqueMerchants;
}
