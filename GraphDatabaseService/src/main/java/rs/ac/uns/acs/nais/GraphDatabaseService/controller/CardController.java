package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.CardNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.CardRepository;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardRepository repo;

    @GetMapping
    public List<CardNode> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public CardNode get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }

    @PostMapping
    public CardNode create(@RequestBody CardNode card) { return repo.save(card); }

    @PutMapping("/{id}")
    public CardNode update(@PathVariable Long id, @RequestBody CardNode card) { card.setId(id); return repo.save(card); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
