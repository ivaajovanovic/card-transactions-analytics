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
    
    /**
     * Debug endpoint za proveravenje iznosa transakcija
     */
    @GetMapping("/transaction-amounts")
    public ResponseEntity<?> checkTransactionAmounts() {
        
        // Ukupan broj transakcija
        var totalCount = neo4j.query("MATCH (t:Transaction) RETURN count(t) AS count")
            .fetchAs(Long.class)
            .one()
            .orElse(0L);
            
        // Min, max, avg iznos
        var amountStats = neo4j.query("""
            MATCH (t:Transaction) 
            RETURN min(t.amount) AS min, max(t.amount) AS max, avg(t.amount) AS avg
            """)
            .fetch()
            .one();
            
        // Transakcije sa visokim iznosima (> 5000)
        var highAmountTx = neo4j.query("""
            MATCH (t:Transaction)
            WHERE t.amount > 5000
            RETURN t.id AS txId, t.amount AS amount
            LIMIT 10
            """)
            .fetch()
            .all();
            
        // Transakcije sa niskim iznosima (< 10)
        var lowAmountTx = neo4j.query("""
            MATCH (t:Transaction)
            WHERE t.amount < 10
            RETURN t.id AS txId, t.amount AS amount
            LIMIT 10
            """)
            .fetch()
            .all();
            
        // Distribucija iznosa transakcija
        var amountDistribution = neo4j.query("""
            MATCH (t:Transaction)
            WITH 
              CASE 
                WHEN t.amount < 10 THEN 'Very Low (<10)'
                WHEN t.amount >= 10 AND t.amount < 100 THEN 'Low (10-100)'
                WHEN t.amount >= 100 AND t.amount < 1000 THEN 'Medium (100-1000)'
                WHEN t.amount >= 1000 AND t.amount <= 5000 THEN 'High (1000-5000)'
                WHEN t.amount > 5000 THEN 'Very High (>5000)'
                ELSE 'Unknown'
              END AS range,
              count(*) AS count
            RETURN range, count
            ORDER BY count DESC
            """)
            .fetch()
            .all();

        return ResponseEntity.ok(Map.of(
            "message", "Transaction amounts analysis",
            "totalTransactions", totalCount,
            "amountStats", amountStats,
            "highAmountTransactions", highAmountTx,
            "lowAmountTransactions", lowAmountTx,
            "amountDistribution", amountDistribution
        ));
    }
}