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
package org.openrewrite.sas;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.FileAttributes;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.marker.Markers;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.sas.tree.Space;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.openrewrite.Tree.randomId;

/**
 * Reads the SAS library a shop keeps beside its COBOL: the reports an actuarial or finance group
 * runs over what the nightly batch stream left behind.
 * <p>
 * A SAS program has no name of its own the way a COBOL program has a {@code PROGRAM-ID}. The member
 * name is the only name it has, which is why the program a job writes in-stream — read by
 * {@link org.openrewrite.sas.trait.InstreamSas} — has none at all.
 */
public class SasParser implements Parser {
    /**
     * Compared case-insensitively.
     */
    public static final List<String> SAS_FILE_EXTENSIONS = unmodifiableList(singletonList(".sas"));

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a SAS member")
                            .tag("file.type", "SAS");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        Sas.CompilationUnit cu = parse(path, sourceFile.getFileAttributes(), is.readFully(),
                                is.getCharset(), is.isCharsetBomMarked());

                        sample.stop(MetricsHelper.successTags(timer).register(Metrics.globalRegistry));
                        parsingListener.parsed(sourceFile, cu);
                        return (SourceFile) cu;
                    } catch (Throwable t) {
                        sample.stop(MetricsHelper.errorTags(timer, t).register(Metrics.globalRegistry));
                        return ParseError.build(this, sourceFile, relativeTo, pctx, t);
                    }
                });
    }

    /**
     * A program that is not a member of its own: the one a job writes on a {@code SYSIN} stream,
     * which has the job's path and no member name.
     */
    public static Sas.CompilationUnit parse(Path sourcePath, String source) {
        return parse(sourcePath, null, source, StandardCharsets.UTF_8, false);
    }

    private static Sas.CompilationUnit parse(Path sourcePath, @Nullable FileAttributes fileAttributes,
                                             String source, Charset charset, boolean charsetBomMarked) {
        SasLineReader reader = new SasLineReader(source);
        return new Sas.CompilationUnit(
                randomId(),
                sourcePath,
                fileAttributes,
                Space.EMPTY,
                Markers.EMPTY,
                charset.name(),
                charsetBomMarked,
                null,
                reader.getStatements(),
                reader.getEof()
        );
    }

    @Override
    public boolean accept(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String extension : SAS_FILE_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.sas");
    }

    public static SasParser.Builder builder() {
        return new SasParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(Sas.CompilationUnit.class);
        }

        @Override
        public SasParser build() {
            return new SasParser();
        }

        @Override
        public String getDslName() {
            return "sas";
        }
    }
}
