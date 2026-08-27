package com.pigxity.portablemc.launch.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

class MinecraftProcessTest {
    @TempDir Path workingDirectory;

    @Test
    void placesPackageJvmArgumentsBeforeMainClassAndMinecraftArgumentsAfterIt() {
        MinecraftProcess process =
                MinecraftProcess.create(
                        Path.of("java"),
                        workingDirectory,
                        List.of("-Djava.library.path=natives", "-cp", "library.jar;client.jar", "-Xmx2G"),
                        "net.minecraft.client.main.Main",
                        List.of("--username", "Player"));

        assertEquals(
                List.of(
                        "java",
                        "-Djava.library.path=natives",
                        "-cp",
                        "library.jar;client.jar",
                        "-Xmx2G",
                        "net.minecraft.client.main.Main",
                        "--username",
                        "Player"),
                process.command());
    }
}
