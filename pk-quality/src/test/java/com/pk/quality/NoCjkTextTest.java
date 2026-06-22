package com.pk.quality;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NoCjkTextTest {
    private static final Pattern CJK = Pattern.compile("[\\p{IsHan}\\p{InHiragana}\\p{InKatakana}\\p{InHangul_Syllables}]");
    private static final Set<String> SKIPPED_DIRS = Set.of(".git", ".idea", ".mvn", "target");
    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            ".jar", ".class", ".zip", ".png", ".jpg", ".jpeg", ".gif", ".webp", ".ico", ".pdf", ".bin"
    );

    @Test
    void projectFilesDoNotContainCjkText() throws IOException {
        Path root = Path.of("").toAbsolutePath().getParent();
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isCheckedFile)
                    .forEach(path -> inspect(path, root, violations));
        }

        if (!violations.isEmpty()) {
            fail("CJK text is not allowed in pk-backend-app. Fix or remove:\n" + String.join(System.lineSeparator(), violations));
        }
    }

    private boolean isCheckedFile(Path path) {
        for (Path part : path) {
            if (SKIPPED_DIRS.contains(part.toString())) {
                return false;
            }
        }
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && BINARY_EXTENSIONS.contains(fileName.substring(dot).toLowerCase())) {
            return false;
        }
        return true;
    }

    private void inspect(Path path, Path root, List<String> violations) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            String[] lines = content.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                Matcher matcher = CJK.matcher(lines[i]);
                if (matcher.find()) {
                    String relative = root.relativize(path).toString();
                    String snippet = lines[i].trim();
                    if (snippet.length() > 120) {
                        snippet = snippet.substring(0, 117) + "...";
                    }
                    violations.add(relative + ":" + (i + 1) + " -> " + snippet);
                }
            }
        } catch (IOException ignored) {
        }
    }
}
