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
package org.openrewrite.bms;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.*;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.bms.internal.BmsParserVisitor;
import org.openrewrite.bms.internal.grammar.BMSLexer;
import org.openrewrite.bms.internal.grammar.BMSParser;
import org.openrewrite.bms.tree.Bms;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.nio.file.Path;
import java.util.stream.Stream;

public class BmsParser implements Parser {

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();

        return acceptedInputs(sourceFiles)
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a BMS file")
                            .tag("file.type", "BMS");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();
                        String postProcess = BmsLineReader.readLines(sourceStr);
                        CommonTokenStream tokens = new CommonTokenStream(new BMSLexer(
                                CharStreams.fromString(postProcess)));
                        BMSParser parser = new BMSParser(tokens);

                        parser.removeErrorListeners();
                        parser.addErrorListener(new ForwardingErrorListener(sourceFile.getPath(), ctx));

                        Bms.CompilationUnit cu = new BmsParserVisitor(
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
        return path.toString().toLowerCase().endsWith(".bms");
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.bms");
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;
        private final ExecutionContext ctx;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            ctx.getOnError().accept(new BmsParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e));
        }
    }

    public static BmsParser.Builder builder() {
        return new BmsParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(Bms.CompilationUnit.class);
        }

        @Override
        public BmsParser build() {
            return new BmsParser();
        }

        @Override
        public String getDslName() {
            return "bms";
        }
    }
}
