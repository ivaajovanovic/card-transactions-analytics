package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.AnalyticsService;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analytics;

    // ===== USER analytics =====
    @GetMapping("/users/{userId}/spend")
    public List<AggregateAmountDTO> userSpend(@PathVariable String userId,
                                              @RequestParam Instant from,
                                              @RequestParam Instant to,
                                              @RequestParam(defaultValue = "SUCCESS") TransactionStatus status,
                                              @RequestParam(defaultValue = "purpose") String groupBy) {
        return analytics.userSpendByGroup(userId, from, to, status, groupBy);
    }

    @GetMapping("/users/{userId}/top-merchants")
    public List<TopMerchantDTO> topMerchants(@PathVariable String userId,
                                             @RequestParam Instant from,
                                             @RequestParam Instant to,
                                             @RequestParam(defaultValue = "10") long limit) {
        return analytics.topMerchants(userId, from, to, limit);
    }

    @GetMapping("/users/{userId}/channel-mix")
    public List<SpendByGroupDTO> userChannelMix(@PathVariable String userId,
                                                @RequestParam Instant from,
                                                @RequestParam Instant to) {
        return analytics.channelMix(userId, from, to);
    }

    @GetMapping("/users/{userId}/basket-category")
    public List<SpendByGroupDTO> userBasketByCategory(@PathVariable String userId,
                                                      @RequestParam Instant from,
                                                      @RequestParam Instant to) {
        return analytics.basketByCategory(userId, from, to);
    }

    @GetMapping("/users/{userId}/time-of-day")
    public List<SpendByGroupDTO> userTimeOfDay(@PathVariable String userId,
                                               @RequestParam Instant from,
                                               @RequestParam Instant to) {
        return analytics.userTimeOfDay(userId, from, to);
    }

    @GetMapping("/users/{userId}/limit-utilization")
    public SpendByGroupDTO userLimitUtil(@PathVariable String userId,
                                         @RequestParam Instant from,
                                         @RequestParam Instant to) {
        return analytics.userLimitUtilization(userId, from, to);
    }

    // ===== MERCHANT analytics =====
    @GetMapping("/merchants/{merchantId}/spend-by-purpose")
    public List<SpendByGroupDTO> merchantSpendByPurpose(@PathVariable String merchantId,
                                                        @RequestParam Instant from,
                                                        @RequestParam Instant to) {
        return analytics.merchantSpendByPurpose(merchantId, from, to);
    }

    @GetMapping("/merchants/{merchantId}/channel-mix")
    public List<SpendByGroupDTO> merchantChannelMix(@PathVariable String merchantId,
                                                    @RequestParam Instant from,
                                                    @RequestParam Instant to) {
        return analytics.merchantChannelMix(merchantId, from, to);
    }

    @GetMapping("/merchants/{merchantId}/card-network-share")
    public List<SpendByGroupDTO> merchantCardNetworkShare(@PathVariable String merchantId,
                                                          @RequestParam Instant from,
                                                          @RequestParam Instant to) {
        return analytics.merchantCardNetworkShare(merchantId, from, to);
    }

    @GetMapping("/merchants/{merchantId}/failure-reasons")
    public List<SpendByGroupDTO> merchantFailureReasons(@PathVariable String merchantId,
                                                        @RequestParam Instant from,
                                                        @RequestParam Instant to) {
        return analytics.merchantFailureReasons(merchantId, from, to);
    }

    @GetMapping("/merchants/{merchantId}/avg-ticket-over-time")
    public List<AggregateAmountDTO> merchantAvgTicketOverTime(@PathVariable String merchantId,
                                                              @RequestParam Instant from,
                                                              @RequestParam Instant to) {
        return analytics.merchantAvgTicketOverTime(merchantId, from, to);
    }

    @GetMapping("/merchants/{merchantId}/repeat-customers")
    public List<SpendByGroupDTO> merchantRepeat(@PathVariable String merchantId,
                                                @RequestParam Instant from,
                                                @RequestParam Instant to) {
        return analytics.merchantRepeatCustomers(merchantId, from, to);
    }

    @GetMapping("/merchants/{merchantId}/basket-category")
    public List<SpendByGroupDTO> merchantBasketCategory(@PathVariable String merchantId,
                                                        @RequestParam Instant from,
                                                        @RequestParam Instant to) {
        return analytics.merchantBasketByCategory(merchantId, from, to);
    }

    @GetMapping("/merchants/{merchantId}/region-heatmap")
    public List<SpendByGroupDTO> merchantRegionHeatmap(@PathVariable String merchantId,
                                                       @RequestParam Instant from,
                                                       @RequestParam Instant to) {
        return analytics.merchantRegionHeatmap(merchantId, from, to);
    }

    @GetMapping("/merchants/{merchantId}/contactless-share")
    public List<SpendByGroupDTO> contactlessShare(@PathVariable String merchantId,
                                                  @RequestParam Instant from,
                                                  @RequestParam Instant to) {
        return analytics.contactlessShare(merchantId, from, to);
    }

    // ===== ADMIN analytics =====
    @GetMapping("/admin/card-type-failures")
    public List<SpendByGroupDTO> cardTypeFailures(@RequestParam Instant from,
                                                  @RequestParam Instant to) {
        return analytics.cardTypeFailureRates(from, to);
    }

    @GetMapping("/admin/acceptance-coverage")
    public List<SpendByGroupDTO> acceptanceCoverage() {
        return analytics.acceptanceCoverage();
    }

    @GetMapping("/admin/failed-by-merchant")
    public List<SpendByGroupDTO> failedByMerchant(@RequestParam Instant from,
                                                  @RequestParam Instant to,
                                                  @RequestParam(defaultValue = "10") long limit) {
        return analytics.failedByMerchant(from, to, limit);
    }

    @GetMapping("/admin/failure-reasons")
    public List<SpendByGroupDTO> failureReasons(@RequestParam Instant from,
                                                @RequestParam Instant to) {
        return analytics.failureReasons(from, to);
    }

    @GetMapping("/admin/acceptance-gaps")
    public List<SpendByGroupDTO> acceptanceGaps(@RequestParam Instant from,
                                                @RequestParam Instant to,
                                                @RequestParam(defaultValue = "10") long limit) {
        return analytics.acceptanceGaps(from, to, limit);
    }

    // ===== DASHBOARD KPI analytics =====
    @GetMapping("/admin/kpis")
    public AdminKPIResponse getAdminKPIs() {
        return analytics.getAdminKPIs();
    }

    @GetMapping("/admin/segments")
    public List<UserSegmentDTO> getUserSegments() {
        return analytics.getUserSegments();
    }

    @GetMapping("/admin/categories")
    public List<CategoryStatsDTO> getTopCategories() {
        return analytics.getTopCategories();
    }

    @GetMapping("/admin/locations")
    public List<LocationStatsDTO> getTopLocations() {
        return analytics.getTopLocations();
    }

    @GetMapping("/admin/trends")
    public List<TransactionTrendDTO> getTransactionTrends(
        @RequestParam(required = false) Instant fromDate,
        @RequestParam(required = false) Instant toDate
    ) {
        return analytics.getTransactionTrends(fromDate, toDate);
    }
}

