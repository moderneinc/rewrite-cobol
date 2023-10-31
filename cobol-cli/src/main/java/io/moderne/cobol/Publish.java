package io.moderne.cobol;

import io.moderne.cobol.internal.FatalException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.polyglot.ProgressBar;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

import static org.fusesource.jansi.Ansi.ansi;

@AllArgsConstructor
@NoArgsConstructor
@CommandLine.Command(name = "publish")
public class Publish implements Callable<Integer> {
    @CommandLine.Mixin
    ListRepositoryOptions listRepositoryOptions;

    @CommandLine.Option(names = {"--publishUrl"}, defaultValue = "${MODERNE_PUBLISH_URL}",
            description = "The URL of the Maven repository where LST artifacts should be uploaded to. " +
                          "Will default to the environment variable @|bold MODERNE_PUBLISH_URL|@ if one exists.\n")
    private String publishUrl;

    @CommandLine.Option(names = {"--publishUser"}, defaultValue = "${MODERNE_PUBLISH_USER}",
            description = "The user that has permission to upload LST artifacts." +
                          "Will default to the environment variable @|bold MODERNE_PUBLISH_USER|@ if one exists.\n")
    private String publishUser;

    @CommandLine.Option(names = {"--publishPwd"}, defaultValue = "${MODERNE_PUBLISH_PWD}",
            description = "The password for the user that has permission to upload LST artifacts." +
                          "Will default to the environment variable @|bold MODERNE_PUBLISH_PWD|@ if one exists.\n")
    private String publishPwd;

    @CommandLine.Option(names = "--timeoutSeconds",
            defaultValue = "10",
            description = "A per-file timeout in seconds for parsing.")
    protected int timeoutSeconds;

    @CommandLine.Option(names = "--skipSSL",
            defaultValue = "false",
            description = """
                    If this parameter is included, SSL verification will be skipped.

                    @|bold Default|@: ${DEFAULT-VALUE}
                    """)
    protected boolean skipSSL;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() {
        List<LocalRepository> repositories = new ListRepositories(listRepositoryOptions, spec).list();

        validateOptions();

        List<LstJarFile> lstJars = new Build(listRepositoryOptions, spec, timeoutSeconds, false).build(repositories);

        spec.commandLine().getOut().println(ansi().bold().a("\n> Publishing LST(s)\n").boldOff());
        try (ProgressBar progressBar = DefaultProgressBar.builder(spec).build().setMax(lstJars.size())) {
            for (LstJarFile lstJarFile : lstJars) {
                progressBar.setExtraMessage(lstJarFile.getArtifactId() + ":" + lstJarFile.getVersion());
                lstJarFile.publish(publishUser, publishPwd, publishUrl, skipSSL);
                // FIXME make this a link to artifact repo instead so a dev can click through to artifact repo.
                progressBar.intermediateResult("✅ Published " + lstJarFile.getArtifactId() + ":" + lstJarFile.getVersion());
                progressBar.step();
            }
        }
        return 0;
    }

    private void validateOptions() {
        // Ensure values required for publishing are set
        if (StringUtils.isBlank(publishUrl)) {
            throw new FatalException("The publishUrl parameter or MODERNE_PUBLISH_URL should not be null or empty");
        }
        if (StringUtils.isBlank(publishUser)) {
            throw new FatalException("The publishUser parameter or MODERNE_PUBLISH_USER should not be null or empty");
        }
        if (StringUtils.isBlank(publishPwd)) {
            throw new FatalException("The publishPwd parameter or MODERNE_PUBLISH_PWD should not be null or empty");
        }
    }
}
