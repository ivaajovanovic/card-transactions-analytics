package rs.ac.uns.acs.nais.columnar.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import rs.ac.uns.acs.nais.columnar.dto.TransactionDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByCategory;
import rs.ac.uns.acs.nais.columnar.model.TxByMerchant;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.repo.*;
import rs.ac.uns.acs.nais.columnar.service.CompensationService;
import rs.ac.uns.acs.nais.columnar.service.TxIngestService;

@RestController
@RequestMapping("/api/tx")
@RequiredArgsConstructor
@Validated
public class TxController {

    private final TxIngestService ingest;
    private final CompensationService compensation;
    private final TxByUserRepo userRepo;
    private final TxByMerchantRepo merchantRepo;
    private final TxByCategoryRepo categoryRepo;

    @PostMapping
    public void create(@Valid @RequestBody TransactionDTO dto) {
        ingest.ingest(dto);
    }

    @DeleteMapping("/users/{userId}/date/{date}/time/{timeuuid}")
    public boolean deleteByUserKey(@PathVariable UUID userId,
                                   @PathVariable LocalDate date,
                                   @PathVariable("timeuuid") UUID timeUuid) {
        return compensation.deleteByUserKey(userId, date, timeUuid);
    }

    @GetMapping("/users/{userId}")
    public List<TxByUser> listUserDay(@PathVariable UUID userId,
                                      @RequestParam LocalDate date,
                                      @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit) {
        return userRepo.findDay(userId, date, limit);
    }

    @GetMapping("/merchants/{merchantId}")
    public List<TxByMerchant> listMerchantDay(@PathVariable UUID merchantId,
                                              @RequestParam LocalDate date,
                                              @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit) {
        return merchantRepo.findDay(merchantId, date, limit);
    }

    @GetMapping("/categories/{categoryId}")
    public List<TxByCategory> listCategoryDay(@PathVariable UUID categoryId,
                                              @RequestParam LocalDate date,
                                              @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit) {
        return categoryRepo.findDay(categoryId, date, limit);
    }
}
