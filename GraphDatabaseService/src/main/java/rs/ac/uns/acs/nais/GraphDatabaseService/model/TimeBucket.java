package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.SeasonName;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.enums.TimeOfDay;
import lombok.*;

@Node("TimeBucket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeBucket {
    @Id
    @GeneratedValue
    private Long id;
    
    private String granularity; // "MONTH" | "WEEK"
    private Integer year;
    private Integer month; // if MONTH
    private Integer week; // if WEEK (ISO week)
    
    // Temporal Pattern Fields
    private Integer dayOfWeek; // 1-7 (Monday-Sunday)
    private TimeOfDay timeOfDay; // EARLY_MORNING, MORNING, AFTERNOON, EVENING, NIGHT
    private Boolean isWeekend; // true if Saturday/Sunday
    private Boolean isHoliday; // true if public holiday
    private SeasonName seasonName; // SPRING, SUMMER, FALL, WINTER
}
