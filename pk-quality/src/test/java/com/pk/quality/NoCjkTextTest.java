package com.pk.quality;

import static org.junit.jupiter.api.Assertions.fail;

import com.pk.quality.support.RepositoryFileWalker;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class NoCjkTextTest {
    private static final Pattern CJK = Pattern.compile("[\\p{IsHan}\\p{InHiragana}\\p{InKatakana}\\p{InHangul_Syllables}]");

    @Test
    void projectFilesDoNotContainCjkText() throws IOException {
        Path root = RepositoryFileWalker.repositoryRoot();
        List<String> violations = new ArrayList<>();

        RepositoryFileWalker.forEachTextLine(
                root,
                (relative, line) -> {
                    Matcher matcher = CJK.matcher(line);
                    if (matcher.find()) {
                        violations.add(formatViolation(relative, line));
                    }
                });

        if (!violations.isEmpty()) {
            fail("CJK text is not allowed in pk-backend-app. Fix or remove:\n"
                    + String.join(System.lineSeparator(), violations));
        }
    }

    private static String formatViolation(Path relative, String line) {
        String snippet = line.trim();
        if (snippet.length() > 120) {
            snippet = snippet.substring(0, 117) + "...";
        }
        return relative + " -> " + snippet;
    }
}
