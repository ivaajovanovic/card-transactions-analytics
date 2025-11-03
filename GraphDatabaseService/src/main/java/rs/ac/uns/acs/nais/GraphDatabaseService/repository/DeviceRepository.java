package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.DeviceNode;

@Repository
public interface DeviceRepository extends Neo4jRepository<DeviceNode, Long> {
}
