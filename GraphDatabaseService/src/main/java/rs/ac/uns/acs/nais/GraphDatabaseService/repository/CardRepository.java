package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Card;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Channel;

@Repository
public interface CardRepository extends Neo4jRepository<Card, String> {

  // CARD --SPENT_ON {channel, cardPresent}--> TRANSACTION
  @Query("""
    MATCH (c:Card {id:$cid}), (t:Transaction {id:$tid})
    MERGE (c)-[s:SPENT_ON]->(t)
    SET s.channel = $channel, s.cardPresent = $present
    """)
  void upsertSpentOn(String cid, String tid, Channel channel, Boolean present);

  @Query("""
    MATCH (c:Card {id:$cid})-[s:SPENT_ON]->(t:Transaction {id:$tid})
    DELETE s
    """)
  void removeSpentOn(String cid, String tid);

  // Učitaj karticu sa SPENT_ON relacijama + transakcijama (+ trgovcima)
  @Query("""
    MATCH (c:Card {id:$id})
    OPTIONAL MATCH (c)-[s:SPENT_ON]->(t:Transaction)-[:PROCESSED_AT]->(m:Merchant)
    RETURN c, collect(s), collect(t), collect(m)
    """)
  Optional<Card> fetchWithSpends(String id);

  @Query("MATCH (c:Card {id:$id}) DETACH DELETE c")
  void detachDelete(String id);
}
