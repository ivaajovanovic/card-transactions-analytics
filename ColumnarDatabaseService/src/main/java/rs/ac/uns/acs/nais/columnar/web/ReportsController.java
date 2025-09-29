package rs.ac.uns.acs.nais.columnar.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import rs.ac.uns.acs.nais.columnar.dto.ReportRequestDTO;
import rs.ac.uns.acs.nais.columnar.dto.ReportResponseDTO;
import org.springframework.web.bind.annotation.RestController;

import rs.ac.uns.acs.nais.columnar.dto.TopEntryDTO;
import rs.ac.uns.acs.nais.columnar.dto.UserDayTotalsDTO;
import rs.ac.uns.acs.nais.columnar.model.TxByUser;
import rs.ac.uns.acs.nais.columnar.model.TxByMerchant;
import rs.ac.uns.acs.nais.columnar.model.TxByCategory;
import rs.ac.uns.acs.nais.columnar.repo.TxByUserRepo;
import rs.ac.uns.acs.nais.columnar.repo.TxByMerchantRepo;
import rs.ac.uns.acs.nais.columnar.repo.TxByCategoryRepo;
import rs.ac.uns.acs.nais.columnar.service.QueryService;
import rs.ac.uns.acs.nais.columnar.service.ReportGeneratorService;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportsController {

    private final QueryService qs;
    private final ReportGeneratorService reportService;
    private final TxByUserRepo userRepo;
    private final TxByMerchantRepo merchantRepo;
    private final TxByCategoryRepo categoryRepo;

    // --- REPORT GENERATOR ---
    
    @PostMapping("/generate")
    public ReportResponseDTO generateReport(@RequestBody ReportRequestDTO request) {
        return reportService.generateReport(request);
    }

    // --- USER ---

    @GetMapping("/users/{userId}/transactions/today")
    public List<TxByUser> userToday(@PathVariable UUID userId,
                                    @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit) {
        return qs.getUserTransactionsToday(userId, limit);
    }

    @GetMapping("/users/{userId}/transactions/day")
    public List<TxByUser> userDay(@PathVariable UUID userId,
                                  @RequestParam LocalDate date,
                                  @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit,
                                  @RequestParam(required = false) UUID before) {
        return (before != null)
                ? userRepo.findDayBefore(userId, date, before, limit)
                : userRepo.findDay(userId, date, limit);
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

    // --- TOP ---

    @GetMapping("/top-merchants")
    public List<TopEntryDTO> topMerchants(@RequestParam String month, // YYYYMM
                                          @RequestParam(defaultValue = "5") @Min(1) @Max(1000) int limit) {
        return qs.getTopMerchantsByMonth(month, limit);
    }

    @GetMapping("/top-categories")
    public List<TopEntryDTO> topCategories(@RequestParam String month, // YYYYMM
                                           @RequestParam(defaultValue = "5") @Min(1) @Max(1000) int limit) {
        return qs.getTopCategoriesByMonth(month, limit);
    }

    @GetMapping("/categories/{categoryId}/top-merchants")
    public List<TopEntryDTO> topMerchantsForCategory(@PathVariable UUID categoryId,
                                                     @RequestParam LocalDate date,
                                                     @RequestParam(defaultValue = "5") @Min(1) @Max(1000) int limit) {
        return qs.getMostFrequentMerchantsForCategory(categoryId, date, limit);
    }


    @GetMapping("/merchants/{merchantId}/transactions/day")
    public List<TxByMerchant> merchantDay(@PathVariable UUID merchantId,
                                          @RequestParam LocalDate date,
                                          @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit,
                                          @RequestParam(required = false) UUID before) {
        return (before != null)
                ? merchantRepo.findDayBefore(merchantId, date, before, limit)
                : merchantRepo.findDay(merchantId, date, limit);
    }

    @GetMapping("/categories/{categoryId}/transactions/day")
    public List<TxByCategory> categoryDay(@PathVariable UUID categoryId,
                                          @RequestParam LocalDate date,
                                          @RequestParam(defaultValue = "100") @Min(1) @Max(10000) int limit,
                                          @RequestParam(required = false) UUID before) {
        return (before != null)
                ? categoryRepo.findDayBefore(categoryId, date, before, limit)
                : categoryRepo.findDay(categoryId, date, limit);
    }

    // --- ADVANCED REPORTS (New Integration Features) ---

    @GetMapping("/analytics/category-report")
    public ReportResponseDTO getCategoryReport(
            @RequestParam UUID categoryId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        ReportRequestDTO request = ReportRequestDTO.builder()
                .reportType("CATEGORY")
                .startDate(fromDate)
                .endDate(toDate)
                .build();
        return reportService.generateReport(request);
    }

    @GetMapping("/analytics/user-activity")
    public ReportResponseDTO getUserActivityReport(
            @RequestParam UUID userId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        ReportRequestDTO request = ReportRequestDTO.builder()
                .reportType("USER_ACTIVITY")
                .startDate(fromDate)
                .endDate(toDate)
                .build();
        return reportService.generateReport(request);
    }

    @GetMapping("/analytics/complex-daily")
    public ReportResponseDTO getComplexDailyReport(
            @RequestParam LocalDate date) {
        ReportRequestDTO request = ReportRequestDTO.builder()
                .reportType("COMPLEX_DAILY")
                .startDate(date)
                .endDate(date)
                .build();
        return reportService.generateReport(request);
    }
}
