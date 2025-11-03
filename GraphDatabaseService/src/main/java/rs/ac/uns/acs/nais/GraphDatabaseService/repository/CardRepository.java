package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.CardNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CardRepository extends Neo4jRepository<CardNode, Long> {
    Optional<CardNode> findByPanHash(String panHash);
}
