package rs.ac.uns.acs.nais.columnar.model;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PRIMARY KEY ((user_id), tx_date DESC)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_daily_totals")
public class UserDailyTotal {

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private UUID userId;

    @PrimaryKeyColumn(name = "tx_date", type = PrimaryKeyType.CLUSTERED, ordinal = 1, ordering = Ordering.DESCENDING)
    private LocalDate txDate;

    @Column("day_count")
    private Integer dayCount;

    @Column("day_amount_cents")
    private Long dayAmountCents;
}
