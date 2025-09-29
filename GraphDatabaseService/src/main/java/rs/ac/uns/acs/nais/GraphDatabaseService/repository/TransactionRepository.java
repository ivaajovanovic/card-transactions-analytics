package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Transaction;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;

import java.util.List;


@Repository
public interface TransactionRepository extends Neo4jRepository<Transaction, String> {

  // TRANSACTION --PROCESSED_AT--> MERCHANT
  @Query("""
    MATCH (t:Transaction {id:$tid}), (m:Merchant {id:$mid})
    MERGE (t)-[:PROCESSED_AT]->(m)
    """)
  void relateProcessedAt(String tid, String mid);

  // Pronadji transakciju i (opciono) mapiraj trgovca
  @Query("""
    MATCH (t:Transaction {id:$id})
    OPTIONAL MATCH (t)-[:PROCESSED_AT]->(m:Merchant)
    RETURN t, collect(m)
    """)
  Optional<Transaction> fetchWithMerchant(String id);

  @Query("MATCH (t:Transaction {id:$id}) DETACH DELETE t")
  void detachDelete(String id);

  @Query("""
    MATCH (t:Transaction {id:$txId})-[r:PROCESSED_AT]->(:Merchant)
    DELETE r
    """)
  void unsetProcessedAt(String txId);


   // 1) Top merchant-i po korisniku sa POS/ONLINE raspadom
    @Query("""
    MATCH (u:User {id:$userId})-[:OWNS]->(:Card)<-[:MADE_WITH]-(t:Transaction)-[s:SPENT_ON]->(m:Merchant)
    WITH m,
         sum(CASE WHEN s.channel='POS' THEN t.amount ELSE 0 END)    AS posAmt,
         sum(CASE WHEN s.channel='ONLINE' THEN t.amount ELSE 0 END) AS onlineAmt,
         count(*)                                                   AS txCount
    RETURN m.id AS merchantId, m.name AS merchant, txCount AS txCount,
           posAmt AS posAmt, onlineAmt AS onlineAmt, (posAmt+onlineAmt) AS total
    ORDER BY total DESC
    LIMIT $limit
    """)
    List<TopMerchantView> topMerchantsByUser(String userId, long limit);

    // 2) POS, ali cardPresent=false (sumnjivo)
    @Query("""
    MATCH (:User)-[:OWNS]->(c:Card)<-[:MADE_WITH]-(t:Transaction)-[s:SPENT_ON]->(m:Merchant)
    WHERE s.channel='POS' AND coalesce(s.cardPresent,false)=false
    WITH c,m,count(*) AS cnt,sum(t.amount) AS total
    RETURN c.id AS cardId, m.name AS merchant, cnt AS cnt, total AS total
    ORDER BY total DESC
    """)
    List<SuspiciousPosView> suspiciousPos();

    // 3) Potrošnja po kategoriji, dnevno, poslednjih N dana
    @Query("""
    MATCH (u:User {id:$userId})-[:OWNS]->(:Card)<-[:MADE_WITH]-(t:Transaction)-[:SPENT_ON]->(m:Merchant)-[:IN_CATEGORY]->(cat:Category)
    WHERE t.ts >= datetime() - duration({days:$days})
    WITH cat.name AS category, date(t.ts) AS date, sum(t.amount) AS daily
    RETURN category AS category, date AS date, daily AS daily
    ORDER BY category, date
    """)
    List<CategorySpendPoint> categorySpendByDay(String userId, long days);

    // 4) Cross-channel u roku od 7 dana kod istog merchanta (POS pa ONLINE)
    @Query("""
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
    """)
    List<CrossChannelHit> crossChannelWithin7d();

    // 5) Prosečan iznos po CardType + kanalu + kategoriji
    @Query("""
    MATCH (ct:CardType {name:$cardType})<-[:IS_TYPE]-(c:Card)<-[:MADE_WITH]-(t:Transaction)-[s:SPENT_ON]->(:Merchant)-[:IN_CATEGORY]->(cat:Category)
    WITH s.channel AS channel, cat.name AS category, count(*) AS txCount, avg(t.amount) AS avgTicket
    RETURN channel AS channel, category AS category, txCount AS txCount, round(avgTicket,2) AS avgTicket
    ORDER BY channel, category
    """)
    List<CardTypeChannelCategoryAvgView> avgByCardTypeChannelCategory(String cardType);

    // 6) CRUD – obeleži sumnjive SPENT_ON (POS & cardPresent=false)
    @Query("""
    MATCH (t:Transaction)-[s:SPENT_ON]->(:Merchant)
    WHERE s.channel='POS' AND coalesce(s.cardPresent,false)=false
    SET s.suspicious = true, s.flaggedAt = datetime()
    RETURN count(s)
    """)
    long flagSuspiciousPos();

    // 7) CRUD – agregiraj odnos SHOPS_AT (txCount, totalAmount) za sve korisnik–merchant parove
    @Query("""
    MATCH (u:User)-[:OWNS]->(:Card)<-[:MADE_WITH]-(t:Transaction)-[:SPENT_ON]->(m:Merchant)
    WITH u,m,count(*) AS txCount,sum(t.amount) AS totalAmt
    MERGE (u)-[r:SHOPS_AT]->(m)
    SET r.txCount = txCount,
        r.totalAmount = totalAmt,
        r.lastUpdated = datetime()
    RETURN u.id AS userId, m.id AS merchantId, r.txCount AS txCount, r.totalAmount AS totalAmount
    """)
    List<ShopsAtUpsertView> upsertShopEdges();
}
