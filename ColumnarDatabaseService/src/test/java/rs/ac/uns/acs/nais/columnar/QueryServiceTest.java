package rs.ac.uns.acs.nais.columnar;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.repo.TxByUserRepo;
import rs.ac.uns.acs.nais.columnar.service.QueryService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QueryServiceTest {

    @Test
    void getUserTransactionsToday_delegatesToRepo() {
        TxByUserRepo userRepo = mock(TxByUserRepo.class);
        CqlTemplate cql = mock(CqlTemplate.class);

        QueryService qs = new QueryService(userRepo, cql);

        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<TxByUser> expected = Collections.emptyList();

        when(userRepo.findDay(eq(userId), eq(today), anyInt())).thenReturn(expected);

        List<TxByUser> out = qs.getUserTransactionsToday(userId, 10);

        assertEquals(expected, out);
        verify(userRepo).findDay(eq(userId), eq(today), eq(10));
        verifyNoMoreInteractions(cql);
    }
}
