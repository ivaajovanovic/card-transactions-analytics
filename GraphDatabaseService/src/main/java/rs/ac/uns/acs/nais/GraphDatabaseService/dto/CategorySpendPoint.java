package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import java.time.LocalDate;

public record CategorySpendPoint(
        String category,
        LocalDate date,
        Double daily
) {
}
