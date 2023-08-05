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
import org.openrewrite.cobol.internal.CobolParserVisitor;
import org.openrewrite.cobol.internal.CobolPreprocessorOutputSourcePrinter;
import org.openrewrite.cobol.internal.grammar.CobolLexer;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class CobolParser implements Parser {
    public static final List<String> COPYBOOK_FILE_EXTENSIONS = Collections.singletonList(".cpy");
    public static final List<String> COBOL_FILE_EXTENSIONS = singletonList(".cbl");

    private final CobolDialect cobolDialect;
    private final List<SourceFile> copybooks;
    private final boolean enableCopy;
    private final boolean enableReplace;

    public CobolParser(CobolDialect cobolDialect,
                       List<SourceFile> copybooks,
                       boolean enableCopy,
                       boolean enableReplace) {
        this.cobolDialect = cobolDialect;
        this.copybooks = copybooks;
        this.enableCopy = enableCopy;
        this.enableReplace = enableReplace;
    }

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();
        List<Input> accepted = acceptedInputs(sourceFiles).collect(toList());
        List<Input> copybookInputs = new ArrayList<>();
        List<Input> cobolInputs = new ArrayList<>();

        for (Input input : accepted) {
            for (String cobolFileExtension : COBOL_FILE_EXTENSIONS) {
                if (input.getPath().getFileName().toString().toLowerCase().endsWith(cobolFileExtension)) {
                    cobolInputs.add(input);
                }
            }

            for (String copybookFileExtension : COPYBOOK_FILE_EXTENSIONS) {
                if (input.getPath().getFileName().toString().toLowerCase().endsWith(copybookFileExtension)) {
                    copybookInputs.add(input);
                }
            }
        }

        CopybookParser copybookParser = new CopybookParser(cobolDialect);
        List<SourceFile> copybooks = copybookParser.parseInputs(copybookInputs, relativeTo, ctx).collect(toList());

        CobolPreprocessorParser cobolPreprocessorParser = CobolPreprocessorParser.builder()
                .setCobolDialect(cobolDialect)
                .setEnableCopy(enableCopy)
                .setEnableReplace(enableReplace)
                .build();
        cobolPreprocessorParser.setCopybooks(!this.copybooks.isEmpty() ? this.copybooks : copybooks);

        Stream<SourceFile> sources;
        sources = cobolInputs.stream()
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a COBOL file")
                            .tag("file.type", "COBOL");
                    Timer.Sample sample = Timer.start();
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        cobolPreprocessorParser.reset();
                        SourceFile preprocessedCU = cobolPreprocessorParser.parseInputs(singletonList(sourceFile), relativeTo, ctx).collect(toList()).get(0);
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
                        parser.addErrorListener(new ForwardingErrorListener(sourceFile.getPath(), ctx));

                        // Print the pre-processed code to parse COBOL.
                        PrintOutputCapture<ExecutionContext> sourceOutput = new PrintOutputCapture<>(new InMemoryExecutionContext());
                        CobolPreprocessorOutputSourcePrinter<ExecutionContext> printWithColumns = new CobolPreprocessorOutputSourcePrinter<>(cobolDialect, true);
                        printWithColumns.visit(preprocessedCU, sourceOutput);

                        Cobol.CompilationUnit compilationUnit = new CobolParserVisitor(
                                sourceFile.getRelativePath(relativeTo),
                                sourceFile.getFileAttributes(),
                                sourceOutput.getOut(),
                                is.getCharset(),
                                is.isCharsetBomMarked(),
                                cobolDialect,
                                ((CobolPreprocessor.CompilationUnit) preprocessedCU).getPreprocessorStatements(),
                                ((CobolPreprocessor.CompilationUnit) preprocessedCU).getReplacements()
                        ).visitCompilationUnit(parser.compilationUnit());

                        sample.stop(MetricsHelper.successTags(timer).register(Metrics.globalRegistry));
                        parsingListener.parsed(sourceFile, compilationUnit);
                        return compilationUnit;
                    } catch (Throwable t) {
                        sample.stop(MetricsHelper.errorTags(timer, t).register(Metrics.globalRegistry));
                        pctx.getOnError().accept(t);
                        return ParseError.build(this, sourceFile, relativeTo, ctx, t);
                    }
                });

        return Stream.concat(sources, copybooks.stream());
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
        private boolean enableCopy = true;
        private boolean enableReplace = true;

        public Builder() {
            super(Cobol.CompilationUnit.class);
        }

        @Override
        public CobolParser build() {
            return new CobolParser(
                    cobolDialect,
                    copybooks,
                    enableCopy,
                    enableReplace);
        }

        public Builder setCobolDialect(CobolDialect cobolDialect) {
            this.cobolDialect = cobolDialect;
            return this;
        }

        @Deprecated
        public Builder setCopybooks(List<SourceFile> copybooks) {
            this.copybooks = copybooks;
            return this;
        }

        @Deprecated
        public Builder setEnableCopy(boolean enableCopy) {
            this.enableCopy = enableCopy;
            return this;
        }

        @Deprecated
        public Builder setEnableReplace(boolean enableReplace) {
            this.enableReplace = enableReplace;
            return this;
        }

        @Override
        public String getDslName() {
            return "cobol";
        }
    }
}
