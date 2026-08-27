package com.pigxity.portablemc.launch.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.RuleParser;
import com.pigxity.portablemc.rule.RuleResolver;
import com.pigxity.portablemc.rule.UnsupportedRuleException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

class LibraryClasspathTest {
    @TempDir Path gameDirectory;

    @Test
    void includesEveryLibraryAllowedByMojangRules() throws Exception {
        String normal = "example/normal/1.0/normal-1.0.jar";
        String windowsX86 = "example/native/1.0/native-1.0-natives-windows-x86.jar";
        String windowsArm64 = "example/native/1.0/native-1.0-natives-windows-arm64.jar";
        String linux = "example/native/1.0/native-1.0-natives-linux.jar";
        for (String libraryPath : new String[] {normal, windowsX86, windowsArm64, linux}) {
            Path path = gameDirectory.resolve("libraries").resolve(libraryPath);
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }

        RuleEnvironment environment =
                new RuleEnvironment(new OperatingSystem("windows", "10.0", "amd64"), Map.of());
        LibraryClasspath classpath =
                new LibraryClasspath(gameDirectory, new RuleParser(), new RuleResolver(environment));

        var paths =
                classpath.resolve(
                        JsonParser.parseString(
                                        """
                                        [
                                          {"path":"example/normal/1.0/normal-1.0.jar","rules":[]},
                                          {"path":"example/native/1.0/native-1.0-natives-windows-x86.jar","rules":[{"action":"allow","os":{"name":"windows"}}]},
                                          {"path":"example/native/1.0/native-1.0-natives-windows-arm64.jar","rules":[{"action":"allow","os":{"name":"windows"}}]},
                                          {"path":"example/native/1.0/native-1.0-natives-linux.jar","rules":[{"action":"allow","os":{"name":"linux"}}]}
                                        ]
                                        """)
                                .getAsJsonArray());

        assertEquals(
                List.of(
                        gameDirectory.resolve("libraries").resolve(normal).toAbsolutePath().normalize(),
                        gameDirectory
                                .resolve("libraries")
                                .resolve(windowsX86)
                                .toAbsolutePath()
                                .normalize(),
                        gameDirectory
                                .resolve("libraries")
                                .resolve(windowsArm64)
                                .toAbsolutePath()
                                .normalize()),
                paths);
    }

    @Test
    void rejectsUnsupportedMojangRules() {
        RuleEnvironment environment =
                new RuleEnvironment(new OperatingSystem("windows", "10.0", "amd64"), Map.of());
        LibraryClasspath classpath =
                new LibraryClasspath(gameDirectory, new RuleParser(), new RuleResolver(environment));

        assertThrows(
                UnsupportedRuleException.class,
                () ->
                        classpath.resolve(
                                JsonParser.parseString(
                                                """
                                                [{"path":"example/native/1.0/native-1.0-natives-linux.jar","rules":[{"action":"future"}]}]
                                                """)
                                        .getAsJsonArray()));
    }
}
