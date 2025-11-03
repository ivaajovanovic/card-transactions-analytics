package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCardUsage {
    private String cardId;
    private String network;
    private String type;
    private Long transactionCount;
    private Double totalSpent;
    private Long contactlessCount;
    private Long installmentsCount;
}
