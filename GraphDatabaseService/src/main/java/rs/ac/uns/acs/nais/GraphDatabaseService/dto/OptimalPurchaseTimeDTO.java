package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptimalPurchaseTimeDTO {
    private String dayOfWeek;  // e.g., "Monday", "Tuesday"
    private Long hour;
    private Double total;
    private Long transactionCount;
    private Double avgAmount;  // total / transactionCount
}
