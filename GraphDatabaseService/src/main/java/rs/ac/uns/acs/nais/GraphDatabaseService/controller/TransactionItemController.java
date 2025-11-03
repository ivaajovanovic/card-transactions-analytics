package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.TransactionItem;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.TransactionItemRepository;

import java.util.List;

@RestController
@RequestMapping("/api/transaction-items")
@RequiredArgsConstructor
public class TransactionItemController {
    private final TransactionItemRepository repo;

    @GetMapping
    public List<TransactionItem> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public TransactionItem get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }

    @PostMapping
    public TransactionItem create(@RequestBody TransactionItem item) { return repo.save(item); }

    @PutMapping("/{id}")
    public TransactionItem update(@PathVariable Long id, @RequestBody TransactionItem item) { item.setId(id); return repo.save(item); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
