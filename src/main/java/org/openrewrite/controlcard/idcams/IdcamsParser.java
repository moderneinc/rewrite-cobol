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
package org.openrewrite.controlcard.idcams;

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
import org.openrewrite.cobol.WrongLanguageException;
import org.openrewrite.controlcard.ControlCards;
import org.openrewrite.controlcard.idcams.internal.IdcamsParserVisitor;
import org.openrewrite.controlcard.idcams.internal.grammar.IdcamsCardLexer;
import org.openrewrite.controlcard.idcams.internal.grammar.IdcamsCardParser;
import org.openrewrite.controlcard.idcams.tree.Idcams;
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

public class IdcamsParser implements Parser {
    /**
     * Compared case-insensitively. A control card library is a PDS, so its members also reach a
     * repository with no extension at all; either way the deck is typed by what its first command
     * says and not by what it is called.
     */
    public static final List<String> IDCAMS_FILE_EXTENSIONS = asList(".ctl", ".prm");

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing an IDCAMS control card file")
                            .tag("file.type", "IDCAMS");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();
                        // The grammar reads any deck of cards, so a member that calls no access
                        // method service has to be refused before it.
                        if (!sourceFile.isSynthetic() && !IdcamsLineReader.isIdcamsDeck(sourceStr)) {
                            throw new WrongLanguageException(sourceFile.getPath(),
                                    sourceFile.getPath() + " is not an IDCAMS deck: it opens with no access method services command.", null);
                        }
                        Idcams.CompilationUnit cu = parse(path, sourceFile.getFileAttributes(), sourceStr,
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
    public static Idcams.CompilationUnit parse(Path sourcePath, String source) {
        return parse(sourcePath, null, source, StandardCharsets.UTF_8, false,
                new ForwardingErrorListener(sourcePath, new InMemoryExecutionContext()));
    }

    private static Idcams.CompilationUnit parse(Path path, @Nullable FileAttributes fileAttributes, String source,
                                                Charset charset, boolean charsetBomMarked,
                                                ANTLRErrorListener errorListener) {
        CommonTokenStream tokens = new CommonTokenStream(new IdcamsCardLexer(
                CharStreams.fromString(IdcamsLineReader.readLines(source))));
        IdcamsCardParser parser = new IdcamsCardParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return new IdcamsParserVisitor(
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
        return ControlCards.accept(path, IDCAMS_FILE_EXTENSIONS, IdcamsLineReader::isIdcamsDeck);
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("idcamscard.ctl");
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;
        private final ExecutionContext ctx;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            ctx.getOnError().accept(new IdcamsParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e));
        }
    }

    public static IdcamsParser.Builder builder() {
        return new IdcamsParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(Idcams.CompilationUnit.class);
        }

        @Override
        public IdcamsParser build() {
            return new IdcamsParser();
        }

        @Override
        public String getDslName() {
            return "idcams";
        }
    }
}
