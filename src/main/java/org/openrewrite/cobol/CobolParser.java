/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import org.antlr.v4.runtime.*;
import org.openrewrite.Parser;
import org.openrewrite.*;
import org.openrewrite.cobol.internal.CobolDialect;
import org.openrewrite.cobol.internal.CobolParserVisitor;
import org.openrewrite.cobol.internal.CobolPreprocessorOutputSourcePrinter;
import org.openrewrite.cobol.internal.grammar.CobolLexer;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class CobolParser implements Parser {
    public static final List<String> COPYBOOK_FILE_EXTENSIONS = Collections.singletonList(".cpy");
    public static final List<String> COBOL_FILE_EXTENSIONS = singletonList(".cbl");

    private final CobolDialect cobolDialect;
    private final List<SourceFile> copybooks;

    public CobolParser(CobolDialect cobolDialect,
                       List<SourceFile> copybooks) {
        this.cobolDialect = cobolDialect;
        this.copybooks = copybooks;
    }

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        CobolPreprocessorParser cobolPreprocessorParser = CobolPreprocessorParser.builder()
                .cobolDialect(cobolDialect)
                .copybooks(copybooks)
                .build();

        return acceptedInputs(sourceFiles).map(s -> parseInput(s, relativeTo, ctx, cobolPreprocessorParser));
    }

    private SourceFile parseInput(Input input, @Nullable Path relativeTo, ExecutionContext ctx,
                                  CobolPreprocessorParser cobolPreprocessorParser) {
        ParsingEventListener parserListener = ParsingExecutionContextView.view(ctx).getParsingListener();
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
                            new CommonTokenStream(new CobolLexer(CharStreams.fromString(cobolParserOutput.getOut()))));

            parser.removeErrorListeners();
            parser.addErrorListener(new ForwardingErrorListener(input.getPath(), ctx));

            // Print the pre-processed code to parse COBOL.
            PrintOutputCapture<ExecutionContext> sourceOutput = new PrintOutputCapture<>(new InMemoryExecutionContext());
            CobolPreprocessorOutputSourcePrinter<ExecutionContext> printWithColumns = new CobolPreprocessorOutputSourcePrinter<>(cobolDialect, true);
            printWithColumns.visit(preprocessedCU, sourceOutput);

            Cobol.CompilationUnit compilationUnit = new CobolParserVisitor(
                    input.getRelativePath(relativeTo),
                    input.getFileAttributes(),
                    sourceOutput.getOut(),
                    is.getCharset(),
                    is.isCharsetBomMarked(),
                    cobolDialect,
                    ((CobolPreprocessor.CompilationUnit) preprocessedCU).getPreprocessorStatements(),
                    ((CobolPreprocessor.CompilationUnit) preprocessedCU).getReplacements()
            ).visitCompilationUnit(parser.compilationUnit());

            parserListener.parsed(input, compilationUnit);
            return compilationUnit;
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

    private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;
        private final ExecutionContext ctx;

        private ForwardingErrorListener(Path sourcePath, ExecutionContext ctx) {
            this.sourcePath = sourcePath;
            this.ctx = ctx;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            ctx.getOnError().accept(new CobolParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e));
        }
    }

    public static CobolParser.Builder builder() {
        return new CobolParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        private CobolDialect cobolDialect = CobolDialect.ibmAnsi85();
        private List<SourceFile> copybooks = emptyList();

        public Builder() {
            super(Cobol.CompilationUnit.class);
        }

        @Override
        public CobolParser build() {
            return new CobolParser(
                    cobolDialect,
                    copybooks);
        }

        public Builder setCobolDialect(CobolDialect cobolDialect) {
            this.cobolDialect = cobolDialect;
            return this;
        }

        public Builder copybooks(List<SourceFile> copybooks) {
            this.copybooks = copybooks;
            return this;
        }

        @Override
        public String getDslName() {
            return "cobol";
        }
    }
}
