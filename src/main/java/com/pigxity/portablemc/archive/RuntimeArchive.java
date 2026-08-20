package com.pigxity.portablemc.archive;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class RuntimeArchive {
    private static final List<String> RUNTIME_PREFIXES =
            List.of("libraries/", "assets/", "versions/");
    private final Path archive;

    public RuntimeArchive(Path archive) {
        this.archive = archive.toAbsolutePath().normalize();
    }

    public static RuntimeArchive current() throws IOException {
        try {
            Path location =
                    Path.of(
                            RuntimeArchive.class
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI());
            if (!Files.isRegularFile(location)) {
                throw new IOException(
                        "The runtime loader must be launched from its bundled JAR: " + location);
            }
            return new RuntimeArchive(location);
        } catch (URISyntaxException exception) {
            throw new IOException("Could not locate the bundled JAR", exception);
        }
    }

    public void extractRuntimeTrees(Path destination) throws IOException {
        Path root = destination.toAbsolutePath().normalize();
        Files.createDirectories(root);
        try (JarFile jar = new JarFile(archive.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()
                        || RUNTIME_PREFIXES.stream().noneMatch(entry.getName()::startsWith)) {
                    continue;
                }
                Path output = root.resolve(entry.getName()).normalize();
                if (!output.startsWith(root)) {
                    throw new IOException(
                            "Refusing to extract unsafe JAR entry: " + entry.getName());
                }
                Files.createDirectories(output.getParent());
                try (InputStream input = jar.getInputStream(entry)) {
                    Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public String readMainClass() throws IOException {
        try (JarFile jar = new JarFile(archive.toFile())) {
            JarEntry entry = jar.getJarEntry("main-class");
            if (entry == null) {
                throw new IOException("Bundled resource main-class is missing");
            }
            try (InputStream input = jar.getInputStream(entry)) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
        }
    }

    public JsonObject readPackageRules() throws IOException {
        try (JarFile jar = new JarFile(archive.toFile())) {
            List<JarEntry> matches = new ArrayList<>();
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().matches("packagerules-[^/]+\\.json")) {
                    matches.add(entry);
                }
            }
            if (matches.size() != 1) {
                throw new IOException(
                        "Expected exactly one packagerules-{version}.json resource but found "
                                + matches.size());
            }
            try (InputStreamReader reader =
                    new InputStreamReader(
                            jar.getInputStream(matches.getFirst()), StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
    }
}
