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
 * Table: transactions_by_user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions_by_user")
public class TxByUser {

    @PrimaryKey
    private TxByUserKey key;

    @Column("tx_id")
    private UUID txId;

    @Column("card_id")
    private UUID cardId;

    @Column("merchant_id")
    private UUID merchantId;

    @Column("category_id")
    private UUID categoryId;

    @Column("amount_cents")
    private Long amountCents;

    @Column("currency")
    private String currency;

    @Column("status")
    private String status; // npr. APPROVED / DECLINED / REVERSED
}
