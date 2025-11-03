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
    
    private final Neo4jClient neo4jClient;
    
    @PostMapping("/create-test-transactions")
    public ResponseEntity<Map<String, Object>> createTestTransactions(@RequestParam String email) {
        String query = """
            MATCH (u:User {email: $email})
            MATCH (m:Merchant)
            WITH u, collect(m)[0..10] as merchants
            UNWIND range(1, 2) as cardNum
            CREATE (c:Card {
                panHash: 'TESTCARD' + u.externalId + cardNum,
                network: 'VISA',
                type: 'CREDIT',
                issuerCountry: u.homeCountry,
                monthlyLimit: 5000.0
            })
            // Important: OWNS points from Card to User to match queries (u)<-[:OWNS]-(c)
            CREATE (c)-[:OWNS]->(u)
            WITH c, merchants
            UNWIND merchants as merchant
            UNWIND range(1, 4) as txNum
            CREATE (c)-[:TRANSACTED_WITH {
                amount: 50.0 + (txNum * 10.0),
                currency: 'USD',
                timestamp: datetime('2024-06-15T12:00:00Z'),
                status: 'SUCCESS',
                channel: 'IN_STORE',
                purpose: 'GOODS',
                paymentType: 'CARD_PRESENT',
                contactless: true
            }]->(merchant)
            RETURN count(*) as transactionsCreated
            """;
        
        try {
            var result = neo4jClient.query(query)
                .bind(email).to("email")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
            
            return ResponseEntity.ok(Map.of(
                "message", "Created test transactions",
                "email", email,
                "transactionsCreated", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> userStats(@RequestParam String email) {
        String q = """
            MATCH (u:User {email:$email})
            OPTIONAL MATCH (u)<-[:OWNS]-(c:Card)
            WITH u, collect(c) AS cards
            OPTIONAL MATCH (c:Card)-[t:TRANSACTED_WITH]->(m:Merchant)
            WHERE c IN cards
            RETURN size(cards) AS cardsCount, count(t) AS txCount
        """;
        var rec = neo4jClient.query(q)
                .bind(email).to("email")
                .fetch().one().orElse(Map.of("cardsCount",0L,"txCount",0L));
        return ResponseEntity.ok(rec);
    }
}
