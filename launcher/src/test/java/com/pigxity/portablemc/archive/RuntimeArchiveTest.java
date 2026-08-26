package com.pigxity.portablemc.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

class RuntimeArchiveTest {
    @TempDir Path temporaryDirectory;

    @Test
    void extractsEveryOverrideRelativeToTheDestinationAndReadsMetadata() throws Exception {
        Path jar = temporaryDirectory.resolve("runtime.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            entry(output, "overrides/libraries/example.jar", "library");
            entry(output, "overrides/assets/indexes/26.json", "{}");
            entry(output, "overrides/versions/26.1/26.1.jar", "client");
            entry(output, "overrides/config/default.txt", "automatic");
            entry(output, "packagerules.json", "{\"version\":\"26.1\"}");
            entry(output, "clientmeta.properties", "mainClass=example.Main");
            entry(output, "unrelated.txt", "ignored");
        }

        Path destination = temporaryDirectory.resolve("game");
        RuntimeArchive archive = new RuntimeArchive(jar);
        archive.extractOverrides(destination);

        assertTrue(Files.isRegularFile(destination.resolve("libraries/example.jar")));
        assertTrue(Files.isRegularFile(destination.resolve("assets/indexes/26.json")));
        assertTrue(Files.isRegularFile(destination.resolve("versions/26.1/26.1.jar")));
        assertEquals("automatic", Files.readString(destination.resolve("config/default.txt")));
        assertTrue(Files.notExists(destination.resolve("unrelated.txt")));
        assertEquals("mainClass=example.Main", archive.readFile("clientmeta.properties"));
        assertThrows(IOException.class, () -> archive.readFile("missing-file"));
    }

    @Test
    void skipsMatchingFilesAndReplacesFilesWhoseHashesDiffer() throws Exception {
        Path jar = temporaryDirectory.resolve("runtime.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            entry(output, "overrides/matching.txt", "same content");
            entry(output, "overrides/changed.txt", "new content");
        }

        Path destination = temporaryDirectory.resolve("game");
        Files.createDirectories(destination);
        Path matching = destination.resolve("matching.txt");
        Path changed = destination.resolve("changed.txt");
        Files.writeString(matching, "same content");
        Files.writeString(changed, "old content");
        FileTime originalTime = FileTime.fromMillis(946684800000L);
        Files.setLastModifiedTime(matching, originalTime);
        Files.setLastModifiedTime(changed, originalTime);

        new RuntimeArchive(jar).extractOverrides(destination);

        assertEquals("same content", Files.readString(matching));
        assertEquals(originalTime, Files.getLastModifiedTime(matching));
        assertEquals("new content", Files.readString(changed));
        assertTrue(Files.getLastModifiedTime(changed).compareTo(originalTime) > 0);
    }

    @Test
    void refusesOverridePathsOutsideTheDestination() throws Exception {
        Path jar = temporaryDirectory.resolve("unsafe.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            entry(output, "overrides/../escaped.txt", "unsafe");
        }

        Path destination = temporaryDirectory.resolve("game");
        RuntimeArchive archive = new RuntimeArchive(jar);

        assertThrows(IOException.class, () -> archive.extractOverrides(destination));
        assertTrue(Files.notExists(temporaryDirectory.resolve("escaped.txt")));
    }

    private static void entry(JarOutputStream output, String name, String content)
            throws Exception {
        output.putNextEntry(new JarEntry(name));
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
    }
}
