package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.AcceptanceNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.AcceptanceRepository;

import java.util.List;

@RestController
@RequestMapping("/api/acceptances")
@RequiredArgsConstructor
public class AcceptanceController {
    private final AcceptanceRepository repo;

    @GetMapping
    public List<AcceptanceNode> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public AcceptanceNode get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }

    @PostMapping
    public AcceptanceNode create(@RequestBody AcceptanceNode node) { return repo.save(node); }

    @PutMapping("/{id}")
    public AcceptanceNode update(@PathVariable Long id, @RequestBody AcceptanceNode node) { node.setId(id); return repo.save(node); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
