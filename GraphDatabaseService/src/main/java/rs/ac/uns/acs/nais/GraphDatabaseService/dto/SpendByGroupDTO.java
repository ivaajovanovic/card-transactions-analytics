package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpendByGroupDTO {
    private String groupKey; // purpose/category/paymentType
    private Double totalAmount;
    private Long txnCount;
}
