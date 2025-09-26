package rs.ac.uns.acs.nais.columnar.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PRIMARY KEY ((category_id, tx_date), tx_time DESC)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TxByCategoryKey implements Serializable {

    @PrimaryKeyColumn(name = "category_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private UUID categoryId;

    @PrimaryKeyColumn(name = "tx_date", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    private LocalDate txDate;

    @PrimaryKeyColumn(name = "tx_time", type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING)
    @CassandraType(type = CassandraType.Name.TIMEUUID)
    private UUID txTime; // timeuuid
}
