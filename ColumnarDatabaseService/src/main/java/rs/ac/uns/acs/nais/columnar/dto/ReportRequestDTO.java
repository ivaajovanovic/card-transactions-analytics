package rs.ac.uns.acs.nais.columnar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    private String reportType; // "simple" ili "complex"
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> categories; // kategorije za filtriranje
    private String format; // "JSON" ili "PDF"
    private String theme; // "online" ili "offline"
    
    // Za složene upite
    private Double minAmount;
    private Double maxAmount;
    private String currency;
    private String status;
    private String groupBy; // "category", "merchant", "date"
}