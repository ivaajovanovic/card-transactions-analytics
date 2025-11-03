package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.CategoryNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.CategoryRepository;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository repo;

    @GetMapping
    public List<CategoryNode> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public CategoryNode get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }

    @PostMapping
    public CategoryNode create(@RequestBody CategoryNode category) { return repo.save(category); }

    @PutMapping("/{id}")
    public CategoryNode update(@PathVariable Long id, @RequestBody CategoryNode category) { category.setId(id); return repo.save(category); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
