package com.pigxity.portablemc.build.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

class ClientMetadataTest {
    @Test
    void createsRuntimeMetadataIncludingTheVersionedClientJarPath() {
        JsonObject packageJson =
                JsonParser.parseString(
                                """
                                {
                                  "id":"26.2",
                                  "type":"release",
                                  "mainClass":"net.minecraft.client.main.Main",
                                  "assetIndex":{"id":"26"}
                                }
                                """)
                        .getAsJsonObject();

        var metadata = ClientMetadata.create(packageJson);

        assertEquals("net.minecraft.client.main.Main", metadata.getProperty("mainClass"));
        assertEquals("26.2", metadata.getProperty("version"));
        assertEquals("release", metadata.getProperty("versionType"));
        assertEquals("26", metadata.getProperty("assetIndex"));
        assertEquals("./versions/26.2/26.2.jar", metadata.getProperty("clientJarPath"));
    }
}
