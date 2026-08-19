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
package org.openrewrite.jcl;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.*;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.jcl.internal.JclParserVisitor;
import org.openrewrite.jcl.internal.grammar.JCLLexer;
import org.openrewrite.jcl.internal.grammar.JCLParser;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@AllArgsConstructor
public class JclParser implements Parser {

    /**
     * Paths to external PDS members (e.g. {@code .prm} files) referenced by SYSIN/SYSTSIN and
     * other input control DD statements, supplied out-of-band and resolved by member name (the
     * file name without its extension). Passing paths rather than parsed sources lets the parser
     * read and tokenize each member exactly once, then reuse that across all JCL sources.
     */
    private final List<Path> parmMembers;

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();
        Stream<Input> accepted = acceptedInputs(sourceFiles);

        // Read and tokenize the external members once, then reuse the same expander across every
        // JCL source rather than rebuilding it (and re-tokenizing every member) per source.
        ExpandExternalSysinVisitor<ExecutionContext> sysinExpander = parmMembers.isEmpty() ? null :
                new ExpandExternalSysinVisitor<>(readParmMembers(parmMembers, ctx));

        return accepted
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a JCL file")
                            .tag("file.type", "JCL");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
                        String sourceStr = is.readFully();
                        String postProcess = JclLineReader.readLines(sourceStr);
                        CommonTokenStream tokens = new CommonTokenStream(new JCLLexer(
                                CharStreams.fromString(postProcess)));
                        JCLParser parser = new JCLParser(tokens);

                        parser.removeErrorListeners();
                        parser.addErrorListener(new ForwardingErrorListener(sourceFile.getPath(), ctx));

                        Jcl.CompilationUnit cu = new JclParserVisitor(
                                path,
                                sourceFile.getFileAttributes(),
                                sourceStr,
                                is.getCharset(),
                                is.isCharsetBomMarked(),
                                tokens
                        ).visitCompilationUnit(parser.compilationUnit());

                        if (sysinExpander != null) {
                            cu = sysinExpander.visitCompilationUnit(cu, ctx);
                        }

                        sample.stop(MetricsHelper.successTags(timer).register(Metrics.globalRegistry));
                        parsingListener.parsed(sourceFile, cu);
                        return cu;
                    } catch (Throwable t) {
                        sample.stop(MetricsHelper.errorTags(timer, t).register(Metrics.globalRegistry));
                        return ParseError.build(this, sourceFile, relativeTo, pctx, t);
                    }
                });
    }

    /**
     * Reads each member path once into a member-name-keyed map of raw content. The member name is
     * the file name without its extension (e.g. {@code MGSLAP8F.prm} resolves a DD referencing
     * {@code dataset(MGSLAP8F)}), matched case-insensitively. Unreadable members are reported to
     * the context and skipped so a single bad member does not fail the whole parse.
     */
    private static Map<String, String> readParmMembers(List<Path> paths, ExecutionContext ctx) {
        Map<String, String> members = new HashMap<>();
        for (Path path : paths) {
            String fileName = path.getFileName().toString();
            int dot = fileName.indexOf('.');
            String key = (dot < 0 ? fileName : fileName.substring(0, dot)).toUpperCase(Locale.ROOT);
            try (InputStream in = Files.newInputStream(path)) {
                members.putIfAbsent(key, new EncodingDetectingInputStream(in).readFully());
            } catch (IOException e) {
                ctx.getOnError().accept(e);
            }
        }
        return members;
    }

    @Override
    public boolean accept(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        // A Jinja template of a job is still a job. Bank-of-Z ships its whole installation this way
        // and creates every one of its DB2 tables from a templated member, so refusing these leaves
        // an application's schema invisible. A template is named for what it produces, so what is
        // left after dropping the .j2 decides: a .xml.j2 or .yaml.j2 is not JCL, and a member with
        // no extension at all is exactly how a PDS holds one.
        if (name.endsWith(".j2")) {
            name = name.substring(0, name.length() - 3);
            return !name.contains(".") || name.endsWith(".jcl") || name.endsWith(".prc");
        }
        return name.endsWith(".jcl") || name.endsWith(".prc");
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.jcl");
    }

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static class ForwardingErrorListener extends BaseErrorListener {
        private final Path sourcePath;
        private final ExecutionContext ctx;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            ctx.getOnError().accept(new JclParsingException(sourcePath,
                    String.format("Syntax error in %s at line %d:%d %s.", sourcePath, line, charPositionInLine, msg), e));
        }
    }

    public static JclParser.Builder builder() {
        return new JclParser.Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {
        private List<Path> parmMembers = emptyList();

        public Builder() {
            super(Jcl.CompilationUnit.class);
        }

        public Builder parmMembers(List<Path> parmMembers) {
            this.parmMembers = parmMembers;
            return this;
        }

        @Override
        public JclParser build() {
            return new JclParser(parmMembers);
        }

        @Override
        public String getDslName() {
            return "jcl";
        }
    }
}
