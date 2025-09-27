package rs.ac.uns.acs.nais.columnar.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDayTotalsDTO {
    private LocalDate date;
    private int count;
    private long amountCents;
}
