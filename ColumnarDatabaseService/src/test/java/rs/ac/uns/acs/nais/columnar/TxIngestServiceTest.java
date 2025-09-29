package rs.ac.uns.acs.nais.columnar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.model.UserDailyTotal;
import rs.ac.uns.acs.nais.columnar.repo.*;
import rs.ac.uns.acs.nais.columnar.service.DedupService;
import rs.ac.uns.acs.nais.columnar.service.TxIngestService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TxIngestServiceTest {

    @Mock TxByUserRepo userRepo;
    @Mock TxByMerchantRepo merchantRepo;
    @Mock TxByCategoryRepo categoryRepo;
    @Mock UserDailyTotalsRepo dailyRepo;
    @Mock CqlTemplate cql;
    @Mock Supplier<UUID> timeUuidSupplier;
    @Mock DedupService dedupService;

    @Test
    void ingest_writes_denorm_and_aggregates() {
        var svc = new TxIngestService(
            userRepo, merchantRepo, categoryRepo, dailyRepo,
            cql, timeUuidSupplier, dedupService
        );

        UUID timeuuid = UUID.randomUUID();
        when(timeUuidSupplier.get()).thenReturn(timeuuid);

        UUID user = UUID.randomUUID();
        UUID merch = UUID.randomUUID();
        UUID cat = UUID.randomUUID();
        UUID card = UUID.randomUUID();
        UUID txid = UUID.randomUUID();

        TransactionDTO dto = new TransactionDTO();
        dto.setTxId(txid);
        dto.setUserId(user);
        dto.setMerchantId(merch);
        dto.setCategoryId(cat);
        dto.setCardId(card);
        dto.setAmountCents(1234L);
        dto.setCurrency("EUR");
        dto.setOccurredAt(Instant.parse("2025-09-01T12:00:00Z"));
        dto.setStatus("APPROVED");

        // Ako DedupService blokira ingest osim ako eksplicitno dozvoli, odkomentariši odgovarajuću liniju:
        // when(dedupService.shouldIngest(txid)).thenReturn(true);
        // ili, ako je API obrnut:
        // when(dedupService.isDuplicate(txid)).thenReturn(false);

        when(dailyRepo.findOneDay(eq(user), any()))
            .thenReturn(Optional.of(new UserDailyTotal(user, null, 0, 0L)));

        svc.ingest(dto);

        verify(userRepo, times(1)).save(any());
        verify(merchantRepo, times(1)).save(any());
        verify(categoryRepo, times(1)).save(any());
        verify(dailyRepo, times(1)).save(any());
        verify(cql, atLeast(4)).execute(anyString(), any(), any(), any());
    }
}
