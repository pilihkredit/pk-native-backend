package com.pk.quality.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class RepositoryFileWalker {
    private static final Set<String> SKIPPED_DIRS = Set.of(".git", ".idea", ".mvn", "target");
    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            ".jar", ".class", ".zip", ".png", ".jpg", ".jpeg", ".gif", ".webp", ".ico", ".pdf", ".bin"
    );

    private RepositoryFileWalker() {
    }

    public static void forEachTextLine(Path root, BiConsumer<Path, String> lineConsumer) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(RepositoryFileWalker::isCheckedFile)
                    .forEach(path -> readLines(path, root, lineConsumer));
        }
    }

    public static Path repositoryRoot() {
        return Path.of("").toAbsolutePath().getParent();
    }

    private static boolean isCheckedFile(Path path) {
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

    private static void readLines(Path path, Path root, BiConsumer<Path, String> lineConsumer) {
        try {
            String content = Files.readString(path);
            String[] lines = content.split("\n", -1);
            Path relative = root.relativize(path);
            for (String line : lines) {
                lineConsumer.accept(relative, line);
            }
        } catch (IOException ignored) {
        }
    }
}
