package com.pigxity.portablemc.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pigxity.portablemc.platform.OperatingSystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

class MinecraftLaunchPlanTest {
    @TempDir Path gameDirectory;

    @Test
    void combinesMetadataRulesAndEveryStructuredCliArgumentType() throws Exception {
        Path client = gameDirectory.resolve("versions/26.2/26.2.jar");
        Files.createDirectories(client.getParent());
        Files.createFile(client);
        ClientMetadata metadata =
                new ClientMetadata(
                        "net.minecraft.client.main.Main",
                        "26.2",
                        "release",
                        "32",
                        Path.of("./versions/26.2/26.2.jar"));
        JsonObject packageRules =
                JsonParser.parseString(
                                """
                                {
                                  "arguments":{
                                    "jvm":[
                                      "-Xmx2G",
                                      {"rules":[{"action":"allow","features":{"is_demo_user":true}}],"value":"-Ddemo=true"},
                                      "-cp","${classpath}"
                                    ],
                                    "game":[
                                      "--username","${auth_player_name}",
                                      {"rules":[{"action":"allow","features":{"is_demo_user":true}}],"value":"--demo"}
                                    ]
                                  },
                                  "libraries":[]
                                }
                                """)
                        .getAsJsonObject();
        CommandLineArguments cli =
                new CommandLineArguments(
                        Map.of("is_demo_user", true),
                        Map.of("auth_player_name", "Player1"),
                        List.of("-Xmx8G"),
                        List.of("--uuid", "xxxxx"));

        MinecraftLaunchPlan plan =
                MinecraftLaunchPlan.create(
                        gameDirectory,
                        metadata,
                        packageRules,
                        cli,
                        new OperatingSystem("windows", "10.0", "amd64"));

        assertEquals("net.minecraft.client.main.Main", plan.mainClass());
        assertTrue(plan.jvmArguments().contains("-Xmx8G"));
        assertFalse(plan.jvmArguments().contains("-Xmx2G"));
        assertTrue(plan.jvmArguments().contains("-Ddemo=true"));
        assertTrue(plan.jvmArguments().contains(client.toAbsolutePath().normalize().toString()));
        assertEquals(
                List.of("--username", "Player1", "--demo", "--uuid", "xxxxx"),
                plan.minecraftArguments());
    }
}
