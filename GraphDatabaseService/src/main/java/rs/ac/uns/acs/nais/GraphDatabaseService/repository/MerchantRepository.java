package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Merchant;

@Repository
public interface MerchantRepository extends Neo4jRepository<Merchant, String> {

  // MERCHANT --BELONGS_TO--> CATEGORY
  @Query("""
    MATCH (m:Merchant {id:$mid}), (cat:Category {id:$cid})
    MERGE (m)-[:BELONGS_TO]->(cat)
    """)
  void relateToCategory(String mid, String cid);

  // Merchants by category
  @Query("""
    MATCH (m:Merchant)-[:BELONGS_TO]->(cat:Category {id:$categoryId})
    RETURN m
    """)
  List<Merchant> findByCategory(String categoryId);

  @Query("MATCH (m:Merchant {id:$id}) DETACH DELETE m")
  void detachDelete(String id);

  @Query("""
    MATCH (m:Merchant {id:$merchantId})-[r:BELONGS_TO]->(:Category)
    DELETE r
    """)
  void unsetCategory(String merchantId);
}
