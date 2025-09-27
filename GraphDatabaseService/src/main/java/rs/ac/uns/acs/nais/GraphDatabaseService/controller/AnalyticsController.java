package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.AnalyticsService;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-merchants")
    public List<TopMerchantView> topMerchants(@RequestParam String userId,
                                              @RequestParam(defaultValue = "5") long limit) {
        return analyticsService.getTopMerchantsByUser(userId, limit);
    }

    @GetMapping("/suspicious/pos")
    public List<SuspiciousPosView> suspiciousPos() {
        return analyticsService.getSuspiciousPos();
    }

    @GetMapping("/category-daily")
    public List<CategorySpendPoint> categoryDaily(@RequestParam String userId,
                                                  @RequestParam(defaultValue = "30") long days) {
        return analyticsService.getCategorySpendByDay(userId, days);
    }

    @GetMapping("/cross-channel")
    public List<CrossChannelHit> crossChannel() {
        return analyticsService.getCrossChannelWithin7d();
    }

    @GetMapping("/avg-ticket")
    public List<CardTypeChannelCategoryAvgView> avgTicket(@RequestParam String cardType) {
        return analyticsService.getAvgByCardTypeChannelCategory(cardType);
    }

    @PostMapping("/flag-pos-suspicious")
    public long flagPosSuspicious() {
        return analyticsService.flagSuspiciousPos();
    }

    @PostMapping("/aggregate/shops-at")
    public List<ShopsAtUpsertView> aggregateShopsAt() {
        return analyticsService.upsertShopEdges();
    }
}
