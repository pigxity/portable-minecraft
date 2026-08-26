package com.pigxity.portablemc.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class ClientMetadataTest {
    @TempDir Path temporaryDirectory;

    @Test
    void createsVersionBasedClientPathAndRoundTripsTheMetadataFile() throws Exception {
        ClientMetadata expected =
                ClientMetadata.create(
                        "net.minecraft.client.main.Main", "26.2", "release", "32");
        Path file = temporaryDirectory.resolve("nested").resolve(ClientMetadata.FILE_NAME);

        PropertiesFile.write(file, expected.toMap());

        assertEquals("./versions/26.2/26.2.jar", expected.clientJarPath());
        assertEquals(expected, ClientMetadata.fromMap(PropertiesFile.read(file)));
    }

    @Test
    void parsesBundledMetadataAndResolvesConfiguredClientJar() throws Exception {
        ClientMetadata metadata =
                ClientMetadata.fromMap(
                        PropertiesFile.parse(
                                """
                                mainClass=example.Main
                                version=26.2
                                versionType=release
                                assetIndex=32
                                clientJarPath=./versions/26.2/custom-name.jar
                                """));

        assertEquals(
                temporaryDirectory.resolve("versions/26.2/custom-name.jar").toAbsolutePath(),
                metadata.resolveClientJar(temporaryDirectory));
    }

    @Test
    void parsesCharactersWithPropertiesSyntax() throws Exception {
        ClientMetadata metadata =
                ClientMetadata.fromMap(
                        PropertiesFile.parse(
                                """
                                mainClass=example\\:Main\\#1
                                version=26.2
                                versionType=release=test
                                assetIndex=index\\\\with-tab\t
                                clientJarPath=./versions/26.2/26.2.jar
                                """));

        assertEquals("example:Main#1", metadata.mainClass());
        assertEquals("release=test", metadata.versionType());
        assertEquals("index\\with-tab\t", metadata.assetIndex());
    }

    @Test
    void rejectsPathsOutsideWorkingDirectory() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ClientMetadata(
                                        "example.Main",
                                        "26.2",
                                        "release",
                                        "32",
                                        "../outside.jar")
                                .resolveClientJar(temporaryDirectory));
    }
}
