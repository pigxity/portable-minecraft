package com.pigxity.portablemc.build.model;

import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ClientMetadata {
    private ClientMetadata() {}

    public static Properties create(JsonObject packageJson) {
        String version = packageJson.get("id").getAsString();
        Properties metadata = new Properties();
        metadata.setProperty("mainClass", packageJson.get("mainClass").getAsString());
        metadata.setProperty("version", version);
        metadata.setProperty("versionType", packageJson.get("type").getAsString());
        metadata.setProperty(
                "assetIndex",
                packageJson.getAsJsonObject("assetIndex").get("id").getAsString());
        metadata.setProperty("clientJarPath", "./versions/" + version + "/client.jar");
        return metadata;
    }

    public static void write(Path path, Properties metadata) throws IOException {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (String key : metadata.stringPropertyNames().stream().sorted().toList()) {
                writer.write(key);
                writer.write('=');
                writer.write(metadata.getProperty(key));
                writer.newLine();
            }
        }
    }
}
