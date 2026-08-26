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
package org.openrewrite.ims;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.*;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.WrongLanguageException;
import org.openrewrite.controlcard.ControlCards;
import org.openrewrite.ims.internal.ImsParserVisitor;
import org.openrewrite.ims.internal.grammar.IMSLexer;
import org.openrewrite.ims.internal.grammar.IMSParser;
import org.openrewrite.ims.tree.Ims;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

/**
 * Reads the IMS gen libraries, which are one shape.
 * <p>
 * A DBD, a PSB, a format set and a stage 1 deck are all assembler macro source, so one line reader
 * and one LST serve all four and only the traits differ. One parser reads them so that what a member
 * gens is asked in one place: the extensions grow as the traits for each library land, and a reader
 * of plain assembler declines whatever {@link #isGenSource} claims.
 */
public class ImsParser implements Parser {
    /**
     * Compared case-insensitively.
     */
    public static final List<String> IMS_FILE_EXTENSIONS = singletonList(".dbd");

    /**
     * The operations that open a gen member. An IMS gen library is often kept as {@code .asm} —
     * Bank of Z writes its DBDs as {@code src/base/ims/DBD/*.asm} — so an assembler-named member is
     * typed by what it gens, and the HLASM reader declines what {@link #isGenSource} claims.
     */
    private static final Set<String> GEN_OPERATIONS = new HashSet<>(singletonList("DBD"));

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing an IMS gen file")
                            .tag("file.type", "IMS");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();
                        // The grammar reads any assembler-shaped text, so a member that gens nothing
                        // has to be refused before it.
                        if (!sourceFile.isSynthetic() &&
                            !GEN_OPERATIONS.contains(ImsLineReader.firstOperation(sourceStr))) {
                            throw new WrongLanguageException(sourceFile.getPath(),
                                    sourceFile.getPath() + " is not an IMS gen member: it opens with no DBD macro.", null);
                        }
                        String postProcess = ImsLineReader.readLines(sourceStr);
                        CommonTokenStream tokens = new CommonTokenStream(new IMSLexer(
                                CharStreams.fromString(postProcess)));
                        IMSParser parser = new IMSParser(tokens);

                        parser.removeErrorListeners();
                        parser.addErrorListener(new ForwardingErrorListener(sourceFile.getPath(), ctx));

                        Ims.CompilationUnit cu = new ImsParserVisitor(
                                path,
                                sourceFile.getFileAttributes(),
                                sourceStr,
                                is.getCharset(),
                                is.isCharsetBomMarked(),
                                tokens
                        ).visitCompilationUnit(parser.compilationUnit());

                        sample.stop(MetricsHelper.successTags(timer).register(Metrics.globalRegistry));
                        parsingListener.parsed(sourceFile, cu);
                        return cu;
                    } catch (Throwable t) {
                        sample.stop(MetricsHelper.errorTags(timer, t).register(Metrics.globalRegistry));
                        return ParseError.build(this, sourceFile, relativeTo, pctx, t);
                    }
                });
    }

    @Override
    public boolean accept(Path path) {
        return isGenSource(path);
    }

    /**
     * Whether this reader claims the member, which any reader of assembler source has to ask before
     * taking a file: a DBD kept as {@code .asm} is gen source and not a program.
     */
    public static boolean isGenSource(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        for (String extension : IMS_FILE_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return name.endsWith(".asm") && GEN_OPERATIONS.contains(ImsLineReader.firstOperation(ControlCards.head(path)));
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.dbd");
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;
        private final ExecutionContext ctx;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            ctx.getOnError().accept(new ImsParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e));
        }
    }

    public static ImsParser.Builder builder() {
        return new ImsParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(Ims.CompilationUnit.class);
        }

        @Override
        public ImsParser build() {
            return new ImsParser();
        }

        @Override
        public String getDslName() {
            return "ims";
        }
    }
}
