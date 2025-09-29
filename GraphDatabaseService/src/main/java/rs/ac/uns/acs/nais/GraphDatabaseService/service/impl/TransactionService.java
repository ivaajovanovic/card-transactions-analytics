package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Transaction;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.TransactionRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.ITransactionService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
  private final TransactionRepository txRepo;

  @Override public Transaction create(Transaction t){ return txRepo.save(t); }
  @Override public Optional<Transaction> get(String id){ return txRepo.findById(id); }
  @Override public List<Transaction> list(){ return txRepo.findAll(); }
  @Override public Transaction update(Transaction t){ return txRepo.save(t); }
  @Override public void delete(String id){ txRepo.detachDelete(id); }

  @Override public void processedAt(String txId, String merchantId){
    txRepo.relateProcessedAt(txId, merchantId);
  }

  @Override public void unsetProcessedAt(String txId){
  txRepo.unsetProcessedAt(txId); // treba @Query u repo
}

}
