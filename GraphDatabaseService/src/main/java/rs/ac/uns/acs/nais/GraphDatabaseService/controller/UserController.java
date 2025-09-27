package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.User;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.IUserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final IUserService userService;

  @PostMapping
  public ResponseEntity<User> create(@RequestBody User u) {
    User saved = userService.create(u);
    return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
  }

  @GetMapping
  public ResponseEntity<List<User>> findAll() {
    return ResponseEntity.ok(userService.list());
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> findById(@PathVariable String id) {
    return userService.get(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> update(@PathVariable String id, @RequestBody User u) {
    u.setId(id);
    return ResponseEntity.ok(userService.update(u));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // --- RELACIJE: OWNS ---
  @PostMapping("/{userId}/owns/{cardId}")
  public ResponseEntity<Void> ownsCard(@PathVariable String userId, @PathVariable String cardId) {
    userService.ownsCard(userId, cardId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{userId}/owns/{cardId}")
  public ResponseEntity<Void> unownsCard(@PathVariable String userId, @PathVariable String cardId) {
    userService.unownsCard(userId, cardId);
    return ResponseEntity.noContent().build();
  }
}
