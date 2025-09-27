package rs.ac.uns.acs.nais.columnar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
public class TxByUserKey {

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "tx_date", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private LocalDate txDate;

    // timeuuid / UUID koji obezbeđuje jedinstven i vremenski-sortiran ključ
    @PrimaryKeyColumn(name = "tx_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private UUID txTime;
}
