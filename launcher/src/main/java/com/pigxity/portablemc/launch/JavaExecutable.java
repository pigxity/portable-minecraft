package com.pigxity.portablemc.launch;

import java.nio.file.Files;
import java.nio.file.Path;

final class JavaExecutable {
    private JavaExecutable() {}

    static Path current() {
        Path bin = Path.of(System.getProperty("java.home"), "bin");
        Path executable = bin.resolve(isWindows() ? "java.exe" : "java");
        if (!Files.isRegularFile(executable)) {
            throw new IllegalStateException("Java executable is missing: " + executable);
        }
        return executable.toAbsolutePath().normalize();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
