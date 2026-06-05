/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.cobol;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.Parser;
import org.openrewrite.cobol.internal.CobolDialect;
import org.openrewrite.cobol.internal.CobolParserVisitor;
import org.openrewrite.cobol.internal.CobolPreprocessorOutputSourcePrinter;
import org.openrewrite.cobol.internal.grammar.CobolLexer;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class CobolParser implements Parser {
    public static final List<String> COPYBOOK_FILE_EXTENSIONS = Arrays.asList(".cpy", ".dcl");
    public static final List<String> COBOL_FILE_EXTENSIONS = singletonList(".cbl");

    private final CobolDialect cobolDialect;
    private final List<SourceFile> copybooks;
    private final Duration timeout;

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        CobolPreprocessorParser cobolPreprocessorParser = CobolPreprocessorParser.builder()
                .cobolDialect(cobolDialect)
                .copybooks(copybooks)
                .build();

        ParsingEventListener parserListener = ParsingExecutionContextView.view(ctx).getParsingListener();
        return acceptedInputs(sourceFiles).map(s -> parseInput(s, relativeTo, ctx, cobolPreprocessorParser, parserListener));
    }

    private SourceFile parseInput(Input input, @Nullable Path relativeTo, ExecutionContext ctx,
                                  CobolPreprocessorParser cobolPreprocessorParser, ParsingEventListener parserListener) {
        try {
            parserListener.startedParsing(input);
            EncodingDetectingInputStream is = input.getSource(ctx);
            cobolPreprocessorParser.reset();
            SourceFile preprocessedCU = cobolPreprocessorParser.parseInputs(singletonList(input), relativeTo, ctx).collect(toList()).get(0);
            assert preprocessedCU != null;
            if (preprocessedCU instanceof ParseError) {
                return preprocessedCU;
            }

            // Print processed code to parse COBOL.
            PrintOutputCapture<ExecutionContext> cobolParserOutput = new PrintOutputCapture<>(new InMemoryExecutionContext());
            CobolPreprocessorOutputSourcePrinter<ExecutionContext> printWithoutColumns = new CobolPreprocessorOutputSourcePrinter<>(cobolDialect, false);
            printWithoutColumns.visit(preprocessedCU, cobolParserOutput);

            org.openrewrite.cobol.internal.grammar.CobolParser parser =
                    new org.openrewrite.cobol.internal.grammar.CobolParser(
                            new CommonTokenStream(new CobolLexer(CharStreams.fromString(cobolParserOutput.getOut())))) {{
                        _interp = new TimeLimitingParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
                    }};
            parser.removeErrorListeners();
            parser.addErrorListener(new ForwardingErrorListener(input.getPath()));

            // Print the pre-processed code to parse COBOL.
            PrintOutputCapture<ExecutionContext> sourceOutput = new PrintOutputCapture<>(new InMemoryExecutionContext());
            CobolPreprocessorOutputSourcePrinter<ExecutionContext> printWithColumns = new CobolPreprocessorOutputSourcePrinter<>(cobolDialect, true);
            printWithColumns.visit(preprocessedCU, sourceOutput);

            try {
                org.openrewrite.cobol.internal.grammar.CobolParser.CompilationUnitContext tokenizedCU = parser.compilationUnit();
                Cobol.CompilationUnit compilationUnit = new CobolParserVisitor(
                        input.getRelativePath(relativeTo),
                        input.getFileAttributes(),
                        sourceOutput.getOut(),
                        is.getCharset(),
                        is.isCharsetBomMarked(),
                        cobolDialect,
                        ((CobolPreprocessor.CompilationUnit) preprocessedCU).getPreprocessorStatements(),
                        ((CobolPreprocessor.CompilationUnit) preprocessedCU).getReplacements(),
                        timeout
                ).visitCompilationUnit(tokenizedCU);
                parserListener.parsed(input, compilationUnit);
                return compilationUnit;
            } catch (ParseCancellationException e) {
                throw new CobolParsingTimeoutException(relativeTo == null ? input.getPath() :
                        relativeTo.relativize(input.getPath()));
            }
        } catch (Throwable t) {
            ctx.getOnError().accept(t);
            return ParseError.build(this, input, relativeTo, ctx, t);
        }
    }

    @Override
    public boolean accept(Path path) {
        String s = path.toString().toLowerCase();
        for (String COBOL_FILE_EXTENSION : COBOL_FILE_EXTENSIONS) {
            if (s.endsWith(COBOL_FILE_EXTENSION)) {
                return true;
            }
        }
        for (String COPYBOOK_FILE_EXTENSION : COPYBOOK_FILE_EXTENSIONS) {
            if (s.endsWith(COPYBOOK_FILE_EXTENSION)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.CBL");
    }

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new CobolParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e);
        }
    }

    public static CobolParser.Builder builder() {
        return new CobolParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {
        private CobolDialect cobolDialect = CobolDialect.ibmAnsi85();
        private List<SourceFile> copybooks = emptyList();
        private Duration timeout = Duration.ofSeconds(10);

        public Builder() {
            super(Cobol.CompilationUnit.class);
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder cobolDialect(CobolDialect cobolDialect) {
            this.cobolDialect = cobolDialect;
            return this;
        }

        public Builder copybooks(List<SourceFile> copybooks) {
            this.copybooks = copybooks;
            return this;
        }

        @Override
        public CobolParser build() {
            return new CobolParser(cobolDialect, copybooks, timeout);
        }

        @Override
        public String getDslName() {
            return "cobol";
        }
    }

    private class TimeLimitingParserATNSimulator extends ParserATNSimulator {
        private final Instant start = Instant.now();

        public TimeLimitingParserATNSimulator(org.antlr.v4.runtime.Parser parser, ATN atn, DFA[] decisionToDFA,
                                              PredictionContextCache sharedContextCache) {
            super(parser, atn, decisionToDFA, sharedContextCache);
        }

        @Override
        protected void closure(ATNConfig config,
                               ATNConfigSet configs,
                               Set<ATNConfig> closureBusy,
                               boolean collectPredicates,
                               boolean fullCtx,
                               boolean treatEofAsEpsilon) {
            Duration timeElapsed = Duration.between(start, Instant.now());
            if (timeElapsed.compareTo(timeout) > 0) {
                throw new ParseCancellationException();
            }
            super.closure(config, configs, closureBusy, collectPredicates, fullCtx, treatEofAsEpsilon);
        }
    }
}
