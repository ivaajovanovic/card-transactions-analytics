package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.TerminalNode;

@Repository
public interface TerminalRepository extends Neo4jRepository<TerminalNode, Long> {
}
