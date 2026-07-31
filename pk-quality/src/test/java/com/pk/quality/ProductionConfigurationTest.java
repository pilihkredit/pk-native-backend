package com.pk.quality;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ProductionConfigurationTest {
    private static final Path REPOSITORY_ROOT = repositoryRoot();
    private static final List<Path> PRODUCTION_CONFIGS = List.of(
            REPOSITORY_ROOT.resolve("pk-app/src/main/resources/application-prod.yml"),
            REPOSITORY_ROOT.resolve("pk-worker/src/main/resources/application-prod.yml")
    );
    private static final Pattern VARIABLE_WITH_DEFAULT = Pattern.compile("\\$\\{[^}:]+:[^}]*}");

    @Test
    void productionVariablesDoNotDeclareDefaultValues() throws IOException {
        for (Path config : PRODUCTION_CONFIGS) {
            String content = Files.readString(config);
            assertFalse(
                    VARIABLE_WITH_DEFAULT.matcher(content).find(),
                    () -> config + " must not assign default values to environment variables"
            );
        }
    }

    @Test
    void productionLoggingUsesInfoLevel() throws IOException {
        for (Path config : PRODUCTION_CONFIGS) {
            String content = Files.readString(config);
            assertTrue(content.contains("root: INFO"), () -> config + " must use INFO root logging");
            assertTrue(content.contains("com.pk: INFO"), () -> config + " must use INFO application logging");
        }
    }

    @Test
    void productionHikariPoolSizeComesFromEnvironment() throws IOException {
        for (Path config : PRODUCTION_CONFIGS) {
            String content = Files.readString(config);
            assertTrue(
                    content.contains("maximum-pool-size: ${PK_DB_MAXIMUM_POOL_SIZE}"),
                    () -> config + " must source the Hikari maximum pool size from the environment"
            );
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        return Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("pk-app"))
                ? current
                : current.getParent();
    }
}
