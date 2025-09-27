package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.User;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

  // USER --OWNS--> CARD
  @Query("""
    MATCH (u:User {id:$uid}), (c:Card {id:$cid})
    MERGE (u)-[:OWNS]->(c)
    """)
  void relateOwns(String uid, String cid);

  // Učitaj user-a sa karticama (da bi SDN mape radio, vraćamo i collect(c))
  @Query("""
    MATCH (u:User {id:$id})
    OPTIONAL MATCH (u)-[:OWNS]->(c:Card)
    RETURN u, collect(c)
    """)
  Optional<User> fetchWithCards(String id);

  // ——— Kompleksni upiti (MATCH+WHERE+WITH+agregacija) ———

  // Potrošnja po kategorijama za datog user-a (period + minimalni iznos)
  @Query("""
    MATCH (u:User {id:$uid})-[:OWNS]->(c:Card)
      -[:SPENT_ON]->(t:Transaction)-[:PROCESSED_AT]->(m:Merchant)
      -[:BELONGS_TO]->(cat:Category)
    WHERE t.amount >= $minAmount
      AND t.date >= datetime($fromIso)
      AND t.date <  datetime($toIso)
    WITH cat, sum(t.amount) AS total
    RETURN cat.id AS categoryId, cat.name AS categoryName, total
    ORDER BY total DESC
    LIMIT $limit
    """)
  List<CategorySpendAgg> categorySpend(String uid, Double minAmount,
                                       String fromIso, String toIso, int limit);

  interface CategorySpendAgg {
    String getCategoryId();
    String getCategoryName();
    Double getTotal();
  }

  // "Sumnjivi" trgovci po učestalosti/iznosu za user-a, opcioni filter na grad
  @Query("""
    MATCH (u:User {id:$uid})-[:OWNS]->(:Card)-[:SPENT_ON]->(t:Transaction)
      -[:PROCESSED_AT]->(m:Merchant)
    WHERE t.amount > $minAmount AND ($city IS NULL OR m.location = $city)
    WITH m, count(t) AS hits, sum(t.amount) AS total
    RETURN m.id AS merchantId, m.name AS merchantName, hits, total
    ORDER BY hits DESC
    LIMIT $limit
    """)
  List<MerchantHitsAgg> suspiciousMerchants(String uid, Double minAmount,
                                            String city, int limit);

  interface MerchantHitsAgg {
    String getMerchantId();
    String getMerchantName();
    Long getHits();
    Double getTotal();
  }

  // Bezbedno brisanje sa svim vezama
  @Query("MATCH (u:User {id:$id}) DETACH DELETE u")
  void detachDelete(String id);


  @Query("""
    MATCH (u:User {id:$userId})-[r:OWNS]->(c:Card {id:$cardId})
    DELETE r
    """)
  void unlinkOwns(String userId, String cardId);
}
