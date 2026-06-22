package com.pk.quality;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NoCjkTextTest {
    private static final Pattern CJK = Pattern.compile("[\\p{IsHan}\\p{InHiragana}\\p{InKatakana}\\p{InHangul_Syllables}]");
    private static final Set<String> SKIPPED_DIRS = Set.of(".git", ".idea", ".mvn", "target");

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
            fail(String.join(System.lineSeparator(), violations));
        }
    }

    private boolean isCheckedFile(Path path) {
        for (Path part : path) {
            if (SKIPPED_DIRS.contains(part.toString())) {
                return false;
            }
        }
        String fileName = path.getFileName().toString();
        return !fileName.endsWith(".class") && !fileName.endsWith(".jar");
    }

    private void inspect(Path path, Path root, List<String> violations) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (CJK.matcher(content).find()) {
                violations.add(root.relativize(path) + " contains disallowed CJK text");
            }
        } catch (IOException | RuntimeException ignored) {
        }
    }
}
