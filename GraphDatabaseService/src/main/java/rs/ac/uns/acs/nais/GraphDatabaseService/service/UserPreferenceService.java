package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.UserPreferenceNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.LoyaltyTier;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceService {
    UserPreferenceNode save(UserPreferenceNode preference);
    Optional<UserPreferenceNode> findById(Long id);
    Optional<UserPreferenceNode> findByUserExternalId(String userExternalId);
    List<UserPreferenceNode> findAll();
    List<UserPreferenceNode> findByLoyaltyTier(LoyaltyTier tier);
    List<UserPreferenceNode> findHighValueUsers(Double minSpend);
    List<UserPreferenceNode> findSimilarUsers(String userExternalId, Integer limit);
    List<UserPreferenceNode> findFrequentTransactors(Integer minFrequency);
    void deleteById(Long id);
}
