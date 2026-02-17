package org.openrewrite.cobol;

import java.nio.file.Path;

public class CopybookParsingException extends Exception {
    private final Path sourcePath;

    public CopybookParsingException(Path sourcePath, String message, Throwable t) {
        super(message, t);
        this.sourcePath = sourcePath;
    }

    public Path getSourcePath() {
        return sourcePath;
    }
}
