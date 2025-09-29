package rs.ac.uns.acs.nais.columnar.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDayTotalsDTO {
    private LocalDate date;
    private int dayCount;
    private long dayAmountCents;
}
