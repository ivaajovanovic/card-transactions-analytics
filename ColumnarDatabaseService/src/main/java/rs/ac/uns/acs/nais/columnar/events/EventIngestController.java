package rs.ac.uns.acs.nais.columnar.events;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.service.TxIngestService;

import jakarta.validation.Valid; // <— važno: JAKARTA, ne javax
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventIngestController {

    private final TxIngestService txIngestService;

    @PostMapping("/transactions")
    public ResponseEntity<Void> ingestOne(@Valid @RequestBody TransactionDTO dto) {
        txIngestService.ingest(dto);
        return ResponseEntity.accepted().build(); // 202
    }

    @PostMapping("/transactions/_bulk")
    public ResponseEntity<Map<String, Object>> ingestBulk(@RequestBody List<@Valid TransactionDTO> items) {
        int total = items == null ? 0 : items.size();
        int ok = 0;
        List<String> errors = new ArrayList<>();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                try {
                    txIngestService.ingest(items.get(i));
                    ok++;
                } catch (Exception e) {
                    errors.add("idx " + i + ": " + e.getMessage());
                }
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("total", total);
        body.put("success", ok);
        body.put("failed", total - ok);
        if (!errors.isEmpty()) body.put("errors", errors);

        HttpStatus status = errors.isEmpty() ? HttpStatus.ACCEPTED : HttpStatus.MULTI_STATUS; // 202 ili 207
        return new ResponseEntity<>(body, status);
    }
}
