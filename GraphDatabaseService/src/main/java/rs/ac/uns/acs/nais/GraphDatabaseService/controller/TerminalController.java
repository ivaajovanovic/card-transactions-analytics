package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.TerminalNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.TerminalRepository;

import java.util.List;

@RestController
@RequestMapping("/api/terminals")
@RequiredArgsConstructor
public class TerminalController {
    private final TerminalRepository repo;

    @GetMapping
    public List<TerminalNode> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public TerminalNode get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }

    @PostMapping
    public TerminalNode create(@RequestBody TerminalNode terminal) { return repo.save(terminal); }

    @PutMapping("/{id}")
    public TerminalNode update(@PathVariable Long id, @RequestBody TerminalNode terminal) { terminal.setId(id); return repo.save(terminal); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
