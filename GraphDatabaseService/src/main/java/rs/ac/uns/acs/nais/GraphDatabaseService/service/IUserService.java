package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.User;
import java.util.List;
import java.util.Optional;

public interface IUserService {
  User create(User u);
  Optional<User> get(String id);
  List<User> list();
  User update(User u);
  void delete(String id);

  void ownsCard(String userId, String cardId);
  void unownsCard(String userId, String cardId); // NOVO
}
