package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousTransactionRequest {
    private String transactionId;
    private String userId;
    private String cardId;
    private Double amount;
    private String reason; // razlog zbog kojeg je sumnjiva
}