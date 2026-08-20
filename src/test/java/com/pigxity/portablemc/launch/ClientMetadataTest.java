package com.pigxity.portablemc.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class ClientMetadataTest {
    @Test
    void parsesAllRuntimeMetadataFromProperties() {
        ClientMetadata metadata =
                ClientMetadata.parse(
                        """
                        mainClass=net.minecraft.client.main.Main
                        version=26.2
                        versionType=release
                        assetIndex=32
                        clientJarPath=./versions/26.2/client.jar
                        """);

        assertEquals("net.minecraft.client.main.Main", metadata.mainClass());
        assertEquals("26.2", metadata.version());
        assertEquals("release", metadata.versionType());
        assertEquals("32", metadata.assetIndex());
        assertEquals(Path.of("./versions/26.2/client.jar"), metadata.clientJarPath());
    }

    @Test
    void rejectsMissingMetadataInsteadOfFallingBackToPackageRules() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ClientMetadata.parse("mainClass=example.Main\n"));
    }
}
