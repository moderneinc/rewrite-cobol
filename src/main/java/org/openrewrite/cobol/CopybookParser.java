/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.internal.CobolDialect;
import org.openrewrite.cobol.internal.CobolPreprocessorParserVisitor;
import org.openrewrite.cobol.internal.grammar.CobolPreprocessorLexer;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Space;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;
import org.openrewrite.text.PlainText;
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
import static org.openrewrite.Tree.randomId;

/**
 * Read preprocessed COBOL and execute preprocessor commands.
 */
public class CopybookParser implements Parser {
    public static final List<String> COPYBOOK_FILE_EXTENSIONS = Collections.singletonList(".cpy");

    private final CobolDialect cobolDialect;

    public CopybookParser(CobolDialect cobolDialect) {
        this.cobolDialect = cobolDialect;
    }

    public Stream<SourceFile> parse(Stream<Path> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        return sourceFiles.filter(this::accept).map(s -> parseInput(new Input(s, () -> {
            try {
                return new BufferedInputStream(Files.newInputStream(s));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }), relativeTo, ctx));
    }

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        return acceptedInputs(sourceFiles).map(input -> parseInput(input, relativeTo, ctx));
    }

    private SourceFile parseInput(Input input, @Nullable Path relativeTo, ExecutionContext ctx) {
        try {
            EncodingDetectingInputStream is = input.getSource(ctx);
            String sourceStr = is.readFully();

            PlainText plainText = new PlainText(
                    randomId(),
                    input.getPath(),
                    Markers.EMPTY,
                    is.getCharset().name(),
                    is.isCharsetBomMarked(),
                    null,
                    null,
                    sourceStr,
                    emptyList()
            );

            String prepareSource = new CobolLineReader().readLines(sourceStr, cobolDialect);
            org.openrewrite.cobol.internal.grammar.CobolPreprocessorParser parser =
                    new org.openrewrite.cobol.internal.grammar.CobolPreprocessorParser(
                            new CommonTokenStream(new CobolPreprocessorLexer(CharStreams.fromString(prepareSource))));

            CobolPreprocessorParserVisitor parserVisitor = new CobolPreprocessorParserVisitor(
                    input.getRelativePath(relativeTo),
                    input.getFileAttributes(),
                    sourceStr,
                    is.getCharset(),
                    is.isCharsetBomMarked(),
                    cobolDialect
            );

            CobolPreprocessor.CompilationUnit preprocessedCU = parserVisitor.visitCompilationUnit(parser.compilationUnit());
            List<CobolPreprocessor> parsedCopySource = preprocessedCU.getCobols();

            CobolPreprocessor.Copybook copybook = new CobolPreprocessor.Copybook(
                    randomId(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    plainText.getSourcePath(),
                    null,
                    plainText.getCharsetName(),
                    plainText.isCharsetBomMarked(),
                    null,
                    parsedCopySource,
                    preprocessedCU.getEof()
            );

            ParsingExecutionContextView.view(ctx).getParsingListener().parsed(input, preprocessedCU);
            return copybook;
        } catch (Throwable t) {
            ctx.getOnError().accept(t);
            return ParseError.build(this, input, relativeTo, ctx, t);
        }
    }

    @Override
    public Stream<SourceFile> parse(String... sources) {
        return parse(new InMemoryExecutionContext(), sources);
    }

    @Override
    public boolean accept(Path path) {
        String s = path.toString().toLowerCase();
        for (String COBOL_FILE_EXTENSION : COPYBOOK_FILE_EXTENSIONS) {
            if (s.endsWith(COBOL_FILE_EXTENSION)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.CPY");
    }

    public static CopybookParser.Builder builder() {
        return new CopybookParser.Builder();
    }

    public static class Builder extends Parser.Builder {

        private CobolDialect cobolDialect = CobolDialect.ibmAnsi85();

        public Builder() {
            super(Cobol.CompilationUnit.class);
        }

        @Override
        public CopybookParser build() {
            return new CopybookParser(cobolDialect);
        }

        public Builder setCobolDialect(CobolDialect cobolDialect) {
            this.cobolDialect = cobolDialect;
            return this;
        }

        @Override
        public String getDslName() {
            return "copybook";
        }
    }
}
