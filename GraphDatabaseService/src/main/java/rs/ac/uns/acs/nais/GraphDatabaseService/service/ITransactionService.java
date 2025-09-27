package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface ITransactionService {
  Transaction create(Transaction t);
  Optional<Transaction> get(String id);
  List<Transaction> list();
  Transaction update(Transaction t);
  void delete(String id);

  void processedAt(String txId, String merchantId);
  void unsetProcessedAt(String txId); // NOVO

}
