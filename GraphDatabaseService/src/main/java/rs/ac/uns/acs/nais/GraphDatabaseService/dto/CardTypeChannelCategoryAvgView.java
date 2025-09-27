package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

public record CardTypeChannelCategoryAvgView(
        String channel,
        String category,
        Long txCount,
        Double avgTicket
) {
}
