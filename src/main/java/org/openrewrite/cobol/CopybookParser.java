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
import org.jspecify.annotations.Nullable;
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
import org.openrewrite.marker.Markers;
import org.openrewrite.text.PlainText;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.openrewrite.Tree.randomId;

/**
 * Read preprocessed COBOL and execute preprocessor commands.
 */
@RequiredArgsConstructor
public class CopybookParser implements Parser {
    public static final List<String> COPYBOOK_FILE_EXTENSIONS = Arrays.asList(".cpy", ".dcl");

    private final CobolDialect cobolDialect;

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingEventListener parsingListener = ParsingExecutionContextView.view(ctx).getParsingListener();
        return acceptedInputs(sourceFiles).map(input -> parseInput(input, relativeTo, ctx, parsingListener));
    }

    private SourceFile parseInput(Input input, @Nullable Path relativeTo, ExecutionContext ctx, ParsingEventListener parsingListener) {
        try {
            parsingListener.startedParsing(input);
            EncodingDetectingInputStream is = input.getSource(ctx);
            String sourceStr = is.readFully();

            PlainText plainText = new PlainText(
                    randomId(),
                    input.getRelativePath(relativeTo),
                    Markers.EMPTY,
                    is.getCharset().name(),
                    is.isCharsetBomMarked(),
                    null,
                    null,
                    sourceStr,
                    emptyList()
            );

            // A copybook has no SOURCE-COMPUTER paragraph, so it cannot say whether debugging lines
            // are live. The including program decides; here they are read as code, as before.
            String prepareSource = new CobolLineReader().readLines(sourceStr, cobolDialect, false);

            CobolPreprocessorLexer lexer = new CobolPreprocessorLexer(CharStreams.fromString(prepareSource));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new ForwardingErrorListener(input.getPath()));

            org.openrewrite.cobol.internal.grammar.CobolPreprocessorParser parser =
                    new org.openrewrite.cobol.internal.grammar.CobolPreprocessorParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(new ForwardingErrorListener(input.getPath()));

            CobolPreprocessorParserVisitor parserVisitor = new CobolPreprocessorParserVisitor(
                    input.getRelativePath(relativeTo),
                    input.getFileAttributes(),
                    sourceStr,
                    is.getCharset(),
                    is.isCharsetBomMarked(),
                    cobolDialect,
                    false
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

            parsingListener.parsed(input, preprocessedCU);
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

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new CopybookParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e);
        }
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
