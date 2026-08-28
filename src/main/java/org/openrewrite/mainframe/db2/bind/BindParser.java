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
package org.openrewrite.mainframe.db2.bind;

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
import org.openrewrite.mainframe.db2.bind.internal.BindParserVisitor;
import org.openrewrite.mainframe.db2.bind.internal.grammar.BindLexer;
import org.openrewrite.mainframe.db2.bind.tree.Bind;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public class BindParser implements Parser {
    /**
     * Compared case-insensitively.
     */
    public static final List<String> BIND_FILE_EXTENSIONS = singletonList(".bnd");

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a bind card file")
                            .tag("file.type", "Bind");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();
                        // The grammar reads any command deck, so a member named .bnd that binds
                        // nothing has to be refused before it.
                        if (!sourceFile.isSynthetic() && !BindLineReader.isBindDeck(sourceStr)) {
                            throw new WrongLanguageException(sourceFile.getPath(),
                                    sourceFile.getPath() + " is not a bind deck: it has no BIND or REBIND subcommand.", null);
                        }
                        Bind.CompilationUnit cu = parse(path, sourceFile.getFileAttributes(), sourceStr,
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
     * Reads a deck that has no file of its own — the in-stream data of a {@code SYSTSIN} DD, which is
     * held in the job's own LST and reaches here as the text it prints back to.
     */
    public static Bind.CompilationUnit parse(Path sourcePath, String source) {
        return parse(sourcePath, null, source, StandardCharsets.UTF_8, false,
                new ForwardingErrorListener(sourcePath, new InMemoryExecutionContext()));
    }

    private static Bind.CompilationUnit parse(Path path, @Nullable FileAttributes fileAttributes, String source,
                                              Charset charset, boolean charsetBomMarked,
                                              ANTLRErrorListener errorListener) {
        CommonTokenStream tokens = new CommonTokenStream(new BindLexer(
                CharStreams.fromString(BindLineReader.readLines(source))));
        org.openrewrite.mainframe.db2.bind.internal.grammar.BindParser parser =
                new org.openrewrite.mainframe.db2.bind.internal.grammar.BindParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return new BindParserVisitor(
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
        String name = path.getFileName().toString().toLowerCase();
        for (String extension : BIND_FILE_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        // A CARDLIB member is a PDS member, so it reaches a repository with no extension at all and
        // is known by nothing but its first subcommand.
        return name.indexOf('.') < 0 && Files.isRegularFile(path) &&
               BindLineReader.isBindDeck(ControlCards.head(path));
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.bnd");
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;
        private final ExecutionContext ctx;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            ctx.getOnError().accept(new BindParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e));
        }
    }

    public static BindParser.Builder builder() {
        return new BindParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(Bind.CompilationUnit.class);
        }

        @Override
        public BindParser build() {
            return new BindParser();
        }

        @Override
        public String getDslName() {
            return "bind";
        }
    }
}
