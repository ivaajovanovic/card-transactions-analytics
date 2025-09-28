package rs.ac.uns.acs.nais.columnar.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tx_by_user")
public class TxByUser {

    @PrimaryKey
    private TxByUserKey key; 

    @Column("tx_id")        private UUID txId;
    @Column("merchant_id")  private UUID merchantId;
    @Column("card_id")      private UUID cardId;
    @Column("category_id")  private UUID categoryId;
    @Column("amount_cents") private Long amountCents;
    @Column("currency")     private String currency;
    @Column("status")       private String status;
    @Column("occurred_at")  private Instant occurredAt;
}
