package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardBenefitDTO {
    private String card;        // card identifier (e.g., panHash)
    private String program;     // reward program name
    private Double rate;        // reward rate (e.g., 0.05 for 5%)
    private Double cap;         // optional cap
    private String conditions;  // textual conditions
}
