package rs.ac.uns.acs.nais.GraphDatabaseService.util;

import java.time.*;

public class DateUtils {
    
    public static Instant startOfMonth(YearMonth ym, ZoneId zone) {
        return ym.atDay(1).atStartOfDay(zone).toInstant();
    }
    
    public static Instant endOfMonth(YearMonth ym, ZoneId zone) {
        return ym.atEndOfMonth().plusDays(1).atStartOfDay(zone).toInstant();
    }
    
    public static Instant startOfDay(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }
    
    public static Instant endOfDay(LocalDate date, ZoneId zone) {
        return date.plusDays(1).atStartOfDay(zone).toInstant();
    }
}
