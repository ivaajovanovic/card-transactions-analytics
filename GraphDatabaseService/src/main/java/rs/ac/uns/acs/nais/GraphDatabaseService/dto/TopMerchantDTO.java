package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopMerchantDTO {
    private String merchantId;
    private String merchantName;
    private String category;
    private Double totalAmount;
    private Long txnCount;
}
