package com.pigxity.portablemc.library;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LibraryClassifierTest {
    @Test
    void matchesClassifierRequirementsAgainstArchitectureAliases() {
        assertSupports("example:library:1.0", "unknown");
        assertSupports("example:library:1.0:natives-windows-arm64", "aarch64");
        assertSupports("example:library:1.0:natives-linux-aarch_64", "arm64");
        assertSupports("example:library:1.0:natives-windows-x86_64", "amd64");
        assertSupports("example:library:1.0:natives-windows-x86", "i686");
        assertSupports("example:library:1.0:natives-windows", "x86_64");

        assertDoesNotSupport("example:library:1.0:natives-windows-arm64", "amd64");
        assertDoesNotSupport("example:library:1.0:natives-windows-x86_64", "aarch64");
        assertDoesNotSupport("example:library:1.0:natives-windows-x86", "amd64");
        assertDoesNotSupport("example:library:1.0:natives-windows", "aarch64");
    }

    private static void assertSupports(String coordinate, String architecture) {
        assertTrue(classifier(coordinate).supports(architecture));
    }

    private static void assertDoesNotSupport(String coordinate, String architecture) {
        assertFalse(classifier(coordinate).supports(architecture));
    }

    private static LibraryClassifier classifier(String coordinate) {
        return LibraryClassifier.fromCoordinate(coordinate);
    }
}
