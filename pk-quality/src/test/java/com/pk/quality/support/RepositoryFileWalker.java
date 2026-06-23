package com.pk.quality.support;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class RepositoryFileWalker {
    private static final Set<String> SKIPPED_DIRS = Set.of(
            ".git",
            ".idea",
            "target",
            "build",
            "out"
    );

    private RepositoryFileWalker() {
    }

    public static Path repositoryRoot() {
        return Path.of("").toAbsolutePath().normalize();
    }

    public static void forEachTextLine(Path root, BiConsumer<Path, String> consumer) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                if (shouldSkip(root, path)) {
                    continue;
                }
                readTextFile(root, path, consumer);
            }
        }
    }

    private static boolean shouldSkip(Path root, Path path) {
        Path relative = root.relativize(path);
        for (Path part : relative) {
            if (SKIPPED_DIRS.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private static void readTextFile(Path root, Path path, BiConsumer<Path, String> consumer) throws IOException {
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                consumer.accept(root.relativize(path), line);
            }
        } catch (CharacterCodingException ignored) {
            // Binary files are skipped by this text-only quality check.
        }
    }
}
