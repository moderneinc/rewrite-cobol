package io.moderne.cobol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import picocli.CommandLine;

import java.nio.file.Path;

/**
 * Options used in the identification of repositories to perform an action on.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ListRepositoryOptions {

    @CommandLine.Option(names = "--path", defaultValue = ".",
            description = "The path on disk to the project directory you want to be ingested into the Moderne platform. " +
                          "Typically a checked-out copy of a Git repository.\n\n" +
                          "@|bold Example|@: /path/to/project\n")
    protected Path path;

    @CommandLine.Option(names = "--recursive",
            defaultValue = "false",
            description = "Specifies whether or not projects should be looked for recursively. If this parameter is " +
                          "included, it will start at the @|bold --path|@ directory and recursively look for all " +
                          "projects to build.\n\n" +
                          "@|bold Default|@: ${DEFAULT-VALUE}\n")
    protected boolean recursive;
}
