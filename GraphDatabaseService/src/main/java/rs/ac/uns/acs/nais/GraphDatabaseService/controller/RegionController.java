package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.RegionNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.RegionRepository;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {
    private final RegionRepository repo;

    @GetMapping
    public List<RegionNode> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public RegionNode get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }

    @PostMapping
    public RegionNode create(@RequestBody RegionNode region) { return repo.save(region); }

    @PutMapping("/{id}")
    public RegionNode update(@PathVariable Long id, @RequestBody RegionNode region) { region.setId(id); return repo.save(region); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
