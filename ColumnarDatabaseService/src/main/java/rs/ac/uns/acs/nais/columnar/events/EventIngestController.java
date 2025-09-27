package rs.ac.uns.acs.nais.columnar.events;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.service.TxIngestService;

/**
 * Minimalni REST ulaz za event-driven sync (bez Kafke).
 * Drugi servisi mogu POST-ovati transaction-created event ovde.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventIngestController {

    private final TxIngestService ingest;

    @PostMapping("/transaction-created")
    public void onTransactionCreated(@Valid @RequestBody TransactionDTO dto) {
        ingest.ingest(dto);
    }
}
