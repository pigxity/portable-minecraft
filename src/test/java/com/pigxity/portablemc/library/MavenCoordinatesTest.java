package com.pigxity.portablemc.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class MavenCoordinatesTest {
    @Test
    void mapsArtifactsAndClassifiersToLibraryPaths() {
        assertEquals(
                Path.of("org/lwjgl/lwjgl/3.4.1/lwjgl-3.4.1.jar"),
                MavenCoordinates.libraryPath("org.lwjgl:lwjgl:3.4.1"));
        assertEquals(
                Path.of("org/lwjgl/lwjgl/3.4.1/lwjgl-3.4.1-natives-windows-arm64.jar"),
                MavenCoordinates.libraryPath("org.lwjgl:lwjgl:3.4.1:natives-windows-arm64"));
        assertThrows(
                IllegalArgumentException.class,
                () -> MavenCoordinates.libraryPath("not:a:coordinate:with:too:many"));
    }
}
