package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Category;

@Repository
public interface CategoryRepository extends Neo4jRepository<Category, String> {

  @Query("MATCH (c:Category {id:$id}) DETACH DELETE c")
  void detachDelete(String id);
}
