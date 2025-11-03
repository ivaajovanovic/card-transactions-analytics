package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.TimeBucket;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.TimeBucketRepository;

import java.util.List;

@RestController
@RequestMapping("/api/time-buckets")
@RequiredArgsConstructor
public class TimeBucketController {
    private final TimeBucketRepository repo;

    @GetMapping
    public List<TimeBucket> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public TimeBucket get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }

    @PostMapping
    public TimeBucket create(@RequestBody TimeBucket tb) { return repo.save(tb); }

    @PutMapping("/{id}")
    public TimeBucket update(@PathVariable Long id, @RequestBody TimeBucket tb) { tb.setId(id); return repo.save(tb); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
