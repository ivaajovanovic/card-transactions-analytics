package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final UserRepository userRepo;
    private final CardRepository cardRepo;
    private final MerchantRepository merchantRepo;
    private final CategoryRepository categoryRepo;
    private final RegionRepository regionRepo;
    private final Neo4jClient neo4jClient;

    @Override
    @Transactional
    public void ensureUserAndCard(UserNode user, CardNode card) {
        UserNode u = userRepo.findByExternalId(user.getExternalId())
                .orElseGet(() -> userRepo.save(user));
        card.setOwner(u);
        cardRepo.findByPanHash(card.getPanHash())
                .orElseGet(() -> cardRepo.save(card));
    }

    @Override
    @Transactional
    public void ensureMerchantAndCategory(MerchantNode merchant, CategoryNode category, RegionNode region) {
        CategoryNode cat = null;
        if (category != null) {
            cat = categoryRepo.findByCode(category.getCode())
                    .orElseGet(() -> categoryRepo.save(category));
        }
        
        RegionNode reg = null;
        if (region != null) {
            reg = regionRepo.save(region);
        }
        
        MerchantNode m = merchantRepo.findByMerchantId(merchant.getMerchantId())
                .orElse(null);
        if (m == null) {
            merchant.setCategory(cat);
            merchant.setRegion(reg);
            merchantRepo.save(merchant);
        } else {
            m.setCategory(cat);
            m.setRegion(reg);
            merchantRepo.save(m);
        }
    }

    @Override
    @Transactional
    public void addTransaction(String panHash, String merchantId, TransactionRel txn) {
    cardRepo.findByPanHash(panHash)
        .orElseThrow(() -> new IllegalArgumentException("Card not found: " + panHash));
    merchantRepo.findByMerchantId(merchantId)
        .orElseThrow(() -> new IllegalArgumentException("Merchant not found: " + merchantId));
        
        // Create relationship using Cypher query via Neo4jClient
        String cypher = """
            MATCH (c:Card {panHash: $panHash}), (m:Merchant {merchantId: $merchantId})
            CREATE (c)-[t:TRANSACTED_WITH {
                timestamp: $timestamp,
                amount: $amount,
                currency: $currency,
                originalAmount: $originalAmount,
                originalCurrency: $originalCurrency,
                fxRate: $fxRate,
                paymentType: $paymentType,
                purpose: $purpose,
                status: $status,
                declineReason: $declineReason,
                authCode: $authCode,
                channel: $channel,
                contactless: $contactless
            }]->(m)
            RETURN id(t)
            """;
        
        neo4jClient.query(cypher)
                .bind(panHash).to("panHash")
                .bind(merchantId).to("merchantId")
                .bind(txn.getTimestamp()).to("timestamp")
                .bind(txn.getAmount()).to("amount")
                .bind(txn.getCurrency() != null ? txn.getCurrency().toString() : null).to("currency")
                .bind(txn.getOriginalAmount()).to("originalAmount")
                .bind(txn.getOriginalCurrency() != null ? txn.getOriginalCurrency().toString() : null).to("originalCurrency")
                .bind(txn.getFxRate()).to("fxRate")
                .bind(txn.getPaymentType() != null ? txn.getPaymentType().toString() : null).to("paymentType")
                .bind(txn.getPurpose() != null ? txn.getPurpose().toString() : null).to("purpose")
                .bind(txn.getStatus() != null ? txn.getStatus().toString() : null).to("status")
                .bind(txn.getDeclineReason() != null ? txn.getDeclineReason().toString() : null).to("declineReason")
                .bind(txn.getAuthCode()).to("authCode")
                .bind(txn.getChannel() != null ? txn.getChannel().toString() : null).to("channel")
                .bind(txn.getContactless()).to("contactless")
                .run();
    }

    @Override
    public boolean checkCardLimit(String panHash, Instant from, Instant to, double incomingAmount) {
        var card = cardRepo.findByPanHash(panHash).orElse(null);
        if (card == null || card.getMonthlyLimit() == null) {
            return true;
        }
        
        String cypher = """
            MATCH (c:Card {panHash: $panHash})-[t:TRANSACTED_WITH]->()
            WHERE t.timestamp >= $from AND t.timestamp < $to AND toString(t.status) = 'SUCCESS'
            RETURN sum(t.amount) AS totalSpent
            """;
        
        Double totalSpent = neo4jClient.query(cypher)
                .bind(panHash).to("panHash")
                .bind(from).to("from")
                .bind(to).to("to")
                .fetchAs(Double.class)
                .one()
                .orElse(0.0);
        
        return (totalSpent + incomingAmount) <= card.getMonthlyLimit();
    }
}
