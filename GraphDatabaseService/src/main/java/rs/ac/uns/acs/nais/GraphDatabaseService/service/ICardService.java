package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.dto.SpentOnRequest;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Card;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Channel;

import java.util.List;
import java.util.Optional;

public interface ICardService {
  Card create(Card c);
  Optional<Card> get(String id);
  List<Card> list();
  Card update(Card c);
  void delete(String id);

  void spentOn(String cardId, String txId, Channel channel, Boolean present);
  void removeSpentOn(String cardId, String txId);

default void spentOn(String cardId, SpentOnRequest req) {
        spentOn(cardId, req.getTxId(), req.getChannel(), req.getCardPresent());
    }
}
