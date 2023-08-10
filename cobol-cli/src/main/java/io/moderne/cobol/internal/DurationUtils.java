package io.moderne.cobol.internal;

import java.time.Duration;

public class DurationUtils {
    private DurationUtils() {
    }

    public static String formatDuration(Duration duration) {
        if (duration.toMillis() < 1000) {
            return (Math.round((duration.toMillis() / 10.0)) / 100.0) + "s";
        }
        return duration.withNanos(0).toString().substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }
}
