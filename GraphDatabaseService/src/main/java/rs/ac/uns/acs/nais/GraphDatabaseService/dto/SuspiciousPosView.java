package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

public record SuspiciousPosView(
        String cardId,
        String merchant,
        Long cnt,
        Double total
) {
}
