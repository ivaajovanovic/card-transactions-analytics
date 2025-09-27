package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.Category;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {
  Category create(Category c);
  Optional<Category> get(String id);
  List<Category> list();
  Category update(Category c);
  void delete(String id);
}
