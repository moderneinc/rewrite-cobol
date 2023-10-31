package io.moderne.cobol;

import io.moderne.cobol.internal.ModerneColors;
import io.moderne.serialization.TreeSerializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.openrewrite.*;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolParser;
import org.openrewrite.cobol.CobolParsingTimeoutException;
import org.openrewrite.cobol.CopybookParser;
import org.openrewrite.cobol.marker.MissingCopybook;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.controlm.ControlMParser;
import org.openrewrite.marker.LstProvenance;
import org.openrewrite.polyglot.OmniParser;
import org.openrewrite.polyglot.ProgressBar;
import org.openrewrite.text.PlainTextParser;
import org.openrewrite.tree.ParsingEventListener;
import org.openrewrite.tree.ParsingExecutionContextView;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.file.Files.exists;
import static java.util.Objects.requireNonNull;
import static org.fusesource.jansi.Ansi.ansi;

@CommandLine.Command(name = "build")
@NoArgsConstructor
@AllArgsConstructor
public class Build implements Callable<Integer> {
    @CommandLine.Mixin
    ListRepositoryOptions listRepositoryOptions;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = "--timeoutSeconds",
            defaultValue = "10",
            description = "A per-file timeout in seconds for parsing.")
    protected int timeoutSeconds;

    @CommandLine.Option(names = "--validate",
            defaultValue = "false",
            description = "When enabled take extra time to validate that the contents of the jar can be successfully deserialized.")
    protected boolean validate;

    @Override
    public Integer call() {
        List<LocalRepository> repositories = new ListRepositories(listRepositoryOptions, spec).list();
        spec.commandLine().getOut().println(ansi().bold().a("> Building LST(s)\n").boldOff());
        build(repositories);
        return 0;
    }

    public List<LstJarFile> build(List<LocalRepository> repositories) {
        List<LstJarFile> lstJars = new ArrayList<>();

        TreeSerializer serializer = TreeSerializer.builder().build();

        CopybookParser copybookParser = CopybookParser.builder().build();
        List<Path> copybookSources = OmniParser.builder(copybookParser).build().acceptedPaths(
                listRepositoryOptions.getPath());

        List<SourceFile> copybooks;
        try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
            progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                    .a(" Parsing " + copybookSources.size() + " copybook files to use on all repositories.")
                    .toString());
            copybooks = copybookParser.parse(copybookSources, listRepositoryOptions.getPath(),
                    progressReportingExecutionContext(progressBar)).toList();
        }

        for (LocalRepository repository : repositories) {
            spec.commandLine().getOut().println("Building " + repository.toAnsi());

            // ensure .moderne/ exists
            dotModerne(repository);
            Path outputDir = outputDir(repository);
            ignoreDotModerne(repository);

            lstJars.add(new RepositoryBuildAction(repository, serializer, copybooks, outputDir).build());
        }

        return lstJars;
    }

    @RequiredArgsConstructor
    public class RepositoryBuildAction {
        private final LocalRepository repository;
        private final TreeSerializer serializer;
        private final List<SourceFile> copybooks;
        private final Path outputDir;

        public LstJarFile build() {
            List<Path> alreadyParsed = new ArrayList<>();
            Set<SourceFile> referencedCopybooks = new TreeSet<>(Comparator.comparing(SourceFile::getSourcePath));

            try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
                LstProvenance lstProvenance = new LstProvenance(Tree.randomId(), LstProvenance.Type.CobolCli,
                        getCliVersion(), getAstWriteVersion(), Instant.now());
                CobolParser cobolParser = CobolParser.builder().copybooks(copybooks)
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .build();
                List<Path> cobolSources = OmniParser.builder(cobolParser)
                        .exclusions(copybooks.stream().map(SourceFile::getSourcePath).toList())
                        .build()
                        .acceptedPaths(repository.getRootDir());
                progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                        .a(" Parsing " + cobolSources.size() + " COBOL files.")
                        .toString());
                progressBar.setMax(cobolSources.size());
                serializer.write(
                        cobolParser.parse(cobolSources, repository.getRootDir(), progressReportingExecutionContext(progressBar))
                                .map(sourceFile -> (SourceFile) sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(lstProvenance)))
                                .peek(sourceFile -> referencedCopybooks(sourceFile, referencedCopybooks)),
                        outputDir
                );
                alreadyParsed.addAll(cobolSources);
            }

            try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
                progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                        .a(" Serializing " + referencedCopybooks.size() + " referenced copybooks.")
                        .toString());
                progressBar.setMax(referencedCopybooks.size());
                serializer.write(
                        referencedCopybooks.stream().map(sourceFile -> {
                            progressBar.setExtraMessage(sourceFile.getSourcePath().toString());
                            alreadyParsed.add(sourceFile.getSourcePath());
                            return sourceFile.withSourcePath(repository.getRootDir().relativize(sourceFile.getSourcePath()));
                        }),
                        outputDir
                );
            }

            try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
                ControlMParser controlMParser = ControlMParser.builder().build();
                List<Path> controlMSources = OmniParser.builder(controlMParser)
                        .exclusions(alreadyParsed)
                        .build()
                        .acceptedPaths(repository.getRootDir());
                progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                        .a(" Parsing " + controlMSources.size() + " Control-M schedules.")
                        .toString());
                progressBar.setMax(referencedCopybooks.size());
                serializer.write(
                        controlMParser.parse(controlMSources, repository.getRootDir(),
                                progressReportingExecutionContext(progressBar)),
                        outputDir
                );
                alreadyParsed.addAll(controlMSources);
            }

            try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
                progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                        .a(" Parsing files related to other mainframe technologies.")
                        .toString());

                OmniParser parser = OmniParser.builder(PlainTextParser.builder().build())
                        .exclusions(alreadyParsed)
                        .onParse(progressBar::setMax)
                        .build();

                List<File> written = serializer.write(
                        parser.parse(parser.acceptedPaths(repository.getRootDir()), repository.getRootDir(),
                                progressReportingExecutionContext(progressBar)),
                        outputDir
                );
                if(validate) {
                    spec.commandLine().getOut().println("Validating that LST files can be deserialized.");
                    for (File astFile : written) {
                        try(InputStream is = Files.newInputStream(astFile.toPath())) {
                            serializer.read(is);
                        } catch (Exception e) {
                            throw new RuntimeException("Unable to validate newly written ast file " + astFile.getPath(), e);
                        }
                    }
                    spec.commandLine().getOut().println("✅ All LST files validated to deserialize.");
                }
            }

            return LstJarFile.assemble(repository, outputDir);
        }

        private void referencedCopybooks(SourceFile sf, Set<SourceFile> referencedCopybooks) {
            new CobolIsoVisitor<ExecutionContext>() {
                @Override
                public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                    Cobol.Word w = super.visitWord(word, ctx);
                    for (CobolPreprocessor ps : w.getPreprocessorStatements()) {
                        if (ps instanceof CobolPreprocessor.CopyStatement copyStatement) {
                            if (copyStatement.getMarkers().findFirst(MissingCopybook.class).isEmpty()) {
                                referencedCopybooks.add(copyStatement.getCopybook());
                            }
                        } else if (ps instanceof CobolPreprocessor.ExecSqlIncludeStatement includeStatement) {
                            if (includeStatement.getMarkers().findFirst(MissingCopybook.class).isEmpty()) {
                                referencedCopybooks.add(includeStatement.getCopybook());
                            }
                        }
                    }
                    return w;
                }
            }.visit(sf, new InMemoryExecutionContext());
        }
    }

    // https://luisdalmolin.dev/blog/ignoring-files-in-git-without-gitignore/
    private void ignoreDotModerne(LocalRepository repository) {
        Path dotGit = repository.getRootDir().resolve(".git");
        if (Files.exists(dotGit)) {
            Path gitExclude = dotGit.resolve("info").resolve("exclude");
            try {
                if (Files.exists(gitExclude) && Files.readAllLines(gitExclude).stream()
                        .anyMatch(line -> line.equals(dotModerne(repository).toString()))) {
                    return;
                }
            } catch (IOException ignored) {
            }
            if (!Files.exists(gitExclude.getParent())) {
                if (!gitExclude.getParent().toFile().mkdirs()) {
                    spec.commandLine().getOut().println("⚠️ Unable to create .git/info/exclude in repository " + repository.toAnsi() + ".");
                }
            }
            try {
                Files.writeString(gitExclude, dotModerne(repository).toString());
            } catch (IOException e) {
                spec.commandLine().getOut().println("⚠️ Unable to add .moderne folder to .git/info/exclude in repository " + repository.toAnsi() + ".");
            }
        }
    }

    private Path dotModerne(LocalRepository repository) {
        Path dotModerne = repository.getRootDir().relativize(repository.getRootDir().resolve(".moderne"));
        if (!exists(dotModerne) && !dotModerne.toFile().mkdirs()) {
            spec.commandLine().getOut().println("⚠️ Unable to create .moderne directory for repository " + repository.toAnsi() + ".");
        }
        return dotModerne;
    }

    private Path outputDir(LocalRepository repository) {
        Path target = dotModerne(repository).resolve("build");
        try {
            if(Files.exists(target)) {
                // clean the existing build directory
                try (Stream<Path> walk = Files.walk(target)) {
                    //noinspection ResultOfMethodCallIgnored
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (!target.toFile().mkdirs()) {
            spec.commandLine().getOut().println("⚠️ Unable to create .moderne/build directory for repository " + repository.toAnsi() + ".");
        }
        return target;
    }

    private ExecutionContext progressReportingExecutionContext(ProgressBar progressBar) {
        ExecutionContext ctx = new InMemoryExecutionContext() {
            @Override
            public Consumer<Throwable> getOnError() {
                return t -> {
                    if (t instanceof CobolParsingTimeoutException timeout) {
                        progressBar.intermediateResult("⚠️ Timed out parsing " + timeout.getSourcePath());
                    }
                };
            }
        };

        return ParsingExecutionContextView.view(ctx).setParsingListener(new ParsingEventListener() {
            @Override
            public void intermediateMessage(String stateMessage) {
                progressBar.intermediateResult(stateMessage);
            }

            @Override
            public void startedParsing(Parser.Input input) {
                progressBar.setExtraMessage(input.getPath().toString());
            }

            @Override
            public void parsed(Parser.Input input, SourceFile sourceFile) {
                progressBar.step();
            }
        });
    }

    public static String getCliVersion() {
        try {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(requireNonNull(Build.class.getResourceAsStream("/cli-version.txt")))))  {
                return reader.readLine().trim();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getAstWriteVersion() {
        try {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(requireNonNull(Build.class.getResourceAsStream("/ast-write-version.txt")))))  {
                return reader.readLine().trim();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
