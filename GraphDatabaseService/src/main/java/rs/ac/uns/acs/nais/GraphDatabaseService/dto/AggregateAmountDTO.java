package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregateAmountDTO {
    private LocalDate bucketDate; // start of week or month
    private String groupKey; // purpose / category / paymentType
    private Double totalAmount;
}
