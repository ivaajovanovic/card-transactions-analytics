package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Merchant;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.MerchantRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.IMerchantService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MerchantService implements IMerchantService {
  private final MerchantRepository merchantRepo;

  @Override public Merchant create(Merchant m){ return merchantRepo.save(m); }
  @Override public Optional<Merchant> get(String id){ return merchantRepo.findById(id); }
  @Override public List<Merchant> list(){ return merchantRepo.findAll(); }
  @Override public Merchant update(Merchant m){ return merchantRepo.save(m); }
  @Override public void delete(String id){ merchantRepo.detachDelete(id); }

  @Override public void belongsTo(String merchantId, String categoryId){
    merchantRepo.relateToCategory(merchantId, categoryId);
  }

  @Override public List<Merchant> byCategory(String categoryId){
    return merchantRepo.findByCategory(categoryId);
  }

  @Override public void unsetCategory(String merchantId){
  merchantRepo.unsetCategory(merchantId); // treba @Query u repo
}

}
