package com.pigxity.portablemc.platform;

import com.pigxity.portablemc.Main;
import com.pigxity.portablemc.shared.ContentHashes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class RuntimeArchive {
    private static final String OVERRIDES_PREFIX = "overrides/";
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

    public void extractOverrides(Path destination) throws IOException {
        final long startTime = System.nanoTime();
        int replacedFiles = 0;
        int skippedFiles = 0;

        Path root = destination.toAbsolutePath().normalize();
        Files.createDirectories(root);

        try (JarFile jar = new JarFile(archive.toFile())) {
            var entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.isDirectory() || !entry.getName().startsWith(OVERRIDES_PREFIX)) {
                    continue;
                }

                String relativeName = entry.getName().substring(OVERRIDES_PREFIX.length());
                if (relativeName.isEmpty()) {
                    continue;
                }

                Path output = root.resolve(relativeName).normalize();

                if (!output.startsWith(root)) {
                    throw new IOException(
                            "Refusing to extract unsafe JAR entry: " + entry.getName());
                }

                if (!matches(jar, entry, output)) {
                    Files.createDirectories(output.getParent());
                    try (InputStream input = jar.getInputStream(entry)) {
                        Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
                    }

                    replacedFiles++;
                    continue;
                }

                skippedFiles++;
            }
        }

        final long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;
        final int total = skippedFiles + replacedFiles;

        Main.log(String.format("Processed %d files in %dms; %d skipped (cached), %d replaced/extracted",
                total, elapsedMillis, skippedFiles, replacedFiles));
    }

    private static boolean matches(JarFile jar, JarEntry entry, Path output) throws IOException {
        if (!Files.isRegularFile(output) || Files.size(output) != entry.getSize()) {
            return false;
        }

        try (InputStream bundled = jar.getInputStream(entry);
                InputStream existing = Files.newInputStream(output)) {
            return ContentHashes.sha256Matches(bundled, existing);
        }
    }

    public String readFile(String filename) throws IOException {
        try (JarFile jar = new JarFile(archive.toFile())) {
            JarEntry entry = jar.getJarEntry(filename);

            if (entry == null) {
                throw new IOException("Bundled resource " + filename + " is missing");
            }

            try (InputStream input = jar.getInputStream(entry)) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }
}
