package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final Neo4jClient neo4j;
    
    public List<TopMerchantView> getTopMerchantsByUser(String userId, long limit) {
        String query = """
            MATCH (u:User {id:$userId})-[:OWNS]->(:Card)<-[:MADE_WITH]-(t:Transaction)-[s:SPENT_ON]->(m:Merchant)
            WITH m,
                 sum(CASE WHEN s.channel='POS' THEN t.amount ELSE 0 END)    AS posAmt,
                 sum(CASE WHEN s.channel='ONLINE' THEN t.amount ELSE 0 END) AS onlineAmt,
                 count(*)                                                   AS txCount
            RETURN m.id AS merchantId, m.name AS merchant, txCount AS txCount,
                   posAmt AS posAmt, onlineAmt AS onlineAmt, (posAmt+onlineAmt) AS total
            ORDER BY total DESC
            LIMIT $limit
            """;
            
        return neo4j.query(query)
                .bindAll(Map.of("userId", userId, "limit", limit))
                .fetchAs(TopMerchantView.class)
                .mappedBy((typeSystem, record) -> 
                    new TopMerchantView(
                        record.get("merchantId").asString(),
                        record.get("merchant").asString(),
                        record.get("txCount").asLong(),
                        record.get("posAmt").asDouble(),
                        record.get("onlineAmt").asDouble(),
                        record.get("total").asDouble()
                    )
                )
                .all()
                .stream()
                .toList();
    }
    
    public List<SuspiciousPosView> getSuspiciousPos() {
        String query = """
            MATCH (:User)-[:OWNS]->(c:Card)<-[:MADE_WITH]-(t:Transaction)-[s:SPENT_ON]->(m:Merchant)
            WHERE s.channel='POS' AND coalesce(s.cardPresent,false)=false
            WITH c,m,count(*) AS cnt,sum(t.amount) AS total
            RETURN c.id AS cardId, m.name AS merchant, cnt AS cnt, total AS total
            ORDER BY total DESC
            """;
            
        return neo4j.query(query)
                .fetchAs(SuspiciousPosView.class)
                .mappedBy((typeSystem, record) -> 
                    new SuspiciousPosView(
                        record.get("cardId").asString(),
                        record.get("merchant").asString(),
                        record.get("cnt").asLong(),
                        record.get("total").asDouble()
                    )
                )
                .all()
                .stream()
                .toList();
    }
    
    public List<CategorySpendPoint> getCategorySpendByDay(String userId, long days) {
        String query = """
            MATCH (u:User {id:$userId})-[:OWNS]->(:Card)<-[:MADE_WITH]-(t:Transaction)-[:SPENT_ON]->(m:Merchant)-[:IN_CATEGORY]->(cat:Category)
            WHERE t.ts >= datetime() - duration({days:$days})
            WITH cat.name AS category, date(t.ts) AS date, sum(t.amount) AS daily
            RETURN category AS category, date AS date, daily AS daily
            ORDER BY category, date
            """;
            
        return neo4j.query(query)
                .bindAll(Map.of("userId", userId, "days", days))
                .fetchAs(CategorySpendPoint.class)
                .mappedBy((typeSystem, record) -> 
                    new CategorySpendPoint(
                        record.get("category").asString(),
                        record.get("date").asLocalDate(),
                        record.get("daily").asDouble()
                    )
                )
                .all()
                .stream()
                .toList();
    }
    
    public List<CrossChannelHit> getCrossChannelWithin7d() {
        String query = """
            MATCH (u:User)-[:OWNS]->(:Card)<-[:MADE_WITH]-(t1:Transaction)-[s1:SPENT_ON]->(m:Merchant)
            WHERE s1.channel='POS'
            WITH u, m, collect({ts:t1.ts}) AS posTs
            MATCH (u)-[:OWNS]->(:Card)<-[:MADE_WITH]-(t2:Transaction)-[s2:SPENT_ON]->(m)
            WHERE s2.channel='ONLINE'
            WITH u, m, posTs, collect(t2.ts) AS onTs
            WITH u, m, [p IN posTs WHERE any(o IN onTs WHERE o>=p.ts AND o<=p.ts+duration('P7D'))] AS cross
            WHERE size(cross) > 0
            RETURN u.name AS user, m.name AS merchant, size(cross) AS occurrences
            ORDER BY occurrences DESC
            """;
            
        return neo4j.query(query)
                .fetchAs(CrossChannelHit.class)
                .mappedBy((typeSystem, record) -> 
                    new CrossChannelHit(
                        record.get("user").asString(),
                        record.get("merchant").asString(),
                        record.get("occurrences").asLong()
                    )
                )
                .all()
                .stream()
                .toList();
    }
    
    public List<CardTypeChannelCategoryAvgView> getAvgByCardTypeChannelCategory(String cardType) {
        String query = """
            MATCH (ct:CardType {name:$cardType})<-[:IS_TYPE]-(c:Card)<-[:MADE_WITH]-(t:Transaction)-[s:SPENT_ON]->(:Merchant)-[:IN_CATEGORY]->(cat:Category)
            WITH s.channel AS channel, cat.name AS category, count(*) AS txCount, avg(t.amount) AS avgTicket
            RETURN channel AS channel, category AS category, txCount AS txCount, round(avgTicket,2) AS avgTicket
            ORDER BY channel, category
            """;
            
        return neo4j.query(query)
                .bindAll(Map.of("cardType", cardType))
                .fetchAs(CardTypeChannelCategoryAvgView.class)
                .mappedBy((typeSystem, record) -> 
                    new CardTypeChannelCategoryAvgView(
                        record.get("channel").asString(),
                        record.get("category").asString(),
                        record.get("txCount").asLong(),
                        record.get("avgTicket").asDouble()
                    )
                )
                .all()
                .stream()
                .toList();
    }
    
    public long flagSuspiciousPos() {
        String query = """
            MATCH (t:Transaction)-[s:SPENT_ON]->(:Merchant)
            WHERE s.channel='POS' AND coalesce(s.cardPresent,false)=false
            SET s.suspicious = true, s.flaggedAt = datetime()
            RETURN count(s)
            """;
            
        return neo4j.query(query)
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }
    
    public List<ShopsAtUpsertView> upsertShopEdges() {
        String query = """
            MATCH (u:User)-[:OWNS]->(:Card)<-[:MADE_WITH]-(t:Transaction)-[:SPENT_ON]->(m:Merchant)
            WITH u,m,count(*) AS txCount,sum(t.amount) AS totalAmt
            MERGE (u)-[r:SHOPS_AT]->(m)
            SET r.txCount = txCount,
                r.totalAmount = totalAmt,
                r.lastUpdated = datetime()
            RETURN u.id AS userId, m.id AS merchantId, r.txCount AS txCount, r.totalAmount AS totalAmount
            """;
            
        return neo4j.query(query)
                .fetchAs(ShopsAtUpsertView.class)
                .mappedBy((typeSystem, record) -> 
                    new ShopsAtUpsertView(
                        record.get("userId").asString(),
                        record.get("merchantId").asString(),
                        record.get("txCount").asLong(),
                        record.get("totalAmount").asDouble()
                    )
                )
                .all()
                .stream()
                .toList();
    }
}