package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.ErrorResponse;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.TransactionService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService txService;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingest(@RequestBody @Validated IngestRequest req) {
        try {
            txService.ensureUserAndCard(req.getUser(), req.getCard());
            txService.ensureMerchantAndCategory(req.getMerchant(), req.getCategory(), req.getRegion());
            txService.addTransaction(req.getCard().getPanHash(), req.getMerchant().getMerchantId(), req.getTxn());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/limit-check/{panHash}")
    public ResponseEntity<?> limitCheck(@PathVariable String panHash,
                                        @RequestParam Instant from,
                                        @RequestParam Instant to,
                                        @RequestParam double incomingAmount) {
        boolean ok = txService.checkCardLimit(panHash, from, to, incomingAmount);
        return ResponseEntity.ok(ok);
    }

    @Data
    public static class IngestRequest {
        private UserNode user;
        private CardNode card;
        private MerchantNode merchant;
        private CategoryNode category;
        private RegionNode region;
        private TransactionRel txn;
    }
}
