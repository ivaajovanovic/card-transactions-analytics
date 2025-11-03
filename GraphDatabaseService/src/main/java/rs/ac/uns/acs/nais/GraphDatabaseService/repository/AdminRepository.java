package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.AdminNode;

@Repository
public interface AdminRepository extends Neo4jRepository<AdminNode, Long> {
    Optional<AdminNode> findByEmail(String email);

    @Query("MATCH (a:Admin) WHERE a.email = $email AND a.password = $password RETURN a ORDER BY id(a) ASC LIMIT 1")
    Optional<AdminNode> findByEmailAndPassword(@Param("email") String email, @Param("password") String password);
}
