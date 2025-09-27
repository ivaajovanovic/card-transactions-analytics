package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

public record ShopsAtUpsertView(
        String userId,
        String merchantId,
        Long txCount,
        Double totalAmount
) {
}
