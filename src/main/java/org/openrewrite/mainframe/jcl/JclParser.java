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
package org.openrewrite.mainframe.jcl;

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
import org.openrewrite.mainframe.cobol.WrongLanguageException;
import org.openrewrite.internal.EncodingDetectingInputStream;
import org.openrewrite.internal.MetricsHelper;
import org.openrewrite.mainframe.jcl.internal.JclParserVisitor;
import org.openrewrite.mainframe.jcl.internal.grammar.JCLLexer;
import org.openrewrite.mainframe.jcl.internal.grammar.JCLParser;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.tree.ParseError;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

@AllArgsConstructor
public class JclParser implements Parser {
    /**
     * Compared case-insensitively. A member kept without an extension, or as {@code .txt} the way
     * a PDS is copied off as text, is accepted by its first card instead — see
     * {@link JclLineReader#isJcl}.
     */
    public static final List<String> JCL_FILE_EXTENSIONS = Arrays.asList(".jcl", ".prc", ".proc");

    /**
     * Control card members — the {@code .ctl} and {@code .prm} files a job's SYSIN names by member.
     * They are not sources of their own; they are supplied as {@link Builder#parmMembers} and
     * grafted into the DD that names them.
     */
    public static final List<String> CONTROL_CARD_EXTENSIONS = Arrays.asList(".ctl", ".prm");

    /**
     * Paths to external PDS members (e.g. {@code .prm} files) referenced by SYSIN/SYSTSIN and
     * other input control DD statements, supplied out-of-band and resolved by member name (the
     * file name without its extension). Passing paths rather than parsed sources lets the parser
     * read and tokenize each member exactly once, then reuse that across all JCL sources.
     */
    private final List<Path> parmMembers;

    /**
     * Paths to the members a job's {@code EXEC} and {@code INCLUDE} statements name — the
     * procedures and INCLUDE groups of the procedure library, supplied out-of-band and resolved by
     * member name (the file name without its extension). Anything {@link #accept} takes is a
     * candidate, so in a portfolio checked out whole this is simply every JCL member in it.
     */
    private final List<Path> procedureLibrary;

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        ParsingExecutionContextView pctx = ParsingExecutionContextView.view(ctx);
        ParsingEventListener parsingListener = pctx.getParsingListener();
        Stream<Input> accepted = acceptedInputs(sourceFiles);

        // Read and tokenize the external members once, then reuse the same expander across every
        // JCL source rather than rebuilding it (and re-tokenizing every member) per source.
        ExpandExternalSysinVisitor<ExecutionContext> sysinExpander = parmMembers.isEmpty() ? null :
                new ExpandExternalSysinVisitor<>(readParmMembers(parmMembers, ctx));
        Map<Path, Jcl.CompilationUnit> library = procedureLibrary.isEmpty() ? emptyMap() :
                readProcedureLibrary(procedureLibrary, ctx);
        ExpandJobVisitor<ExecutionContext> jobExpander = library.isEmpty() ? null :
                new ExpandJobVisitor<>(byMemberName(library));

        return accepted
                .map(sourceFile -> {
                    Timer.Builder timer = Timer.builder("rewrite.parse")
                            .description("The time spent parsing a JCL file")
                            .tag("file.type", "JCL");
                    Timer.Sample sample = Timer.start();
                    Path path = sourceFile.getRelativePath(relativeTo);
                    try {
                        // A member of the library was read for the expansion already; as a source of
                        // its own it is the same tree under its path from the root.
                        Jcl.CompilationUnit cu = library.get(sourceFile.getPath());
                        if (cu != null) {
                            cu = cu.withSourcePath(path);
                        } else {
                            cu = read(sourceFile, path, ctx);
                        }

                        if (sysinExpander != null) {
                            cu = sysinExpander.visitCompilationUnit(cu, ctx);
                        }
                        if (jobExpander != null) {
                            cu = jobExpander.visitCompilationUnit(cu, ctx);
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

    private Jcl.CompilationUnit read(Input sourceFile, Path path, ExecutionContext ctx) {
        EncodingDetectingInputStream is = sourceFile.getSource(ctx);
        String sourceStr = is.readFully();
        // The grammar reads anything, so a member that is not JCL has to be refused before it, or
        // it would be read as a job of unknown cards.
        if (!sourceFile.isSynthetic() && !JclLineReader.hasJcl(sourceStr)) {
            throw new WrongLanguageException(sourceFile.getPath(),
                    sourceFile.getPath() + " is not JCL: no card begins with //.", null);
        }
        String postProcess = JclLineReader.readLines(sourceStr);
        CommonTokenStream tokens = new CommonTokenStream(new JCLLexer(CharStreams.fromString(postProcess)));
        JCLParser parser = new JCLParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new ForwardingErrorListener(sourceFile.getPath(), ctx));

        return new JclParserVisitor(
                path,
                sourceFile.getFileAttributes(),
                sourceStr,
                is.getCharset(),
                is.isCharsetBomMarked(),
                tokens
        ).visitCompilationUnit(parser.compilationUnit());
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
            try (InputStream in = Files.newInputStream(path)) {
                members.putIfAbsent(memberName(path), new EncodingDetectingInputStream(in).readFully());
            } catch (IOException e) {
                ctx.getOnError().accept(e);
            }
        }
        return members;
    }

    /**
     * Parses each procedure library member once, in the order given, the first member of a name
     * standing for it. A member that does not parse is reported and left out, so one bad member
     * costs the procedures it holds rather than the whole portfolio. The parser used has no library
     * of its own: what a member of it in turn refers to is resolved by {@link ExpandJobVisitor} as it
     * expands, not by parsing it twice.
     */
    private static Map<Path, Jcl.CompilationUnit> readProcedureLibrary(List<Path> paths, ExecutionContext ctx) {
        Map<Path, Jcl.CompilationUnit> members = new LinkedHashMap<>();
        Set<String> names = new HashSet<>();
        JclParser parser = new JclParser(emptyList(), emptyList());
        for (Path path : paths) {
            if (!names.add(memberName(path))) {
                continue;
            }
            parser.parseInputs(singletonList(new Parser.Input(path, () -> {
                try {
                    return Files.newInputStream(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })), null, ctx).forEach(parsed -> {
                if (parsed instanceof Jcl.CompilationUnit) {
                    members.put(path, (Jcl.CompilationUnit) parsed);
                }
            });
        }
        return members;
    }

    private static Map<String, Jcl.CompilationUnit> byMemberName(Map<Path, Jcl.CompilationUnit> library) {
        Map<String, Jcl.CompilationUnit> members = new HashMap<>();
        library.forEach((path, cu) -> members.put(memberName(path), cu));
        return members;
    }

    /**
     * The member name a data set reference resolves against: the file name without its extension.
     */
    private static String memberName(Path path) {
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return (dot < 0 ? fileName : fileName.substring(0, dot)).toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean accept(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        // A Jinja template of a job is still a job, and Bank-of-Z ships its whole installation that
        // way. What is left after dropping the .j2 decides.
        if (name.endsWith(".j2")) {
            name = name.substring(0, name.length() - 3);
        }
        for (String extension : JCL_FILE_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return (name.indexOf('.') < 0 || name.endsWith(".txt")) && Files.isRegularFile(path) &&
               JclLineReader.isJcl(head(path));
    }

    private static String head(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            byte[] bytes = new byte[4096];
            int read = in.read(bytes);
            return read < 0 ? "" : new String(bytes, 0, read, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            return "";
        }
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
        private List<Path> procedureLibrary = emptyList();

        public Builder() {
            super(Jcl.CompilationUnit.class);
        }

        public Builder parmMembers(List<Path> parmMembers) {
            this.parmMembers = parmMembers;
            return this;
        }

        public Builder procedureLibrary(List<Path> procedureLibrary) {
            this.procedureLibrary = procedureLibrary;
            return this;
        }

        @Override
        public JclParser build() {
            return new JclParser(parmMembers, procedureLibrary);
        }

        @Override
        public String getDslName() {
            return "jcl";
        }
    }
}
