package rs.ac.uns.acs.nais.columnar.model;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Table: transactions_by_category
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions_by_category")
public class TxByCategory {

    @PrimaryKey
    private TxByCategoryKey key;

    @Column("tx_id")
    private UUID txId;

    @Column("user_id")
    private UUID userId;

    @Column("card_id")
    private UUID cardId;

    @Column("merchant_id")
    private UUID merchantId;

    @Column("amount_cents")
    private Long amountCents;

    @Column("currency")
    private String currency;

    @Column("status")
    private String status;
}
