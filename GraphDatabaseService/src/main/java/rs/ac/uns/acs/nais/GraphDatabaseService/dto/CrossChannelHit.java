package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

public record CrossChannelHit(
        String user,
        String merchant,
        Long occurrences
) {
}
