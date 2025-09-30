// src/main/java/rs/ac/uns/acs/nais/columnar/model/TxByCategory.java
package rs.ac.uns.acs.nais.columnar.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions_by_category")
public class TxByCategory {

    @PrimaryKey
    private TxByCategoryKey key; // category_id, tx_date, tx_time

    @Column("tx_id")        private UUID txId;
    @Column("user_id")      private UUID userId;
    @Column("card_id")      private UUID cardId;
    @Column("merchant_id")  private UUID merchantId;
    @Column("amount_cents") private Long amountCents;
    @Column("currency")     private String currency;
    @Column("status")       private String status;
}
