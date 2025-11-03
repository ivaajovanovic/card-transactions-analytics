package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.UserPreferenceNode;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.LoyaltyTier;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.UserPreferenceRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.UserPreferenceService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {
    
    private final UserPreferenceRepository userPreferenceRepository;

    @Override
    public UserPreferenceNode save(UserPreferenceNode preference) {
        return userPreferenceRepository.save(preference);
    }

    @Override
    public Optional<UserPreferenceNode> findById(Long id) {
        return userPreferenceRepository.findById(id);
    }

    @Override
    public Optional<UserPreferenceNode> findByUserExternalId(String userExternalId) {
        return userPreferenceRepository.findByUserExternalId(userExternalId);
    }

    @Override
    public List<UserPreferenceNode> findAll() {
        return userPreferenceRepository.findAll();
    }

    @Override
    public List<UserPreferenceNode> findByLoyaltyTier(LoyaltyTier tier) {
        return userPreferenceRepository.findByLoyaltyTier(tier);
    }

    @Override
    public List<UserPreferenceNode> findHighValueUsers(Double minSpend) {
        return userPreferenceRepository.findHighValueUsers(minSpend);
    }

    @Override
    public List<UserPreferenceNode> findSimilarUsers(String userExternalId, Integer limit) {
        return userPreferenceRepository.findSimilarUsers(userExternalId, limit);
    }

    @Override
    public List<UserPreferenceNode> findFrequentTransactors(Integer minFrequency) {
        return userPreferenceRepository.findFrequentTransactors(minFrequency);
    }

    @Override
    public void deleteById(Long id) {
        userPreferenceRepository.deleteById(id);
    }
}
