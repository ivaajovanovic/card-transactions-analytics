package rs.ac.uns.acs.nais.columnar.util;

public final class PagingUtil {
    private PagingUtil() {}

    public static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }
}
