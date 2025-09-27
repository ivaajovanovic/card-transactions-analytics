package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Category;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.ICategoryService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category c) {
        Category saved = categoryService.create(c);
        return ResponseEntity
                .created(URI.create("/api/categories/" + saved.getId()))
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll() {
        return ResponseEntity.ok(categoryService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> findById(@PathVariable String id) {
        return categoryService.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable String id, @RequestBody Category c) {
        c.setId(id);
        return ResponseEntity.ok(categoryService.update(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
