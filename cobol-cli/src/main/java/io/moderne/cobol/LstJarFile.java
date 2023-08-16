package io.moderne.cobol;

import io.moderne.cobol.internal.ModerneAstWrite;
import io.moderne.serialization.OriginHelper;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.marker.GitProvenance;
import picocli.CommandLine;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.openrewrite.internal.StringUtils.isBlank;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LstJarFile {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    Path location;
    String orgId;
    String artifactId;
    String version;
    String buildId;
    String cloneUrl;
    String origin;
    String branch;
    String change;

    public static LstJarFile load(Path jarFile) {
        try {
            URL scmProperties = new URL("jar:" + jarFile.toUri().toURL() + "!/scm.properties");
            Properties prop = new Properties();
            JarURLConnection conn = (JarURLConnection) scmProperties.openConnection();

            try (InputStream in = conn.getInputStream()) {
                prop.load(in);
            }
            return new LstJarFile(jarFile,
                    prop.getProperty("groupId"),
                    prop.getProperty("artifactId"),
                    prop.getProperty("version"),
                    prop.getProperty("buildId"),
                    prop.getProperty("cloneUrl"),
                    prop.getProperty("origin"),
                    prop.getProperty("branch"),
                    prop.getProperty("change"));
        } catch (IOException e) {
            throw new UncheckedIOException("Error resolving LST file " + jarFile.toFile().getAbsolutePath(), e);
        }
    }

    public static LstJarFile assemble(LocalRepository repository, Path outputDir) {
        String version = DATE_FORMAT.format(new Date());
        GitProvenance gitProvenance = repository.getGitProvenance();

        String orgName = repository.getRootDir().toFile().getName();
        String artifactName = orgName;
        if (gitProvenance != null) {
            orgName = gitProvenance.getOrganizationName("https://github.com/");
            artifactName = gitProvenance.getRepositoryName();
        }

        String artifactAndVersion = artifactName + '-' + version;

        // Write the AST jar
        Path lstJarFile = outputDir.resolve(artifactAndVersion + "-ast.jar");
        try (OutputStream os = Files.newOutputStream(lstJarFile);
             BufferedOutputStream bos = new BufferedOutputStream(os);
             JarOutputStream jar = new JarOutputStream(bos)) {

            try (DirectoryStream<Path> lsts = Files.newDirectoryStream(outputDir, "*.lst")) {
                boolean found = false;
                for (Path lst : lsts) {
                    found = true;
                    jar.putNextEntry(new JarEntry(lst.getFileName().toString()));
                    Files.copy(lst, jar);
                    jar.closeEntry();
                }

                if (!found) {
                    throw new RuntimeException("No LST files have been produced. An aggregate JAR will not be produced " +
                                               "to prevent the publishing of an unusable JAR.");
                }
            }

            Properties scmProvenance = scmProperties(gitProvenance, orgName, artifactName, version);
            if (Files.exists(outputDir.resolve("project.properties"))) {
                Properties properties = new Properties();
                properties.load(Files.newInputStream(outputDir));
                scmProvenance.setProperty("weight", properties.getProperty("weight", "0"));
            }

            jar.putNextEntry(new JarEntry("scm.properties"));
            scmProvenance.store(jar, null);
            jar.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return load(lstJarFile);
    }

    private static Properties scmProperties(GitProvenance gitProvenance, String groupId, String artifactId, String version) {
        Properties p = new Properties();
        p.setProperty("buildId", UUID.randomUUID().toString());
        p.setProperty("lstFormatVersion", "2");
        if (gitProvenance.getOrigin() == null) {
            //the publish command will reject these jars
            System.out.println("WARNING: The AST for " + groupId + ":" + artifactId + ":" + version
                               + " can't be published because does not contain Git metadata. ");
            p.setProperty("cloneUrl", "");
            p.setProperty("origin", "");
        } else {
            p.setProperty("cloneUrl", gitProvenance.getOrigin());
            p.setProperty("origin", OriginHelper.stripOrigin(gitProvenance.getOrigin()));
        }

        p.setProperty("path", groupId + "/" + artifactId);
        p.setProperty("branch", Optional.ofNullable(gitProvenance.getBranch()).orElse(""));
        p.setProperty("change", Optional.of(gitProvenance.getChange()).orElse(""));
        p.setProperty("path", gitProvenance.getOrganizationName() + "/" + gitProvenance.getRepositoryName());
        p.setProperty("created", Long.toString(System.currentTimeMillis()));
        p.setProperty("groupId", groupId);
        p.setProperty("artifactId", artifactId);
        p.setProperty("version", version);
        p.setProperty("buildPluginName", "moderne-cli");
        p.setProperty("buildPluginVersion", "1.0.0");
        p.setProperty("astWriteVersion", ModerneAstWrite.getVersion());
        return p;
    }

    public void publish(String user, String password, String url, boolean skipSSL) {
        File jarFile = location.toFile();

        if (isBlank(orgId)) {
            throw new RuntimeException("The group id can not be empty for publishing");
        }
        if (isBlank(version) || "unspecified".equals(version)) {
            throw new RuntimeException("Invalid version format for publishing");
        }

        if (isBlank(cloneUrl) || isBlank(origin) || isBlank(branch) || isBlank(change)) {
            throw new RuntimeException(String.format(
                    "Invalid or missing Git repository (cloneURL: %s, origin: %s, branch: %s, change: %s)",
                    cloneUrl, origin, branch, change));
        }

        String deployUrl = String.format("%s/%s/%s/%s/%s",
                url, orgId.replace('.', '/'), artifactId,
                version, jarFile.getName());
        Unirest.config().verifySsl(!skipSSL);

        HttpRequestWithBody request = Unirest.put(deployUrl)
                .header("Content-Type", "multipart/form-data");

        if (!isBlank(user) && !isBlank(password)) {
            String encoding = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
            request.header("Authorization", "Basic " + encoding);
        }

        HttpResponse<String> response = request
                .field("file", jarFile)
                .asString();
        if (!response.isSuccess()) {
            // FIXME error handling here should update progress bar
            throw new RuntimeException("Error publishing " + jarFile.getAbsolutePath()
                                       + " in " + deployUrl + ". Error code " + response.getStatus()
                                       + " with the following error " + response.getBody());
        }
    }
}
