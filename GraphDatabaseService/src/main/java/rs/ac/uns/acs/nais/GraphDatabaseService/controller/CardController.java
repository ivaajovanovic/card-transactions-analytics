package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.SpentOnRequest;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Card;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.ICardService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

  private final ICardService cardService;

  @PostMapping
  public ResponseEntity<Card> create(@RequestBody Card c) {
    Card saved = cardService.create(c);
    return ResponseEntity.created(URI.create("/api/cards/" + saved.getId())).body(saved);
  }

  @GetMapping
  public ResponseEntity<List<Card>> findAll() {
    return ResponseEntity.ok(cardService.list());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Card> findById(@PathVariable String id) {
    return cardService.get(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Card> update(@PathVariable String id, @RequestBody Card c) {
    c.setId(id);
    return ResponseEntity.ok(cardService.update(c));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    cardService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // --- RELACIJE: SPENT_ON (Card -> Transaction sa properti-ima) ---
  @PostMapping("/{cardId}/spent-on")
  public ResponseEntity<Void> spentOn(@PathVariable String cardId, @RequestBody SpentOnRequest req) {
    cardService.spentOn(cardId, req); // koristi preopterećenu metodu
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{cardId}/spent-on/{txId}")
  public ResponseEntity<Void> removeSpentOn(@PathVariable String cardId, @PathVariable String txId) {
    cardService.removeSpentOn(cardId, txId);
    return ResponseEntity.noContent().build();
  }
}
