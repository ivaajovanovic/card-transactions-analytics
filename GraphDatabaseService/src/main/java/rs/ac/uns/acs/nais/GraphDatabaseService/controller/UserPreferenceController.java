package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.UserPreferenceNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.LoyaltyTier;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.UserPreferenceService;

import java.util.List;

@RestController
@RequestMapping("/api/user-preferences")
@RequiredArgsConstructor
public class UserPreferenceController {
    
    private final UserPreferenceService userPreferenceService;

    @PostMapping
    public ResponseEntity<UserPreferenceNode> create(@RequestBody UserPreferenceNode preference) {
        UserPreferenceNode created = userPreferenceService.save(preference);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserPreferenceNode> getById(@PathVariable Long id) {
        return userPreferenceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userExternalId}")
    public ResponseEntity<UserPreferenceNode> getByUserExternalId(@PathVariable String userExternalId) {
        return userPreferenceService.findByUserExternalId(userExternalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserPreferenceNode>> getAll() {
        return ResponseEntity.ok(userPreferenceService.findAll());
    }

    @GetMapping("/loyalty-tier/{tier}")
    public ResponseEntity<List<UserPreferenceNode>> getByLoyaltyTier(@PathVariable LoyaltyTier tier) {
        return ResponseEntity.ok(userPreferenceService.findByLoyaltyTier(tier));
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<UserPreferenceNode>> getHighValueUsers(
            @RequestParam(defaultValue = "1000.0") Double minSpend) {
        return ResponseEntity.ok(userPreferenceService.findHighValueUsers(minSpend));
    }

    @GetMapping("/similar/{userExternalId}")
    public ResponseEntity<List<UserPreferenceNode>> getSimilarUsers(
            @PathVariable String userExternalId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(userPreferenceService.findSimilarUsers(userExternalId, limit));
    }

    @GetMapping("/frequent-transactors")
    public ResponseEntity<List<UserPreferenceNode>> getFrequentTransactors(
            @RequestParam(defaultValue = "10") Integer minFrequency) {
        return ResponseEntity.ok(userPreferenceService.findFrequentTransactors(minFrequency));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserPreferenceNode> update(@PathVariable Long id, @RequestBody UserPreferenceNode preference) {
        return userPreferenceService.findById(id)
                .map(existing -> {
                    preference.setId(id);
                    UserPreferenceNode updated = userPreferenceService.save(preference);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userPreferenceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
