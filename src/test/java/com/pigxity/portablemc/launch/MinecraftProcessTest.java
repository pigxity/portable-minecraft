package com.pigxity.portablemc.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

class MinecraftProcessTest {
    @Test
    void buildsASeparateJavaCommandWithJvmClasspathMainAndMinecraftArguments() {
        List<String> command =
                MinecraftProcess.command(
                        Path.of("java"),
                        List.of("-Xmx8G", "-cp", "one.jar;two.jar"),
                        "net.minecraft.client.main.Main",
                        List.of("--uuid", "xxxxx"));

        assertEquals(
                List.of(
                        Path.of("java").toAbsolutePath().normalize().toString(),
                        "-Xmx8G",
                        "-cp",
                        "one.jar;two.jar",
                        "net.minecraft.client.main.Main",
                        "--uuid",
                        "xxxxx"),
                command);
    }
}
