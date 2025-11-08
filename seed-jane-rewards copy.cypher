// Seed reward programs and rules for Jane's cards to enable Best Card Benefits UI
// Idempotent MERGE operations

// Resolve Jane by provided email (preferred) or fallback to known variations
OPTIONAL MATCH (u1:User {email:'jane.william109@example.com'})
OPTIONAL MATCH (u2:User {email:'jane.williams109@example.com'})
WITH coalesce(u1,u2) AS u
WHERE u IS NOT NULL

// Collect only cards that have transactions (matching Card Details display logic)
MATCH (u)-[:OWNS]->(c:Card)
WHERE c.panHash IN ['TESTCARDUSR001091', 'TESTCARDUSR001092']
WITH u, collect(DISTINCT c) AS cards

// Create/Merge a reward program and link to all user's cards
MERGE (prog:RewardProgram {name:'Everyday Essentials', issuerBank:'Demo Bank'})
ON CREATE SET prog.startDate = datetime(), prog.endDate = null
WITH prog, cards
FOREACH (card IN cards |
  MERGE (prog)-[:APPLIES_TO]->(card)
)

// Grocery 3% (5411)
MERGE (r1:RewardRule {categoryCode:'5411', conditions:'Grocery 3% cashback up to $100/mo'})
ON CREATE SET r1.rewardRate = 0.03, r1.cap = 100.0
MERGE (prog)-[:HAS_RULE]->(r1)

// Dining 2% (5812)
MERGE (r2:RewardRule {categoryCode:'5812', conditions:'Dining 2% cashback'})
ON CREATE SET r2.rewardRate = 0.02, r2.cap = 100.0
MERGE (prog)-[:HAS_RULE]->(r2)

// Hotels 5% (7011)
MERGE (r3:RewardRule {categoryCode:'7011', conditions:'Hotels 5% cashback'})
ON CREATE SET r3.rewardRate = 0.05, r3.cap = 200.0
MERGE (prog)-[:HAS_RULE]->(r3)

RETURN size(cards) AS linkedCards, prog.name AS program, [r IN [r1,r2,r3] | {cat:r.categoryCode, rate:r.rewardRate}] AS rules;
