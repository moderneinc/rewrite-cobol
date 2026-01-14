package io.moderne.cobol.internal;

import lombok.Getter;
import org.fusesource.jansi.Ansi;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;

/**
 * Immediately stops the execution of the CLI without any further
 * message. The message will be written to {@link CommandLine.Model.CommandSpec#getErr()}
 * and execution halted. If there is an exception cause, its stack trace will be printed after the message.
 */
@Getter
public class FatalException extends RuntimeException {
    private final String message;
    private final String[] fixSuggestions;

    public FatalException(Ansi message, String... fixSuggestions) {
        this(message.toString(), fixSuggestions);
    }

    public FatalException(String message, String... fixSuggestions) {
        this(message, null, fixSuggestions);
    }

    public FatalException(String message, @Nullable Throwable cause, String... fixSuggestions) {
        super(message, cause);
        this.message = message;
        this.fixSuggestions = fixSuggestions;
    }
}
