package rs.ac.uns.acs.nais.columnar.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private UUID txId;
    private UUID userId;
    private UUID cardId;
    private UUID merchantId;
    private UUID categoryId;
    private long amountCents;
    private String currency;
    private String status;
    private Instant occurredAt;
}
