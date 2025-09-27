package rs.ac.uns.acs.nais.columnar.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import rs.ac.uns.acs.nais.columnar.dto.TopEntryDTO;
import rs.ac.uns.acs.nais.columnar.dto.UserDayTotalsDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.service.QueryService;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportsController {

    private final QueryService qs;

    @GetMapping("/users/{userId}/transactions/today")
    public List<TxByUser> userToday(@PathVariable UUID userId,
                                    @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit) {
        return qs.getUserTransactionsToday(userId, limit);
    }

    @GetMapping("/top-merchants")
    public List<TopEntryDTO> topMerchants(@RequestParam String month,    // YYYYMM
                                          @RequestParam(defaultValue = "5") @Min(1) @Max(1000) int limit) {
        return qs.getTopMerchantsByMonth(month, limit);
    }

    @GetMapping("/top-categories")
    public List<TopEntryDTO> topCategories(@RequestParam String month,   // YYYYMM
                                           @RequestParam(defaultValue = "5") @Min(1) @Max(1000) int limit) {
        return qs.getTopCategoriesByMonth(month, limit);
    }

    @GetMapping("/categories/{categoryId}/top-merchants")
    public List<TopEntryDTO> topMerchantsForCategory(@PathVariable UUID categoryId,
                                                     @RequestParam LocalDate date,
                                                     @RequestParam(defaultValue = "5") @Min(1) @Max(1000) int limit) {
        return qs.getMostFrequentMerchantsForCategory(categoryId, date, limit);
    }

    @GetMapping("/users/{userId}/daily-totals")
    public List<UserDayTotalsDTO> userDailyTotals(@PathVariable UUID userId,
                                                  @RequestParam LocalDate from,
                                                  @RequestParam LocalDate to) {
        return qs.getUserDailyTotals(userId, from, to);
    }

    @GetMapping("/users/{userId}/avg-spend")
    public Map<String, Object> avgSpend(@PathVariable UUID userId,
                                        @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return qs.getAverageSpendingForUser(userId, days);
    }
}
