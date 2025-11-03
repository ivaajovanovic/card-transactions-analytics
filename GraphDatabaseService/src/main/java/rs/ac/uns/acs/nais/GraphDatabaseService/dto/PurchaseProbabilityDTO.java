package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseProbabilityDTO {
    private String merchantId;
    private Double probability; // placeholder for future model
}
