package com.pigxity.portablemc.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class ClientMetadataTest {
    @TempDir Path temporaryDirectory;

    @Test
    void createsVersionBasedClientPathAndRoundTripsTheMetadataFile() throws Exception {
        ClientMetadata expected =
                ClientMetadata.create(
                        "net.minecraft.client.main.Main", "26.2", "release", "32");
        Path file = temporaryDirectory.resolve("nested").resolve(ClientMetadata.FILE_NAME);

        expected.write(file);

        assertEquals("./versions/26.2/26.2.jar", expected.clientJarPath());
        assertEquals(expected, ClientMetadata.read(file));
    }

    @Test
    void parsesBundledMetadataAndResolvesConfiguredClientJar() throws Exception {
        ClientMetadata metadata =
                ClientMetadata.parse(
                        """
                        mainClass=example.Main
                        version=26.2
                        versionType=release
                        assetIndex=32
                        clientJarPath=./versions/26.2/custom-name.jar
                        """);

        assertEquals(
                temporaryDirectory.resolve("versions/26.2/custom-name.jar").toAbsolutePath(),
                metadata.resolveClientJar(temporaryDirectory));
    }

    @Test
    void roundTripsCharactersWithPropertiesSyntax() throws Exception {
        ClientMetadata expected =
                new ClientMetadata(
                        " example:Main#1",
                        "26.2",
                        "release=test",
                        "index\\with-tab\t",
                        "./versions/26.2/26.2.jar");
        Path file = temporaryDirectory.resolve("special.properties");

        expected.write(file);

        assertEquals(expected, ClientMetadata.read(file));
    }

    @Test
    void rejectsMissingPropertiesAndPathsOutsideWorkingDirectory() {
        assertThrows(IOException.class, () -> ClientMetadata.parse("version=26.2\n"));

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
