package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.dto.SpentOnDto;

public interface IGraphRelationsService {
    // veze bez svojstava
    void relateOwns(String userId, String cardId);
    void relateBelongsTo(String merchantId, String categoryId);
    void relateProcessedAt(String txId, String merchantId);

    // veza sa svojstvima
    void upsertSpentOn(SpentOnDto dto);
    void deleteSpentOn(String cardId, String txId);
}
