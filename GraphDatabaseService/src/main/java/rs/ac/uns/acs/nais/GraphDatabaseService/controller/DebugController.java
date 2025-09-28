package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {
    
    private final Neo4jClient neo4j;
    
    /**
     * Debug endpoint da vidimo kako izgledaju relationship-i u bazi
     */
    @GetMapping("/relationships")
    public ResponseEntity<?> checkRelationships() {
        
        // Proveri kako izgledaju SPENT_ON relationship-i
        var spentOnData = neo4j.query("""
            MATCH (t:Transaction)-[s:SPENT_ON]->(m:Merchant)
            RETURN t.id AS txId, s.channel AS channel, s.cardPresent AS cardPresent, m.name AS merchant
            LIMIT 10
            """)
            .fetch()
            .all();
        
        // Proveri kako izgledaju MADE_WITH relationship-i
        var madeWithData = neo4j.query("""
            MATCH (t:Transaction)-[:MADE_WITH]->(c:Card)
            RETURN t.id AS txId, c.id AS cardId
            LIMIT 10
            """)
            .fetch()
            .all();
        
        // Proveri cross-channel raw podatke
        var crossChannelDebug = neo4j.query("""
            MATCH (u:User)-[:OWNS]->(c:Card)<-[:MADE_WITH]-(t:Transaction)-[s:SPENT_ON]->(m:Merchant)
            WITH u.name AS user, m.name AS merchant, collect({channel: s.channel, ts: t.ts}) AS transactions
            WHERE size(transactions) > 1
            RETURN user, merchant, transactions
            LIMIT 5
            """)
            .fetch()
            .all();
        
        return ResponseEntity.ok(Map.of(
            "spentOnRelationships", spentOnData,
            "madeWithRelationships", madeWithData,
            "crossChannelDebug", crossChannelDebug,
            "message", "Debug data from Neo4j relationships"
        ));
    }
    
    /**
     * Debug endpoint za analytics test
     */
    @GetMapping("/analytics-test")
    public ResponseEntity<?> testAnalytics(@RequestParam(defaultValue = "U_1") String userId) {
        
        // Test osnovni upit
        var basicQuery = neo4j.query("""
            MATCH (u:User {id:$userId})-[:OWNS]->(c:Card)<-[:MADE_WITH]-(t:Transaction)-[s:SPENT_ON]->(m:Merchant)
            RETURN u.name AS user, c.id AS card, t.id AS transaction, s.channel AS channel, m.name AS merchant
            LIMIT 10
            """)
            .bindAll(Map.of("userId", userId))
            .fetch()
            .all();
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "basicQuery", basicQuery,
            "message", "Analytics test for user " + userId
        ));
    }
}