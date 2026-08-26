package com.pigxity.portablemc.shared;

import java.nio.file.Path;
import java.util.Map;

public record ClientMetadata(
        String mainClass,
        String version,
        String versionType,
        String assetIndex,
        String clientJarPath) {

    public static final String FILE_NAME = "clientmeta.properties";

    public static ClientMetadata create(
            String mainClass,
            String version,
            String versionType,
            String assetIndex) {
        return new ClientMetadata(
                mainClass,
                version,
                versionType,
                assetIndex,
                "./versions/" + version + "/" + version + ".jar");
    }

    public static ClientMetadata fromMap(Map<String, String> properties) {
        return new ClientMetadata(
                properties.get("mainClass"),
                properties.get("version"),
                properties.get("versionType"),
                properties.get("assetIndex"),
                properties.get("clientJarPath"));
    }

    public Path resolveClientJar(Path workingDirectory) {
        Path root = workingDirectory.toAbsolutePath().normalize();
        Path clientJar = root.resolve(clientJarPath).normalize();

        if (!clientJar.startsWith(root)) {
            throw new IllegalArgumentException("clientJarPath points outside the working directory: " + clientJarPath);
        }

        return clientJar;
    }

    public Map<String, String> toMap() {
        return Map.of(
                "mainClass", mainClass,
                "version", version,
                "versionType", versionType,
                "assetIndex", assetIndex,
                "clientJarPath", clientJarPath);
    }
}