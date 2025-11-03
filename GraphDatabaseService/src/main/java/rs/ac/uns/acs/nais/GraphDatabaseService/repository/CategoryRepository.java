package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.CategoryNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends Neo4jRepository<CategoryNode, Long> {
    Optional<CategoryNode> findByCode(String code);
}
