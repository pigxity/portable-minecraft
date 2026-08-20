package com.pigxity.portablemc.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

class RuntimeArchiveTest {
    @TempDir Path temporaryDirectory;

    @Test
    void extractsOnlyRuntimeTreesAndReadsMetadata() throws Exception {
        Path jar = temporaryDirectory.resolve("runtime.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            entry(output, "libraries/example.jar", "library");
            entry(output, "assets/indexes/26.json", "{}");
            entry(output, "versions/26.1/client-26.1.jar", "client");
            entry(output, "packagerules.json", "{\"version\":\"26.1\"}");
            entry(output, "main-class", "example.Main");
            entry(output, "unrelated.txt", "ignored");
        }

        Path destination = temporaryDirectory.resolve("game");
        RuntimeArchive archive = new RuntimeArchive(jar);
        archive.extractRuntimeTrees(destination);

        assertTrue(Files.isRegularFile(destination.resolve("libraries/example.jar")));
        assertTrue(Files.isRegularFile(destination.resolve("assets/indexes/26.json")));
        assertTrue(Files.isRegularFile(destination.resolve("versions/26.1/client-26.1.jar")));
        assertTrue(Files.notExists(destination.resolve("unrelated.txt")));
        assertEquals("example.Main", archive.readFile("main-class"));
        assertEquals(
                "26.1",
                JsonParser.parseString(archive.readFile("packagerules.json"))
                        .getAsJsonObject()
                        .get("version")
                        .getAsString());
        assertThrows(IOException.class, () -> archive.readFile("missing-file"));
    }

    private static void entry(JarOutputStream output, String name, String content)
            throws Exception {
        output.putNextEntry(new JarEntry(name));
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
    }
}
