package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.TransactionRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.AnalyticsService;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
    private final TransactionRepository txRepo;

    // USER analytics
    @Override
    public List<AggregateAmountDTO> userSpendByGroup(String userId, Instant from, Instant to, TransactionStatus status, String groupBy) {
        return txRepo.userSpendByGroup(userId, from, to, status.toString(), groupBy);
    }

    @Override
    public List<TopMerchantDTO> topMerchants(String userId, Instant from, Instant to, long limit) {
        return txRepo.topMerchants(userId, from, to, limit);
    }

    @Override
    public List<SpendByGroupDTO> channelMix(String userId, Instant from, Instant to) {
        return txRepo.channelMix(userId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> userTimeOfDay(String userId, Instant from, Instant to) {
        return txRepo.userTimeOfDay(userId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> userLimitUtilizationRaw(String userId, Instant from, Instant to) {
        return txRepo.userLimitUtilizationRaw(userId, from, to);
    }

    @Override
    public SpendByGroupDTO userLimitUtilization(String userId, Instant from, Instant to) {
        var list = userLimitUtilizationRaw(userId, from, to);
        return list.isEmpty() ? new SpendByGroupDTO("limitUtilization", 0.0, 0L) : list.get(0);
    }

    @Override
    public List<SpendByGroupDTO> basketByCategory(String userId, Instant from, Instant to) {
        return txRepo.basketByCategory(userId, from, to);
    }

    // MERCHANT analytics
    @Override
    public List<SpendByGroupDTO> merchantSpendByPurpose(String merchantId, Instant from, Instant to) {
        return txRepo.merchantSpendByPurpose(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantCardNetworkShare(String merchantId, Instant from, Instant to) {
        return txRepo.merchantCardNetworkShare(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantFailureReasons(String merchantId, Instant from, Instant to) {
        return txRepo.merchantFailureReasons(merchantId, from, to);
    }

    @Override
    public List<AggregateAmountDTO> merchantAvgTicketOverTime(String merchantId, Instant from, Instant to) {
        return txRepo.merchantAvgTicketOverTime(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantRepeatCustomers(String merchantId, Instant from, Instant to) {
        return txRepo.merchantRepeatCustomers(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantBasketByCategory(String merchantId, Instant from, Instant to) {
        return txRepo.merchantBasketByCategory(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantRegionHeatmap(String merchantId, Instant from, Instant to) {
        return txRepo.merchantRegionHeatmap(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> merchantChannelMix(String merchantId, Instant from, Instant to) {
        return txRepo.merchantChannelMix(merchantId, from, to);
    }

    @Override
    public List<SpendByGroupDTO> contactlessShare(String merchantId, Instant from, Instant to) {
        return txRepo.contactlessShare(merchantId, from, to);
    }

    // ADMIN analytics
    @Override
    public List<SpendByGroupDTO> cardTypeFailureRates(Instant from, Instant to) {
        return txRepo.cardTypeFailureRates(from, to);
    }

    @Override
    public List<SpendByGroupDTO> acceptanceCoverage() {
        return txRepo.acceptanceCoverage();
    }

    @Override
    public List<SpendByGroupDTO> failedByMerchant(Instant from, Instant to, long limit) {
        return txRepo.failedByMerchant(from, to, limit);
    }

    @Override
    public List<SpendByGroupDTO> failureReasons(Instant from, Instant to) {
        return txRepo.failureReasons(from, to);
    }

    @Override
    public List<SpendByGroupDTO> acceptanceGaps(Instant from, Instant to, long limit) {
        return txRepo.acceptanceGaps(from, to, limit);
    }

    // ===== DASHBOARD KPI analytics =====
    @Override
    public AdminKPIResponse getAdminKPIs() {
        Instant oneMonthAgo = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
        
        Long totalUsers = txRepo.countTotalUsers();
        Long totalMerchants = txRepo.countTotalMerchants();
        Long totalTransactions = txRepo.countTotalTransactions();
        Double totalVolume = txRepo.sumTotalVolume();
        
        Double userGrowth = txRepo.calculateUserGrowth(oneMonthAgo);
        Double merchantGrowth = txRepo.calculateMerchantGrowth(oneMonthAgo);
        Double transactionGrowth = txRepo.calculateTransactionGrowth(oneMonthAgo);
        Double volumeGrowth = txRepo.calculateVolumeGrowth(oneMonthAgo);
        
        return new AdminKPIResponse(
            totalUsers != null ? totalUsers : 0L,
            totalMerchants != null ? totalMerchants : 0L,
            totalTransactions != null ? totalTransactions : 0L,
            totalVolume != null ? totalVolume : 0.0,
            userGrowth != null ? userGrowth * 100 : 0.0,
            merchantGrowth != null ? merchantGrowth * 100 : 0.0,
            transactionGrowth != null ? transactionGrowth * 100 : 0.0,
            volumeGrowth != null ? volumeGrowth * 100 : 0.0
        );
    }

    @Override
    public List<UserSegmentDTO> getUserSegments() {
        List<UserSegmentDTO> segments = txRepo.getUserSegments();
        long total = segments.stream().mapToLong(UserSegmentDTO::getValue).sum();
        if (total > 0) {
            segments.forEach(s -> s.setPercentage((double) s.getValue() / total * 100));
        }
        return segments;
    }

    @Override
    public List<CategoryStatsDTO> getTopCategories() {
        return txRepo.getTopCategories();
    }

    @Override
    public List<LocationStatsDTO> getTopLocations() {
        return txRepo.getTopLocations();
    }

    @Override
    public List<TransactionTrendDTO> getTransactionTrends(Instant fromDate, Instant toDate) {
        if (fromDate == null) {
            fromDate = Instant.now().minus(180, java.time.temporal.ChronoUnit.DAYS);
        }
        if (toDate == null) {
            toDate = Instant.now();
        }
        return txRepo.getTransactionTrends(fromDate, toDate);
    }
}

