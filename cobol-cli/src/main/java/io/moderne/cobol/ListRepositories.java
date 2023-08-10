package io.moderne.cobol;

import io.moderne.cobol.internal.FatalException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.openrewrite.ProgressBar;
import org.openrewrite.marker.GitProvenance;
import org.openrewrite.marker.ci.BuildEnvironment;
import picocli.CommandLine;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.fusesource.jansi.Ansi.ansi;

@NoArgsConstructor
@AllArgsConstructor
@CommandLine.Command(name = "list",
        headerHeading = "@|bold,underline Usage|@:%n%n",
        synopsisHeading = "%n",
        descriptionHeading = "%n@|bold,underline Description|@:%n%n",
        parameterListHeading = "%n@|bold,underline Parameters|@:%n%n",
        optionListHeading = "%n@|bold,underline Options|@:%n%n",
        header = "Lists repository roots that can be built and published.")
public class ListRepositories implements Callable<Integer> {
    public static final List<PathMatcher> FIND_REPOSITORY_EXCLUSIONS = Stream.of("**/*~", "**/#*#", "**/.#*", "**/%*%",
                    "**/._*", "**/CVS", "**/CVS/**", "**/.cvsignore", "**/SCCS", "**/SCCS/**", "**/vssver.scc",
                    "**/.svn", "**/.svn/**", "**/.git", "**/.git/**", "**/.gitattributes", "**/.gitignore",
                    "**/.gitmodules", "**/.hg", "**/.hg/**", "**/.hgignore", "**/.hgsub", "**/.hgsubstate",
                    "**/.moderne", "**/.moderne/**", "**/target", "**/target/**", "**/build", "**/build/**",
                    "**/.hgtags", "**/.bzr", "**/.bzr/**", "**/.bzrignore", "**/.DS_Store")
            .map(exclude -> FileSystems.getDefault().getPathMatcher("glob:" + exclude))
            .collect(Collectors.toList());
    private static final BuildEnvironment BUILD_ENVIRONMENT = BuildEnvironment.build(System::getenv);

    @CommandLine.Mixin
    ListRepositoryOptions listRepositoryOptions;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() {
        list();
        return 0;
    }

    public List<LocalRepository> list() {
        if (!Files.exists(listRepositoryOptions.path)) {
            throw new FatalException("The search directory does not exist.");
        }

        spec.commandLine().getOut().println(ansi().bold().a("> Identifying repositories\n").boldOff());
        try (ProgressBar progressBar = DefaultProgressBar.builder(spec)
                .task("Searching for repositories")
                .continuousUpdate(true)
                .build()) {
            List<LocalRepository> repositories = new ArrayList<>();
            Files.walkFileTree(listRepositoryOptions.getPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) {
                    for (PathMatcher pathMatcher : FIND_REPOSITORY_EXCLUSIONS) {
                        if (pathMatcher.matches(file)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }

                    GitProvenance gitProvenance = GitProvenance.fromProjectDirectory(file.toFile().toPath(), BUILD_ENVIRONMENT);
                    if (gitProvenance != null) {
                        LocalRepository repository = new LocalRepository(file, gitProvenance);
                        progressBar.intermediateResult(String.format("Found repository %s", repository.toAnsi()));
                        repositories.add(repository);
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return listRepositoryOptions.isRecursive() ?
                            FileVisitResult.CONTINUE :
                            FileVisitResult.SKIP_SUBTREE;
                }
            });
            return repositories;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
