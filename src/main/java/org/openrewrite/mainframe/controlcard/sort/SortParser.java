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
package org.openrewrite.mainframe.controlcard.sort;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.*;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.FileAttributes;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.WrongLanguageException;
import org.openrewrite.mainframe.controlcard.ControlCards;
import org.openrewrite.mainframe.controlcard.sort.internal.SortParserVisitor;
import org.openrewrite.mainframe.controlcard.sort.internal.grammar.SortCardLexer;
import org.openrewrite.mainframe.controlcard.sort.internal.grammar.SortCardParser;
import org.openrewrite.mainframe.controlcard.sort.tree.Sort;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class SortParser implements Parser {
    /**
     * Compared case-insensitively. A control card library is a PDS, so its members also reach a
     * repository with no extension at all; either way the deck is typed by what its first statement
     * says and not by what it is called.
     */
    public static final List<String> SORT_FILE_EXTENSIONS = asList(".ctl", ".prm");

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a sort control card file")
                            .tag("file.type", "Sort");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();
                        // The grammar reads any deck of cards, so a member that sorts nothing has to
                        // be refused before it.
                        if (!sourceFile.isSynthetic() && !SortLineReader.isSortDeck(sourceStr)) {
                            throw new WrongLanguageException(sourceFile.getPath(),
                                    sourceFile.getPath() + " is not a sort deck: it opens with no DFSORT or ICETOOL statement.", null);
                        }
                        Sort.CompilationUnit cu = parse(path, sourceFile.getFileAttributes(), sourceStr,
                                is.getCharset(), is.isCharsetBomMarked(),
                                new ForwardingErrorListener(sourceFile.getPath(), ctx));

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
     * Reads a deck that has no file of its own — the in-stream data of a DD, which is held in the
     * job's own LST and reaches here as the text it prints back to.
     */
    public static Sort.CompilationUnit parse(Path sourcePath, String source) {
        return parse(sourcePath, null, source, StandardCharsets.UTF_8, false,
                new ForwardingErrorListener(sourcePath, new InMemoryExecutionContext()));
    }

    private static Sort.CompilationUnit parse(Path path, @Nullable FileAttributes fileAttributes, String source,
                                              Charset charset, boolean charsetBomMarked,
                                              ANTLRErrorListener errorListener) {
        CommonTokenStream tokens = new CommonTokenStream(new SortCardLexer(
                CharStreams.fromString(SortLineReader.readLines(source))));
        SortCardParser parser = new SortCardParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return new SortParserVisitor(
                path,
                fileAttributes,
                source,
                charset,
                charsetBomMarked,
                tokens
        ).visitCompilationUnit(parser.compilationUnit());
    }

    @Override
    public boolean accept(Path path) {
        return ControlCards.accept(path, SORT_FILE_EXTENSIONS, SortLineReader::isSortDeck);
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("sortcard.ctl");
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;
        private final ExecutionContext ctx;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            ctx.getOnError().accept(new SortParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e));
        }
    }

    public static SortParser.Builder builder() {
        return new SortParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(Sort.CompilationUnit.class);
        }

        @Override
        public SortParser build() {
            return new SortParser();
        }

        @Override
        public String getDslName() {
            return "sort";
        }
    }
}
