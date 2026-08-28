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
package org.openrewrite.mainframe.assembler;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.assembler.tree.Assembler;
import org.openrewrite.mainframe.assembler.tree.Space;
import org.openrewrite.mainframe.cobol.WrongLanguageException;
import org.openrewrite.mainframe.ims.ImsParser;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.marker.Markers;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.openrewrite.Tree.randomId;

/**
 * Reads the HLASM libraries a shop keeps: the programs of {@code ASM} and the macros and copy members
 * of {@code MACLIB}, which are the same language and are told apart by nothing but the library they
 * live in.
 * <p>
 * An {@code .asm} is not always a program. IMS gen libraries are macro source too and are often kept
 * under that extension, so {@link ImsParser#isGenSource} is asked first and whatever it claims is left
 * alone.
 */
public class AssemblerParser implements Parser {
    /**
     * Compared case-insensitively. A macro library member is HLASM whether it defines a macro or is
     * copied verbatim, so both extensions read the same way.
     */
    public static final List<String> ASSEMBLER_FILE_EXTENSIONS = unmodifiableList(asList(".asm", ".mac"));

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing an assembler member")
                            .tag("file.type", "Assembler");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();
                        if (!sourceFile.isSynthetic() && ImsParser.isGenSource(sourceFile.getPath())) {
                            throw new WrongLanguageException(sourceFile.getPath(),
                                    sourceFile.getPath() + " is an IMS gen member, not an assembler program.", null);
                        }
                        AssemblerLineReader reader = new AssemblerLineReader(sourceStr);
                        Assembler.CompilationUnit cu = new Assembler.CompilationUnit(
                                randomId(),
                                path,
                                sourceFile.getFileAttributes(),
                                Space.EMPTY,
                                Markers.EMPTY,
                                is.getCharset().name(),
                                is.isCharsetBomMarked(),
                                null,
                                reader.getStatements(),
                                reader.getEof()
                        );

                        sample.stop(MetricsHelper.successTags(timer).register(Metrics.globalRegistry));
                        parsingListener.parsed(sourceFile, cu);
                        return (SourceFile) cu;
                    } catch (Throwable t) {
                        sample.stop(MetricsHelper.errorTags(timer, t).register(Metrics.globalRegistry));
                        return ParseError.build(this, sourceFile, relativeTo, pctx, t);
                    }
                });
    }

    @Override
    public boolean accept(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String extension : ASSEMBLER_FILE_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return !ImsParser.isGenSource(path);
            }
        }
        return false;
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.asm");
    }

    /**
     * A member of a macro library, which is where a name a {@code COPY} or a macro invocation reaches
     * is looked for.
     */
    public static boolean isMacroLibraryMember(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".mac");
    }

    public static AssemblerParser.Builder builder() {
        return new AssemblerParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(Assembler.CompilationUnit.class);
        }

        @Override
        public AssemblerParser build() {
            return new AssemblerParser();
        }

        @Override
        public String getDslName() {
            return "assembler";
        }
    }

}
