package io.moderne.cobol;

import io.moderne.cobol.internal.ModerneColors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.fusesource.jansi.Ansi;
import org.openrewrite.marker.GitProvenance;
import picocli.CommandLine;

import java.nio.file.Path;

import static java.nio.file.Files.exists;
import static org.fusesource.jansi.Ansi.ansi;

@Data
@RequiredArgsConstructor
public class LocalRepository {
    private final Path rootDir;
    private final GitProvenance gitProvenance;

    public Ansi toAnsi() {
        if (gitProvenance == null) {
            return ansi().fgRgb(ModerneColors.Blue.rgb()).a(rootDir.toFile().getName()).reset().a(" (not a git repository)");
        }
        return ansi().fgRgb(ModerneColors.Blue.rgb()).a(gitProvenance.getOrganizationName()).a('/').a(gitProvenance.getRepositoryName()).a('@')
                .a(gitProvenance.getBranch() == null ? "detached" : gitProvenance.getBranch()).reset();
    }
}
