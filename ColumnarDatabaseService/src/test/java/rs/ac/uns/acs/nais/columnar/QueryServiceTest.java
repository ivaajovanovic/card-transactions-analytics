package rs.ac.uns.acs.nais.columnar;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.uns.acs.nais.columnar.dto.TopEntryDTO;
import rs.ac.uns.acs.nais.columnar.repo.*;
import rs.ac.uns.acs.nais.columnar.service.QueryService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueryServiceTest {

    @Mock TxByUserRepo userRepo;
    @Mock TxByMerchantRepo merchantRepo;
    @Mock TxByCategoryRepo categoryRepo;
    @Mock UserDailyTotalsRepo dailyRepo;
    @Mock CqlSession session;

    @Test
    void top_merchants_reads_by_period_partition() {
        QueryService svc = new QueryService(userRepo, merchantRepo, categoryRepo, dailyRepo, session);

        PreparedStatement ps = mock(PreparedStatement.class);
        BoundStatement bs = mock(BoundStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Row row1 = mock(Row.class);
        Row row2 = mock(Row.class);

        when(session.prepare(anyString())).thenReturn(ps);
        when(ps.bind("202509")).thenReturn(bs);
        when(session.execute(bs)).thenReturn(rs);
        when(rs.iterator()).thenReturn(List.of(row1, row2).iterator());

        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();
        when(row1.getUuid("merchant_id")).thenReturn(m1);
        when(row1.getLong("tx_count")).thenReturn(5L);
        when(row1.getLong("amount_cents")).thenReturn(1000L);

        when(row2.getUuid("merchant_id")).thenReturn(m2);
        when(row2.getLong("tx_count")).thenReturn(7L);
        when(row2.getLong("amount_cents")).thenReturn(500L);

        List<TopEntryDTO> top = svc.getTopMerchantsByMonth("202509", 2);
        assertEquals(2, top.size());
        assertEquals(m1, top.get(0).getId()); // 1000 > 500, pa je prvi m1
    }
}
