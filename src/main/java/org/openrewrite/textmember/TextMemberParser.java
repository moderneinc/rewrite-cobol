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
package org.openrewrite.textmember;

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
import org.openrewrite.textmember.tree.TextMember;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.openrewrite.Tree.randomId;

/**
 * What every reader of a member held as lines does, which is the same for all of them: keep the lines,
 * say what kind of member it is, and take the members whose extension its own kind claims.
 * <p>
 * There is one reader per kind rather than one for all five, because a reader is what an ingestion
 * selects a file with and what a repository's language composition is counted from. A single reader
 * over all five extensions would type a C source and a run book member alike.
 */
public abstract class TextMemberParser implements Parser {

    /**
     * What this reader's members are. It rides on the source file, so a recipe reading one knows what
     * it is holding without going back to the path.
     */
    public abstract TextMember.Kind getKind();

    /**
     * The extensions this reader claims, compared case-insensitively.
     */
    public abstract List<String> getExtensions();

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent reading a member held as lines")
                            .tag("file.type", getKind().name());
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        TextMember.CompilationUnit cu = parse(path, sourceFile.getFileAttributes(),
                                is.readFully(), is.getCharset(), is.isCharsetBomMarked());

                        sample.stop(MetricsHelper.successTags(timer).register(Metrics.globalRegistry));
                        parsingListener.parsed(sourceFile, cu);
                        return (SourceFile) cu;
                    } catch (Throwable t) {
                        sample.stop(MetricsHelper.errorTags(timer, t).register(Metrics.globalRegistry));
                        return ParseError.build(this, sourceFile, relativeTo, pctx, t);
                    }
                });
    }

    private TextMember.CompilationUnit parse(Path sourcePath, @Nullable FileAttributes fileAttributes,
                                             String source, Charset charset, boolean charsetBomMarked) {
        return new TextMember.CompilationUnit(
                randomId(),
                sourcePath,
                fileAttributes,
                Markers.EMPTY,
                charset.name(),
                charsetBomMarked,
                null,
                getKind(),
                TextMemberLineReader.readLines(source)
        );
    }

    @Override
    public boolean accept(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String extension : getExtensions()) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file" + getExtensions().get(0));
    }
}
