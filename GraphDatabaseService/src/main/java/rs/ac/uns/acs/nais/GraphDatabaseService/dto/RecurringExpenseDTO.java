package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecurringExpenseDTO {
    private String merchantName;
    private Double avgMonthlySpend;
    private Long months;
}
