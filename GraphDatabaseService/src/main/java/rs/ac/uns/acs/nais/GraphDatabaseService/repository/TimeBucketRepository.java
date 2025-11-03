package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.TimeBucket;

@Repository
public interface TimeBucketRepository extends Neo4jRepository<TimeBucket, Long> {
}
