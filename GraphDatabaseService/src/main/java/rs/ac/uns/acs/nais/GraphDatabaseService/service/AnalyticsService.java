package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.TransactionStatus;
import java.time.Instant;
import java.util.List;

public interface AnalyticsService {
    // USER analytics
    List<AggregateAmountDTO> userSpendByGroup(String userId, Instant from, Instant to, TransactionStatus status, String groupBy);
    List<TopMerchantDTO> topMerchants(String userId, Instant from, Instant to, long limit);
    List<SpendByGroupDTO> channelMix(String userId, Instant from, Instant to);
    List<SpendByGroupDTO> userTimeOfDay(String userId, Instant from, Instant to);
    List<SpendByGroupDTO> userLimitUtilizationRaw(String userId, Instant from, Instant to);
    SpendByGroupDTO userLimitUtilization(String userId, Instant from, Instant to);
    List<SpendByGroupDTO> basketByCategory(String userId, Instant from, Instant to);

    // MERCHANT analytics
    List<SpendByGroupDTO> merchantSpendByPurpose(String merchantId, Instant from, Instant to);
    List<SpendByGroupDTO> merchantCardNetworkShare(String merchantId, Instant from, Instant to);
    List<SpendByGroupDTO> merchantFailureReasons(String merchantId, Instant from, Instant to);
    List<AggregateAmountDTO> merchantAvgTicketOverTime(String merchantId, Instant from, Instant to);
    List<SpendByGroupDTO> merchantRepeatCustomers(String merchantId, Instant from, Instant to);
    List<SpendByGroupDTO> merchantBasketByCategory(String merchantId, Instant from, Instant to);
    List<SpendByGroupDTO> merchantRegionHeatmap(String merchantId, Instant from, Instant to);
    List<SpendByGroupDTO> merchantChannelMix(String merchantId, Instant from, Instant to);
    List<SpendByGroupDTO> contactlessShare(String merchantId, Instant from, Instant to);

    // ADMIN analytics
    List<SpendByGroupDTO> cardTypeFailureRates(Instant from, Instant to);
    List<SpendByGroupDTO> acceptanceCoverage();
    List<SpendByGroupDTO> failedByMerchant(Instant from, Instant to, long limit);
    List<SpendByGroupDTO> failureReasons(Instant from, Instant to);
    List<SpendByGroupDTO> acceptanceGaps(Instant from, Instant to, long limit);

    // DASHBOARD KPI analytics
    AdminKPIResponse getAdminKPIs();
    List<UserSegmentDTO> getUserSegments();
    List<CategoryStatsDTO> getTopCategories();
    List<LocationStatsDTO> getTopLocations();
    List<TransactionTrendDTO> getTransactionTrends(Instant fromDate, Instant toDate);
}

