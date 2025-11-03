package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.DeviceNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.DeviceRepository;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceRepository repo;

    @GetMapping
    public List<DeviceNode> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public DeviceNode get(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }

    @PostMapping
    public DeviceNode create(@RequestBody DeviceNode device) { return repo.save(device); }

    @PutMapping("/{id}")
    public DeviceNode update(@PathVariable Long id, @RequestBody DeviceNode device) { device.setId(id); return repo.save(device); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }
}
