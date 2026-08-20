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
import java.time.Instant;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

class RuntimeArchiveTest {
    @TempDir Path temporaryDirectory;

    @Test
    void extractsTheOverridesTreeAndSkipsUnchangedFiles() throws Exception {
        Path jar = temporaryDirectory.resolve("runtime.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            entry(output, "overrides/libraries/example.jar", "library");
            entry(output, "overrides/assets/indexes/26.json", "{}");
            entry(output, "overrides/versions/26.1/26.1.jar", "client");
            entry(output, "overrides/config/new-file.txt", "automatic");
            entry(output, "unrelated.txt", "ignored");
        }

        Path destination = temporaryDirectory.resolve("game");
        RuntimeArchive archive = new RuntimeArchive(jar);
        archive.extractOverrides(destination);

        assertTrue(Files.isRegularFile(destination.resolve("libraries/example.jar")));
        assertTrue(Files.isRegularFile(destination.resolve("assets/indexes/26.json")));
        assertTrue(Files.isRegularFile(destination.resolve("versions/26.1/26.1.jar")));
        assertTrue(Files.isRegularFile(destination.resolve("config/new-file.txt")));
        assertTrue(Files.notExists(destination.resolve("unrelated.txt")));

        Path unchanged = destination.resolve("libraries/example.jar");
        FileTime modified = FileTime.from(Instant.parse("2000-01-01T00:00:00Z"));
        Files.setLastModifiedTime(unchanged, modified);
        archive.extractOverrides(destination);
        assertEquals(modified, Files.getLastModifiedTime(unchanged));

        Files.writeString(unchanged, "corrupt", StandardCharsets.UTF_8);
        archive.extractOverrides(destination);
        assertTrue("library".equals(Files.readString(unchanged, StandardCharsets.UTF_8)));
        assertThrows(IOException.class, () -> archive.readFile("missing-file"));
    }

    private static void entry(JarOutputStream output, String name, String content)
            throws Exception {
        output.putNextEntry(new JarEntry(name));
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
    }
}
