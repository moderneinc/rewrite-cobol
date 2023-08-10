package io.moderne.cobol;

import io.moderne.cobol.internal.ModerneColors;
import io.moderne.serialization.TreeSerializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.openrewrite.*;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolParser;
import org.openrewrite.cobol.CopybookParser;
import org.openrewrite.cobol.markers.MissingCopybook;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.text.PlainTextParser;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

import static org.fusesource.jansi.Ansi.ansi;

@CommandLine.Command(name = "build")
@NoArgsConstructor
@AllArgsConstructor
public class Build implements Callable<Integer> {
    @CommandLine.Mixin
    ListRepositoryOptions listRepositoryOptions;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

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
        List<Path> copybookSources = OmniParser.builder().parsers(copybookParser).build().acceptedPaths(
                listRepositoryOptions.getPath());

        List<SourceFile> copybooks;
        try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
            progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                    .a(" Parsing " + copybookSources.size() + " copybook files to use on all repositories.")
                    .toString());
            copybooks = copybookParser.parse(copybookSources.stream().peek(s -> {
                progressBar.setExtraMessage(s.toString());
                progressBar.step();
            }), listRepositoryOptions.getPath(), new InMemoryExecutionContext()).toList();
        }

        for (LocalRepository repository : repositories) {
            spec.commandLine().getOut().println("Building " + repository.toAnsi());

            repository.dotModerne(spec);
            // ensure .moderne/ exists
            Path outputDir = repository.outputDir(spec);
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
            ExecutionContext ctx = new InMemoryExecutionContext();
            List<Path> alreadyParsed = new ArrayList<>();
            Set<SourceFile> referencedCopybooks = new TreeSet<>(Comparator.comparing(SourceFile::getSourcePath));

            try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
                CobolParser cobolParser = CobolParser.builder().copybooks(copybooks).build();
                List<Path> cobolSources = OmniParser.builder().parsers(cobolParser)
                        .exclusions(copybooks.stream().map(SourceFile::getSourcePath).toList())
                        .build()
                        .acceptedPaths(repository.getRootDir());
                progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                        .a(" Parsing " + cobolSources.size() + " COBOL files.")
                        .toString());
                progressBar.setMax(cobolSources.size());
                serializer.write(
                        cobolParser.parse(cobolSources.stream().peek(s -> {
                                            progressBar.setExtraMessage(s.toString());
                                            progressBar.step();
                                        }),
                                        repository.getRootDir(), ctx)
                                .peek(sourceFile -> referencedCopybooks(referencedCopybooks))
                                .peek(sourceFile -> alreadyParsed.add(sourceFile.getSourcePath())),
                        outputDir
                );
            }

            try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
                progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                        .a(" Serializing " + referencedCopybooks.size() + " referenced copybooks.")
                        .toString());
                progressBar.setMax(referencedCopybooks.size());
                serializer.write(
                        referencedCopybooks.stream()
                                .map(sourceFile -> {
                                    progressBar.setExtraMessage(sourceFile.getSourcePath().toString());
                                    alreadyParsed.add(sourceFile.getSourcePath());
                                    return sourceFile.withSourcePath(repository.getRootDir().relativize(sourceFile.getSourcePath()));
                                }),
                        outputDir
                );
            }

            try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build()) {
                progressBar.intermediateResult(ansi().fgRgb(ModerneColors.Yellow.rgb()).a(">").reset()
                        .a(" Parsing files related to other mainframe technologies.")
                        .toString());

                serializer.write(
                        OmniParser.builder()
                                .parsers(PlainTextParser.builder().build())
                                .exclusions(alreadyParsed)
                                .onParse(progressBar::setMax)
                                .onParseStart(path -> progressBar.setExtraMessage(path.toString()))
                                .build()
                                .parseAll(repository.getRootDir()),
                        outputDir
                );
            }

            return LstJarFile.assemble(repository, spec);
        }

        private void referencedCopybooks(Set<SourceFile> referencedCopybooks) {
            new CobolIsoVisitor<ExecutionContext>() {
                @Override
                public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                    Cobol.Word w = super.visitWord(word, ctx);
                    for (CobolPreprocessor ps : w.getPreprocessorStatements()) {
                        if (ps instanceof CobolPreprocessor.CopyStatement copyStatement) {
                            if (copyStatement.getMarkers().findFirst(MissingCopybook.class).isEmpty()) {
                                referencedCopybooks.add(copyStatement.getCopybook());
                            }
                        }
                    }

                    return w;
                }
            };
        }
    }

    // https://luisdalmolin.dev/blog/ignoring-files-in-git-without-gitignore/
    private void ignoreDotModerne(LocalRepository repository) {
        Path dotGit = repository.getRootDir().resolve(".git");
        if (Files.exists(dotGit)) {
            Path gitExclude = dotGit.resolve("info").resolve("exclude");
            try {
                if (Files.exists(gitExclude) && Files.readAllLines(gitExclude).stream()
                        .anyMatch(line -> line.equals(repository.dotModerne(spec).toString()))) {
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
                Files.writeString(gitExclude, repository.dotModerne(spec).toString());
            } catch (IOException e) {
                spec.commandLine().getOut().println("⚠️ Unable to add .moderne folder to .git/info/exclude in repository " + repository.toAnsi() + ".");
            }
        }
    }
}
