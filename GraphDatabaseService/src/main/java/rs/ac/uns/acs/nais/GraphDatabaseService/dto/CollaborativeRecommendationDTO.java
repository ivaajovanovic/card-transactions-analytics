package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborativeRecommendationDTO {
    private String merchantName;
    private Long score;
}
