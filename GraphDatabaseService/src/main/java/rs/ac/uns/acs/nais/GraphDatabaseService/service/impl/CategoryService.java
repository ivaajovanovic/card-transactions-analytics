package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Category;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.CategoryRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.ICategoryService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
  private final CategoryRepository categoryRepo;

  @Override public Category create(Category c){ return categoryRepo.save(c); }
  @Override public Optional<Category> get(String id){ return categoryRepo.findById(id); }
  @Override public List<Category> list(){ return categoryRepo.findAll(); }
  @Override public Category update(Category c){ return categoryRepo.save(c); }
  @Override public void delete(String id){ categoryRepo.detachDelete(id); }
}
