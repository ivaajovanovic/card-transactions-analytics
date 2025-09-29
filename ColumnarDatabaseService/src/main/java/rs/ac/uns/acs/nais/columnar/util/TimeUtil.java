package rs.ac.uns.acs.nais.columnar.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {
    private TimeUtil() {}

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    public static LocalDate toUtcDate(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).toLocalDate();
    }

    public static String toDayKey(LocalDate date) {
        return date.toString(); // YYYY-MM-DD
    }

    public static String toMonthKey(LocalDate date) {
        return MONTH.format(date); // YYYYMM
    }
}
