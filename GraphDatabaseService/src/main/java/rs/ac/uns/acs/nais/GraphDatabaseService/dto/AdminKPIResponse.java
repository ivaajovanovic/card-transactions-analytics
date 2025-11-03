package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminKPIResponse {
    private Long totalUsers;
    private Long totalMerchants;
    private Long totalTransactions;
    private Double totalVolume;
    private Double userGrowth;
    private Double merchantGrowth;
    private Double transactionGrowth;
    private Double volumeGrowth;
}
