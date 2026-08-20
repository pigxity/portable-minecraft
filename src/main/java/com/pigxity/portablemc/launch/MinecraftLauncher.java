package com.pigxity.portablemc.launch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pigxity.portablemc.Main;
import com.pigxity.portablemc.archive.RuntimeArchive;
import com.pigxity.portablemc.platform.OperatingSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftLauncher {
    public void launch(Path workingDirectory, CommandLineArguments commandLine) throws Exception {
        Path gameDirectory = workingDirectory.toAbsolutePath().normalize();
        RuntimeArchive archive = RuntimeArchive.current();

        Main.log("Extracting/verifying required files");

        archive.extractOverrides(gameDirectory);
        JsonObject packageRules =
                JsonParser.parseString(archive.readFile("packagerules.json")).getAsJsonObject();
        ClientMetadata metadata = ClientMetadata.parse(archive.readFile("clientmeta.properties"));

        Path natives = gameDirectory.resolve("natives");
        Files.createDirectories(natives);
        OperatingSystem operatingSystem = OperatingSystem.current();
        Main.log("Loading libraries, OS: " + operatingSystem);
        MinecraftLaunchPlan plan =
                MinecraftLaunchPlan.create(
                        gameDirectory, metadata, packageRules, commandLine, operatingSystem);

        Main.log("JVM arguments: " + plan.jvmArguments());
        Main.log("Minecraft arguments: " + plan.minecraftArguments());
        Main.log("Starting Minecraft in a new JVM!");
        int exitCode =
                MinecraftProcess.run(
                        gameDirectory,
                        plan.jvmArguments(),
                        plan.mainClass(),
                        plan.minecraftArguments());
        if (exitCode != 0) {
            throw new IOException("Minecraft exited with code " + exitCode);
        }
    }
}
