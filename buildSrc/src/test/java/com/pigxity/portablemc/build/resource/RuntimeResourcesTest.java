package com.pigxity.portablemc.build.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pigxity.portablemc.build.cache.MinecraftCache;
import com.pigxity.portablemc.build.model.MinecraftPackage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

class RuntimeResourcesTest {
    @TempDir Path temporaryDirectory;

    @Test
    void preparesMetadataAndAllMinecraftTreesUnderOverrides() throws Exception {
        MinecraftCache cache = new MinecraftCache(temporaryDirectory.resolve("cache"));
        Path output = temporaryDirectory.resolve("generated-resources");
        JsonObject packageJson =
                JsonParser.parseString(
                                """
                                {
                                  "id":"26.2",
                                  "type":"release",
                                  "mainClass":"net.minecraft.client.main.Main",
                                  "downloads":{"client":{"url":"https://invalid/client","sha1":"clienthash"}},
                                  "assetIndex":{"id":"26","url":"https://invalid/index","sha1":"indexhash"},
                                  "arguments":{"jvm":[],"game":[]},
                                  "libraries":[{
                                    "name":"example:library:1.0",
                                    "downloads":{"artifact":{
                                      "path":"example/library/1.0/library-1.0.jar",
                                      "url":"https://invalid/library",
                                      "sha1":"libraryhash"
                                    }}
                                  }]
                                }
                                """)
                        .getAsJsonObject();
        write(cache.client("clienthash"), "client");
        write(cache.assetIndex("indexhash"), "{\"objects\":{}}");
        write(cache.library("example/library/1.0/library-1.0.jar"), "library");
        write(cache.packageRules("26.2"), "{\"arguments\":{},\"libraries\":[]}");
        MinecraftPackage minecraftPackage =
                new MinecraftPackage(
                        "26.2",
                        "packagehash",
                        "https://invalid/package",
                        Path.of("package.json"),
                        packageJson);

        RuntimeResources.prepare(cache, minecraftPackage, output);

        assertTrue(Files.isRegularFile(output.resolve("clientmeta.properties")));
        assertTrue(Files.isRegularFile(output.resolve("packagerules.json")));
        assertEquals(
                "client",
                Files.readString(output.resolve("overrides/versions/26.2/client.jar")));
        assertEquals(
                "library",
                Files.readString(
                        output.resolve(
                                "overrides/libraries/example/library/1.0/library-1.0.jar")));
        assertTrue(
                Files.isRegularFile(output.resolve("overrides/assets/indexes/26.json")));
        assertFalse(Files.exists(output.resolve("versions")));
        assertFalse(Files.exists(output.resolve("assets")));
        assertFalse(Files.exists(output.resolve("libraries")));
        Properties metadata = new Properties();
        try (Reader reader =
                Files.newBufferedReader(output.resolve("clientmeta.properties"))) {
            metadata.load(reader);
        }
        assertEquals("./versions/26.2/client.jar", metadata.getProperty("clientJarPath"));
    }

    private static void write(Path path, String content) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}
