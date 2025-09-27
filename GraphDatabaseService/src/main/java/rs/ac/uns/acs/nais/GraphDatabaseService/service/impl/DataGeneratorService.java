package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGeneratorService {

    private final Neo4jClient neo4j;

    /* -------------------- PUBLIC API -------------------- */

    /** Brzo briše celu bazu (samo za razvoj!) */
    public long resetDatabase() {
        return neo4j.query("MATCH (n) DETACH DELETE n RETURN count(n) AS c")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }

    /** Napravi osnovne šifrarnike i veze (Category, CardType, Channel, Merchant->Category) */
    public void generateReferenceData(int merchantCount) {
        // ensureConstraints(); // Commenting out constraints that cause issues

        String[] categories = {
                "Groceries","Electronics","Restaurants","Fuel","Fashion","Pharmacy","Sports","Home","Online"
        };
        String[] cardTypes = {"Visa","Mastercard","Amex"};
        String[] channels  = {"POS","Online"};

        // Categories
        for (int i = 0; i < categories.length; i++) {
            Map<String,Object> p = Map.of("id", "CAT_"+(i+1), "name", categories[i]);
            neo4j.query("MERGE (c:Category {id:$id}) SET c.name=$name").bindAll(p).run();
        }
        // CardTypes
        for (String ct : cardTypes) {
            Map<String,Object> p = Map.of("id", "CT_"+ct.toUpperCase(), "name", ct);
            neo4j.query("MERGE (t:CardType {id:$id}) SET t.name=$name").bindAll(p).run();
        }
        // Channels
        for (String ch : channels) {
            Map<String,Object> p = Map.of("id", "CH_"+ch.toUpperCase(), "name", ch);
            neo4j.query("MERGE (ch:Channel {id:$id}) SET ch.name=$name").bindAll(p).run();
        }

        // Merchants (random category)
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 1; i <= merchantCount; i++) {
            String mid = "M_" + i;
            String mname = randomMerchantName(i);
            String catId = "CAT_" + (rnd.nextInt(categories.length) + 1);

            Map<String,Object> p = new HashMap<>();
            p.put("id", mid);
            p.put("name", mname);
            p.put("catId", catId);

            neo4j.query("""
                MERGE (m:Merchant {id:$id})
                  SET m.name=$name
                WITH m
                MATCH (c:Category {id:$catId})
                MERGE (m)-[:IN_CATEGORY]->(c)
            """).bindAll(p).run();
        }

        log.info("[generator] reference data generated ({} merchants).", merchantCount);
    }

    /** Generiše N korisnika i svakom 1..maxCards kartica (sa CardType) */
    public void generateUsers(int userCount, int maxCards) {
        // ensureConstraints(); // Commenting out constraints that cause issues
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = 1; i <= userCount; i++) {
            String uid = "U_" + i;
            Map<String,Object> u = Map.of(
                    "id", uid,
                    "name", "User "+i,
                    "email", "user"+i+"@example.com"
            );
            neo4j.query("MERGE (u:User {id:$id}) SET u.name=$name, u.email=$email").bindAll(u).run();

            int cards = 1 + rnd.nextInt(Math.max(1, maxCards));
            for (int k = 1; k <= cards; k++) {
                String cid = "C_"+i+"_"+k;
                String ct  = rnd.nextBoolean() ? "CT_VISA" : "CT_MASTERCARD"; // najčešće
                if (rnd.nextInt(10) == 0) ct = "CT_AMEX"; // poneka Amex
                String panMasked = (rnd.nextBoolean() ? "411111" : "550000") + "******" + String.format("%04d", rnd.nextInt(10000));

                Map<String,Object> p = new HashMap<>();
                p.put("cid", cid);
                p.put("pan", panMasked);
                p.put("uid", uid);
                p.put("ctid", ct);

                neo4j.query("""
                    MATCH (u:User {id:$uid})
                    MATCH (ct:CardType {id:$ctid})
                    MERGE (c:Card {id:$cid})
                      SET c.pan=$pan
                    MERGE (u)-[:OWNS]->(c)
                    MERGE (c)-[:IS_TYPE]->(ct)
                """).bindAll(p).run();
            }
        }
        log.info("[generator] users generated: {} (maxCards/user={})", userCount, maxCards);
    }

    /** Generiše transakcije u poslednjih X dana i vezuje ih za postojeće kartice/merchante/kanale */
    public void generateTransactions(int txCount, int daysBack, double suspiciousPosRate, double crossChannelRate) {
        // ensureConstraints(); // Commenting out constraints that cause issues
        List<String> cardIds = listIds("Card");
        List<String> merchantIds = listIds("Merchant");
        List<String> channels = List.of("CH_POS","CH_ONLINE");

        if (cardIds.isEmpty() || merchantIds.isEmpty()) {
            throw new IllegalStateException("Nema kartica ili merchanata! Pozovi prvo generateReferenceData() i generateUsers().");
        }

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        long now = Instant.now().getEpochSecond();
        long since = now - daysBack * 24L * 3600L;

        int made = 0;
        for (int i = 1; i <= txCount; i++) {
            // base
            String tid = "T_" + UUID.randomUUID();
            String cardId = pick(cardIds, rnd);
            String merchantId = pick(merchantIds, rnd);
            String channel = pick(channels, rnd);

            double amount = round2(rnd.nextDouble(5.0, 5000.0));
            boolean cardPresent = channel.equals("CH_POS") && rnd.nextDouble() > suspiciousPosRate; // deo POS ide kao false
            String currency = "RSD";
            String tsIso = LocalDateTime.ofEpochSecond(rnd.nextLong(since, now), 0, ZoneOffset.UTC).toString();

            createTx(tid, tsIso, amount, currency, cardId, merchantId, channel, cardPresent);
            made++;

            // ponekad “cross-channel” duplikat u kratkom roku
            if (rnd.nextDouble() < crossChannelRate) {
                String otherChannel = channel.equals("CH_POS") ? "CH_ONLINE" : "CH_POS";
                boolean otherCardPresent = otherChannel.equals("CH_POS") && rnd.nextDouble() > suspiciousPosRate;
                String tid2 = "T_" + UUID.randomUUID();
                // par minuta kasnije, sličan iznos
                String ts2 = LocalDateTime.ofEpochSecond(
                        Math.min(now, LocalDateTime.parse(tsIso).toEpochSecond(ZoneOffset.UTC) + rnd.nextInt(60, 3600)),
                        0, ZoneOffset.UTC).toString();
                double amount2 = round2(amount * (0.9 + rnd.nextDouble(0.0, 0.2)));
                createTx(tid2, ts2, amount2, currency, cardId, merchantId, otherChannel, otherCardPresent);
                made++;
            }
        }

        log.info("[generator] transactions generated: {}", made);
    }

    /** Full demo: referentni podaci + korisnici + transakcije */
    public void generateSampleData(int merchants, int users, int maxCardsPerUser, int tx, int daysBack) {
        generateReferenceData(merchants);
        generateUsers(users, maxCardsPerUser);
        // 15% sumnjivih POS, 20% cross-channel “duplih” pogodaka
        generateTransactions(tx, daysBack, 0.15, 0.20);
    }

    /* -------------------- PRIVATE HELPERS -------------------- */

    private void ensureConstraints() {
        // Jedinstveni ID-jevi za sve glavne tipove
        neo4j.query("CREATE CONSTRAINT IF NOT EXISTS FOR (n:User) REQUIRE n.id IS UNIQUE").run();
        neo4j.query("CREATE CONSTRAINT IF NOT EXISTS FOR (n:Card) REQUIRE n.id IS UNIQUE").run();
        neo4j.query("CREATE CONSTRAINT IF NOT EXISTS FOR (n:CardType) REQUIRE n.id IS UNIQUE").run();
        neo4j.query("CREATE CONSTRAINT IF NOT EXISTS FOR (n:Merchant) REQUIRE n.id IS UNIQUE").run();
        neo4j.query("CREATE CONSTRAINT IF NOT EXISTS FOR (n:Category) REQUIRE n.id IS UNIQUE").run();
        neo4j.query("CREATE CONSTRAINT IF NOT EXISTS FOR (n:Channel) REQUIRE n.id IS UNIQUE").run();
        neo4j.query("CREATE CONSTRAINT IF NOT EXISTS FOR (n:Transaction) REQUIRE n.id IS UNIQUE").run();
    }

    private void createTx(String id, String tsIso, double amount, String currency,
                          String cardId, String merchantId, String channelId, boolean cardPresent) {
        Map<String,Object> p = new HashMap<>();
        p.put("id", id);
        p.put("ts", tsIso);
        p.put("amount", amount);
        p.put("currency", currency);
        p.put("cardId", cardId);
        p.put("merchantId", merchantId);
        p.put("channelId", channelId);
        p.put("cardPresent", cardPresent);

        // Prvo dobijamo channel name iz Channel objekta
        String channelName = neo4j.query("MATCH (ch:Channel {id:$channelId}) RETURN ch.name AS name")
                .bindAll(Map.of("channelId", channelId))
                .fetchAs(String.class)
                .one()
                .orElse("UNKNOWN");
        
        p.put("channelName", channelName);
        
        neo4j.query("""
            MATCH (c:Card {id:$cardId})
            MATCH (m:Merchant {id:$merchantId})
            MERGE (t:Transaction {id:$id})
              SET t.ts = datetime($ts),
                  t.amount = $amount,
                  t.currency = $currency
            MERGE (t)-[:MADE_WITH]->(c)
            MERGE (t)-[:SPENT_ON {channel:$channelName, cardPresent:$cardPresent}]->(m)
        """).bindAll(p).run();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static <T> T pick(List<T> list, ThreadLocalRandom rnd) {
        return list.get(rnd.nextInt(list.size()));
    }

    private List<String> listIds(String label) {
    return neo4j.query("MATCH (n:" + label + ") RETURN n.id AS id")
            .fetchAs(String.class)
            .mappedBy((t, r) -> r.get("id").asString())
            .all()                 // Collection<String>
            .stream()
            .toList();             // -> List<String> (Java 16+)
}


    private String randomMerchantName(int i) {
        String[] base = {"Maxi","Idea","Gigatron","Tehnomanija","CafeX","McDonalds","IKEA","Decathlon",
                "OMV","NIS","Lilly","DM","FashionHub","SportTime","HomeCenter","PharmacyCare","MovieWorld",
                "OnlineStore","Grocery Express","Coffee Corner"};
        return base[i % base.length] + " #" + i;
    }
}
