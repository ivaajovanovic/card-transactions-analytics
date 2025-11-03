package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSpendingByCategory {
    private String categoryCode;
    private String categoryName;
    private Long transactionCount;
    private Double totalAmount;
    private Double avgAmount;
}
