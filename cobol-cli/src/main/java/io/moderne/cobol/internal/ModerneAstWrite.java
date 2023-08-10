package io.moderne.cobol.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class ModerneAstWrite {
    private final static Properties properties = new Properties();

    public static String getVersion() {
        if (properties.size() == 0) {
            synchronized (properties) {
                if (properties.size() == 0) {
                    try (InputStream is = ModerneAstWrite.class.getResourceAsStream("/moderne-ast-write.properties")) {
                        if (is == null) {
                            // We are probably in a dev env running a test in IntelliJ mode, and we do not have the resource.
                            // Nothing to do, we are just going to fall back to default value.
                            return "latest.release";
                        }
                        properties.load(is);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        }
        return properties.getProperty("ast.version", "latest.release");
    }
}
