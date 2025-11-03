package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationDTO {
    private String merchantId;
    private String merchantName;
    private String reason;
    private Double expectedBenefit;
}
