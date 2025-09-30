package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaTransactionResult {
    private String transactionId;
    private boolean success;
    private String step; // korak u kome se dogodila greška
    private String errorMessage;
    private Object rollbackData; // podaci potrebni za rollback
}