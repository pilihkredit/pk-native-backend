package com.pk.quality;

import static org.junit.jupiter.api.Assertions.fail;

import com.pk.quality.support.RepositoryFileWalker;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class NoPersonalIdentifiersTest {
    private static final String ENV_KEY = "PK_QUALITY_BLOCKED_SUBSTRINGS";
    private static final String LOCAL_FILE = "blocked-personal-substrings.local.txt";

    @Test
    void projectFilesDoNotContainBlockedPersonalIdentifiers() throws IOException {
        List<String> blocked = loadBlockedSubstrings();
        Assumptions.assumeFalse(
                blocked.isEmpty(),
                "Skip: set " + ENV_KEY + " or create " + LOCAL_FILE + " from .example (never commit)");

        Path root = RepositoryFileWalker.repositoryRoot();
        List<Pattern> patterns = blocked.stream()
                .map(s -> Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE))
                .toList();

        List<String> violations = new ArrayList<>();
        RepositoryFileWalker.forEachTextLine(
                root,
                (relative, line) -> {
                    String path = relative.toString();
                    if (path.endsWith(LOCAL_FILE) || path.endsWith(".example")) {
                        return;
                    }
                    for (Pattern pattern : patterns) {
                        if (pattern.matcher(line).find()) {
                            violations.add(formatViolation(relative, line));
                            break;
                        }
                    }
                });

        if (!violations.isEmpty()) {
            fail("Personal identifiers are not allowed in pk-backend-app. Fix or remove:\n"
                    + String.join(System.lineSeparator(), violations));
        }
    }

    private static List<String> loadBlockedSubstrings() throws IOException {
        List<String> blocked = new ArrayList<>();
        blocked.addAll(fromEnvironment());
        blocked.addAll(fromLocalFile());
        return blocked.stream().map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }

    private static List<String> fromEnvironment() {
        String raw = System.getenv(ENV_KEY);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static List<String> fromLocalFile() throws IOException {
        Path root = RepositoryFileWalker.repositoryRoot();
        Path local = root.resolve(LOCAL_FILE);
        if (!Files.isRegularFile(local)) {
            return List.of();
        }
        return Files.readAllLines(local, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                .toList();
    }

    private static String formatViolation(Path relative, String line) {
        String snippet = line.trim();
        if (snippet.length() > 120) {
            snippet = snippet.substring(0, 117) + "...";
        }
        return relative + " -> " + snippet;
    }
}
