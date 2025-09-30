package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousActivityUpdateRequest {
    private String userId;
    private Long suspiciousTransactionCount;
}