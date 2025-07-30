/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.antlr.v4.runtime.*;
import org.openrewrite.*;
import org.openrewrite.Parser;
import org.openrewrite.cobol.internal.CobolDialect;
import org.openrewrite.cobol.internal.CobolPreprocessorParserVisitor;
import org.openrewrite.cobol.internal.grammar.CobolPreprocessorLexer;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Read preprocessed COBOL and execute preprocessor commands.
 */
public class CobolPreprocessorParser implements Parser {
    private static final List<String> COBOL_FILE_EXTENSIONS = singletonList(".cbl");

    private final CobolDialect cobolDialect;
    private List<SourceFile> copybooks;

    public CobolPreprocessorParser(CobolDialect cobolDialect,
                                   List<SourceFile> copybooks) {
        this.cobolDialect = cobolDialect;
        this.copybooks = copybooks;
    }

    @Override
    public Stream<SourceFile> parseInputs(Iterable<org.openrewrite.Parser.Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();
        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a COBOL file")
                            .tag("file.type", "COBOL");
                    Timer.Sample sample = Timer.start();
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();

                        String prepareSource = new CobolLineReader().readLines(sourceStr, cobolDialect);
                        org.openrewrite.cobol.internal.grammar.CobolPreprocessorParser parser =
                                new org.openrewrite.cobol.internal.grammar.CobolPreprocessorParser(
                                        new CommonTokenStream(new CobolPreprocessorLexer(CharStreams.fromString(prepareSource))));
                        parser.removeErrorListeners();
                        parser.addErrorListener(new ForwardingErrorListener(sourceFile.getPath(), ctx));

                        CobolPreprocessorParserVisitor parserVisitor = new CobolPreprocessorParserVisitor(
                                sourceFile.getRelativePath(relativeTo),
                                sourceFile.getFileAttributes(),
                                sourceStr,
                                is.getCharset(),
                                is.isCharsetBomMarked(),
                                cobolDialect
                        );

                        CobolPreprocessor.CompilationUnit preprocessedCU = parserVisitor.visitCompilationUnit(parser.compilationUnit());

                        PreprocessCopyVisitor<ExecutionContext> copyPhase = new PreprocessCopyVisitor<>(preprocessedCU.getPreprocessorStatements(), copybooks);

                        // CU after copy includes the copied source.
                        preprocessedCU = (CobolPreprocessor.CompilationUnit) copyPhase.visit(preprocessedCU, new InMemoryExecutionContext());
                        assert preprocessedCU != null;

                        PreprocessReplaceVisitor<ExecutionContext> replacePhase = new PreprocessReplaceVisitor<>(preprocessedCU.getReplacements());

                        // CU after replace has replaced words in each ReplaceArea based on the ReplaceClause.
                        preprocessedCU = (CobolPreprocessor.CompilationUnit) replacePhase.visit(preprocessedCU, new InMemoryExecutionContext());
                        assert preprocessedCU != null;

                        sample.stop(MetricsHelper.successTags(timer).register(Metrics.globalRegistry));
                        parsingListener.parsed(sourceFile, preprocessedCU);
                        return preprocessedCU;
                    } catch (Throwable t) {
                        sample.stop(MetricsHelper.errorTags(timer, t).register(Metrics.globalRegistry));
                        ctx.getOnError().accept(t);
                        return ParseError.build(this, sourceFile, relativeTo, ctx, t);
                    }
                });
    }

    public void setCopybooks(List<SourceFile> copybooks) {
        this.copybooks = copybooks;
    }

    @Override
    public boolean accept(Path path) {
        String s = path.toString().toLowerCase();
        for (String COBOL_FILE_EXTENSION : COBOL_FILE_EXTENSIONS) {
            if (s.endsWith(COBOL_FILE_EXTENSION)) {
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

    public static CobolPreprocessorParser.Builder builder() {
        return new CobolPreprocessorParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {
        private CobolDialect cobolDialect = CobolDialect.ibmAnsi85();
        private List<SourceFile> copybooks = emptyList();

        public Builder() {
            super(Cobol.CompilationUnit.class);
        }

        @Override
        public CobolPreprocessorParser build() {
            return new CobolPreprocessorParser(
                    cobolDialect,
                    copybooks
            );
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
        public String getDslName() {
            return "preprocessCobol";
        }
    }
}
