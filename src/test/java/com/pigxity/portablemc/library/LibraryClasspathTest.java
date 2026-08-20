package com.pigxity.portablemc.library;

import com.google.gson.JsonParser;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.RuleResolver;
import com.pigxity.portablemc.rule.UnsupportedRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LibraryClasspathTest {
    @TempDir
    Path gameDirectory;

    @Test
    void includesOnlyRuleAndArchitectureCompatibleLibraries() throws Exception {
        String normal = "example:normal:1.0";
        String windows = "example:native:1.0:natives-windows";
        String arm64 = "example:native:1.0:natives-windows-arm64";
        for (String coordinate : new String[]{normal, windows, arm64}) {
            Path path = gameDirectory.resolve("libraries").resolve(MavenCoordinates.libraryPath(coordinate));
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
        RuleEnvironment environment = new RuleEnvironment(
                new OperatingSystem("windows", "10.0", "amd64"), Map.of());
        LibraryClasspath classpath = new LibraryClasspath(gameDirectory, new RuleResolver(environment), environment.operatingSystem());

        var paths = classpath.resolve(JsonParser.parseString("""
                [
                  {"name":"example:normal:1.0","rules":[]},
                  {"name":"example:native:1.0:natives-windows","rules":[{"action":"allow","os":{"name":"windows"}}]},
                  {"name":"example:native:1.0:natives-windows-arm64","rules":[{"action":"allow","os":{"name":"windows"}}]}
                ]
                """).getAsJsonArray());

        assertEquals(2, paths.size());
    }

    @Test
    void validatesRulesEvenForAnIncompatibleClassifier() {
        RuleEnvironment environment = new RuleEnvironment(
                new OperatingSystem("windows", "10.0", "amd64"), Map.of());
        LibraryClasspath classpath = new LibraryClasspath(gameDirectory, new RuleResolver(environment), environment.operatingSystem());
        assertThrows(UnsupportedRuleException.class, () -> classpath.resolve(JsonParser.parseString("""
                [{"name":"example:native:1.0:natives-linux","rules":[{"action":"future"}]}]
                """).getAsJsonArray()));
    }
}
