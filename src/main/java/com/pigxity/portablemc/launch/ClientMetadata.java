package com.pigxity.portablemc.launch;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Properties;

public record ClientMetadata(
        String mainClass,
        String version,
        String versionType,
        String assetIndex,
        Path clientJarPath) {
    public static ClientMetadata parse(String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read clientmeta.properties", exception);
        }

        Path clientJarPath = Path.of(required(properties, "clientJarPath"));
        Path normalizedClientPath = clientJarPath.normalize();
        if (clientJarPath.isAbsolute()
                || normalizedClientPath.startsWith("..")) {
            throw new IllegalArgumentException(
                    "clientJarPath must stay within the game directory: " + clientJarPath);
        }
        return new ClientMetadata(
                required(properties, "mainClass"),
                required(properties, "version"),
                required(properties, "versionType"),
                required(properties, "assetIndex"),
                clientJarPath);
    }

    private static String required(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "clientmeta.properties is missing required property " + name);
        }
        return value;
    }
}
