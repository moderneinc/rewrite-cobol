package io.moderne.cobol.internal;

import java.util.StringJoiner;

public class ExceptionPrinter {
    private ExceptionPrinter() {
    }

    public static String getSanitizedStackTrace(Throwable t) {
        StringJoiner sanitized = new StringJoiner("\n");

        int causeDepth = 0;
        for (Throwable tt = t; tt != null; tt = tt.getCause(), causeDepth++) {
            sanitized.add((causeDepth == 0 ? "" : "Caused by ") +
                          tt.getClass().getName() + ": " + tt.getLocalizedMessage());
            int i = 0;
            for (StackTraceElement stackTraceElement : tt.getStackTrace()) {
                if (stackTraceElement.getClassName().startsWith("picocli")) {
                    break;
                }
                if (i++ >= 8) {
                    sanitized.add("  ...");
                    break;
                }
                sanitized.add("  " + stackTraceElement);
            }
        }
        return sanitized.toString();
    }
}
