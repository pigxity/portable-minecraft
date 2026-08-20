package com.pigxity.portablemc.build.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class VerifiedDownloader {
    private static final int CONNECT_TIMEOUT_MILLIS = 30_000;
    private static final int READ_TIMEOUT_MILLIS = 120_000;

    public Path downloadVerified(String url, Path destination, String expectedSha1) throws IOException {
        Objects.requireNonNull(expectedSha1, "expectedSha1");
        if (Files.isRegularFile(destination) && expectedSha1.equalsIgnoreCase(Hashing.sha1(destination))) {
            return destination;
        }
        Files.deleteIfExists(destination);
        Path temporary = download(url, destination);
        try {
            String actualSha1 = Hashing.sha1(temporary);
            if (!expectedSha1.equalsIgnoreCase(actualSha1)) {
                throw new DownloadVerificationException(
                        "SHA-1 mismatch for " + url + ": expected " + expectedSha1 + " but received " + actualSha1);
            }
            moveIntoPlace(temporary, destination);
            return destination;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public Path downloadWithTtl(String url, Path destination, Duration ttl) throws IOException {
        if (Files.isRegularFile(destination)
                && Files.getLastModifiedTime(destination).toInstant().plus(ttl).isAfter(Instant.now())) {
            return destination;
        }
        Path temporary = download(url, destination);
        try {
            moveIntoPlace(temporary, destination);
            return destination;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Path download(String url, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        Path temporary = destination.resolveSibling(destination.getFileName() + ".part-" + UUID.randomUUID());
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        connection.setRequestProperty("User-Agent", "portable-minecraft-gradle-plugin/1");
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Download failed with HTTP " + responseCode + " for " + url);
            }
            try (InputStream input = connection.getInputStream()) {
                Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
            }
            return temporary;
        } catch (IOException exception) {
            Files.deleteIfExists(temporary);
            throw exception;
        } finally {
            connection.disconnect();
        }
    }

    private static void moveIntoPlace(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
