package com.pigxity.portablemc.library;

import java.nio.file.Path;

public final class MavenCoordinates {
    private MavenCoordinates() {}

    public static Path libraryPath(String coordinate) {
        String[] parts = parts(coordinate);
        String classifier = parts.length == 4 ? "-" + parts[3] : "";

        return Path.of(
                parts[0].replace('.', '/'),
                parts[1],
                parts[2],
                parts[1] + "-" + parts[2] + classifier + ".jar");
    }

    public static String classifier(String coordinate) {
        String[] parts = parts(coordinate);

        return parts.length == 4 ? parts[3] : "";
    }

    private static String[] parts(String coordinate) {
        String[] parts = coordinate.split(":", -1);

        if (parts.length < 3
                || parts.length > 4
                || parts[0].isBlank()
                || parts[1].isBlank()
                || parts[2].isBlank()) {
            throw new IllegalArgumentException("Unsupported Maven coordinate: " + coordinate);
        }

        return parts;
    }
}
