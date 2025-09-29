package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.Merchant;

import java.util.List;
import java.util.Optional;

public interface IMerchantService {
  Merchant create(Merchant m);
  Optional<Merchant> get(String id);
  List<Merchant> list();
  Merchant update(Merchant m);
  void delete(String id);

  void belongsTo(String merchantId, String categoryId);
  List<Merchant> byCategory(String categoryId);
  void unsetCategory(String merchantId); // NOVO

}
