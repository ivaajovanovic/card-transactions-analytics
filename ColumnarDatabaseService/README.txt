0) Preflight

# servis radi?
curl.exe -s http://localhost:9050/actuator/health
# očekuješ: {"status":"UP",...}

# (opciono) otvori CQL shell
docker exec -it cta-cassandra cqlsh

1) Jedan event (C – Create)
$BASE = "http://localhost:9050"
# identifikatori (isti kroz sve testove)
$U1 = "11111111-1111-1111-1111-111111111111"
$C1 = "22222222-2222-2222-2222-222222222222"
$M1 = "33333333-3333-3333-3333-333333333333"
$G1 = "44444444-4444-4444-4444-444444444444"

# koristimo fiksni txId da posle testiramo deduplikaciju
$TX_DUP = "9f8b2b4e-5c3c-4a44-9a7b-1b5e4d5a12ef"

# SINGLE
$json = @"
{
  "txId":"$TX_DUP",
  "userId":"$U1",
  "cardId":"$C1",
  "merchantId":"$M1",
  "categoryId":"$G1",
  "amountCents":12345,
  "currency":"EUR",
  "status":"SETTLED",
  "occurredAt":"2025-09-28T14:59:30Z"
}
"@

curl.exe -s -X POST "$BASE/events/transactions" -H "Content-Type: application/json" --data-binary $json

Provera u CQL-u (docker exec -it cta-cassandra cqlsh):

USE nais;

/* red u fakt tabelama (tx_by_*) */
SELECT user_id, tx_date, tx_id, amount_cents
FROM tx_by_user
WHERE user_id=$U1 AND tx_date='2025-09-28';

SELECT merchant_id, tx_date, tx_id, amount_cents
FROM tx_by_merchant
WHERE merchant_id=$M1 AND tx_date='2025-09-28';

SELECT category_id, tx_date, tx_id, amount_cents
FROM tx_by_category
WHERE category_id=$G1 AND tx_date='2025-09-28';

/* tx_dedup evidentiran */
SELECT * FROM tx_dedup WHERE tx_id=$TX_DUP;
