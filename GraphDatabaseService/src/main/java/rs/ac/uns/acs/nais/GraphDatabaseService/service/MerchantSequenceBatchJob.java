package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;

@Service
public class MerchantSequenceBatchJob {
    @Autowired
    private Neo4jClient neo4jClient;

    // Run daily to update merchant sequence relationships
    @Scheduled(cron = "0 0 3 * * *")
    public void buildMerchantSequences() {
        String cypher = """
        MATCH (u:User)<-[:OWNS]-(c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
        WITH u, c, t, m
        ORDER BY u.externalId, c.panHash, t.timestamp
        WITH u, collect(m) AS merchants
        UNWIND range(0, size(merchants)-2) AS i
        WITH merchants[i] AS m1, merchants[i+1] AS m2
        MERGE (m1)-[s:FOLLOWED_BY]->(m2)
        ON CREATE SET s.sequenceCount = 1
        ON MATCH SET s.sequenceCount = coalesce(s.sequenceCount,0) + 1
        """;
        neo4jClient.query(cypher).run();
    }
}
