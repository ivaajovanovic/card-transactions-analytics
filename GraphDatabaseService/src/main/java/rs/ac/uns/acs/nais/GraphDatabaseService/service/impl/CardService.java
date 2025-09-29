package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Card;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Channel;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.CardRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.ICardService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService implements ICardService {
  private final CardRepository cardRepo;

  @Override public Card create(Card c){ return cardRepo.save(c); }
  @Override public Optional<Card> get(String id){ return cardRepo.findById(id); }
  @Override public List<Card> list(){ return cardRepo.findAll(); }
  @Override public Card update(Card c){ return cardRepo.save(c); }
  @Override public void delete(String id){ cardRepo.detachDelete(id); }

  @Override public void spentOn(String cardId, String txId, Channel channel, Boolean present){
    cardRepo.upsertSpentOn(cardId, txId, channel, present);
  }
  @Override public void removeSpentOn(String cardId, String txId){
    cardRepo.removeSpentOn(cardId, txId);
  }
}
