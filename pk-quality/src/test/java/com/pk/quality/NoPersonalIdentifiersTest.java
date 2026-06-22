package com.pk.quality;

import static org.junit.jupiter.api.Assertions.fail;

import com.pk.quality.support.RepositoryFileWalker;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class NoPersonalIdentifiersTest {
    @Test
    void projectFilesDoNotContainBlockedPersonalIdentifiers() throws IOException {
        Path root = RepositoryFileWalker.repositoryRoot();
        List<String> blocked = loadBlockedSubstrings();
        List<Pattern> patterns = blocked.stream()
                .map(s -> Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE))
                .toList();

        List<String> violations = new ArrayList<>();
        RepositoryFileWalker.forEachTextLine(
                root,
                (relative, line) -> {
                    if (relative.toString().endsWith("blocked-personal-substrings.txt")) {
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
        Path list = Path.of("src/test/resources/blocked-personal-substrings.txt")
                .toAbsolutePath()
                .normalize();
        if (!Files.isRegularFile(list)) {
            list = Path.of("pk-quality/src/test/resources/blocked-personal-substrings.txt")
                    .toAbsolutePath()
                    .normalize();
        }
        return Files.readAllLines(list, StandardCharsets.UTF_8).stream()
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
