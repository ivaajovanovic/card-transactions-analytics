package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Merchant;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.IMerchantService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

  private final IMerchantService merchantService;

  @PostMapping
  public ResponseEntity<Merchant> create(@RequestBody Merchant m) {
    Merchant saved = merchantService.create(m);
    return ResponseEntity.created(URI.create("/api/merchants/" + saved.getId())).body(saved);
  }

  @GetMapping
  public ResponseEntity<List<Merchant>> findAll() {
    return ResponseEntity.ok(merchantService.list());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Merchant> findById(@PathVariable String id) {
    return merchantService.get(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Merchant> update(@PathVariable String id, @RequestBody Merchant m) {
    m.setId(id);
    return ResponseEntity.ok(merchantService.update(m));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    merchantService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // --- RELACIJE: BELONGS_TO (Merchant -> Category) ---
  @PostMapping("/{merchantId}/category/{categoryId}")
  public ResponseEntity<Void> setCategory(@PathVariable String merchantId, @PathVariable String categoryId) {
    merchantService.belongsTo(merchantId, categoryId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{merchantId}/category")
  public ResponseEntity<Void> unsetCategory(@PathVariable String merchantId) {
    merchantService.unsetCategory(merchantId);
    return ResponseEntity.noContent().build();
  }

  // --- Upit: merchant-i po kategoriji ---
  @GetMapping("/by-category/{categoryId}")
  public ResponseEntity<List<Merchant>> byCategory(@PathVariable String categoryId) {
    return ResponseEntity.ok(merchantService.byCategory(categoryId));
  }
}
