package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.*;
import java.time.Instant;

public interface TransactionService {
    void ensureUserAndCard(UserNode user, CardNode card);
    void ensureMerchantAndCategory(MerchantNode merchant, CategoryNode category, RegionNode region);
    void addTransaction(String panHash, String merchantId, TransactionRel txn);
    boolean checkCardLimit(String panHash, Instant from, Instant to, double incomingAmount);
}
