/*
 * Copyright 2026 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.mainframe.cobol;

import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

/**
 * The file's name promised a language its content is not written in: a control card kept as
 * {@code .cbl}, a CICS parameter member kept as {@code .jcl}. Every parser reports it under this one
 * type so that a parse-quality report can tell a misnamed member from a grammar gap without knowing
 * each parser's own exception.
 * <p>
 * A source parsed from a string has no name to have promised anything, so it is never reported
 * this way.
 */
public class WrongLanguageException extends RuntimeException {
    @Getter
    private final Path sourcePath;

    public WrongLanguageException(Path sourcePath, String message, @Nullable Throwable cause) {
        super(message, cause);
        this.sourcePath = sourcePath;
    }
}
