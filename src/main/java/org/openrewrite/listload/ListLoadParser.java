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
package org.openrewrite.listload;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.WrongLanguageException;
import org.openrewrite.controlcard.ControlCards;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.listload.tree.ListLoad;
import org.openrewrite.marker.Markers;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.openrewrite.Tree.randomId;

public class ListLoadParser implements Parser {
    /**
     * Compared case-insensitively.
     */
    public static final List<String> LIST_LOAD_FILE_EXTENSIONS =
            Arrays.asList(".amblist", ".binder", ".listload");

    /**
     * The extensions a shop's control card library uses, where an AMBLIST request deck sits beside the
     * sort cards, the IDCAMS cards and the parm cards it is not.
     */
    private static final List<String> CONTROL_CARD_FILE_EXTENSIONS = Arrays.asList(".ctl", ".prm");

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a load module listing")
                            .tag("file.type", "ListLoad");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();
                        if (!sourceFile.isSynthetic() && !ListLoadLineReader.isModuleListing(sourceStr)) {
                            throw new WrongLanguageException(sourceFile.getPath(),
                                    sourceFile.getPath() + " is not a load module listing: it has no AMBLIST or binder heading and asks for no report.", null);
                        }
                        ListLoad.CompilationUnit cu = new ListLoad.CompilationUnit(
                                randomId(),
                                path,
                                sourceFile.getFileAttributes(),
                                Markers.EMPTY,
                                is.getCharset().name(),
                                is.isCharsetBomMarked(),
                                null,
                                ListLoadLineReader.readLines(sourceStr)
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
        for (String extension : LIST_LOAD_FILE_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        // A control card library member and a PDS member kept without an extension are known by
        // nothing but what they ask AMBLIST for.
        return ControlCards.accept(path, CONTROL_CARD_FILE_EXTENSIONS, ListLoadLineReader::isModuleListing);
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.amblist");
    }

    public static ListLoadParser.Builder builder() {
        return new ListLoadParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(ListLoad.CompilationUnit.class);
        }

        @Override
        public ListLoadParser build() {
            return new ListLoadParser();
        }

        @Override
        public String getDslName() {
            return "listLoad";
        }
    }
}
