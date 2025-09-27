package rs.ac.uns.acs.nais.columnar.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tx_by_merchant")
public class TxByMerchant {

    @PrimaryKey
    private TxByMerchantKey key;

    @Column("tx_id")        private UUID txId;
    @Column("user_id")      private UUID userId;
    @Column("card_id")      private UUID cardId;
    @Column("merchant_id")  private UUID merchantId;
    @Column("category_id")  private UUID categoryId;
    @Column("amount_cents") private Long amountCents; 
    @Column("currency")     private String currency;
    @Column("status")       private String status;
    @Column("occurred_at")  private Instant occurredAt;
}
