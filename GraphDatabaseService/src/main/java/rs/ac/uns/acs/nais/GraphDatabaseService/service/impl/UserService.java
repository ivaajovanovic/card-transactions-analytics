package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.User;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.UserRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.IUserService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
  private final UserRepository userRepo;

  @Override public User create(User u){ return userRepo.save(u); }
  @Override public Optional<User> get(String id){ return userRepo.findById(id); }
  @Override public List<User> list(){ return userRepo.findAll(); }
  @Override public User update(User u){ return userRepo.save(u); }
  @Override public void delete(String id){ userRepo.detachDelete(id); }

  @Override public void ownsCard(String userId, String cardId){
    userRepo.relateOwns(userId, cardId);
  }
  @Override public void unownsCard(String userId, String cardId){
  userRepo.unlinkOwns(userId, cardId); // treba @Query u repo
}
}
