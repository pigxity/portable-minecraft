package com.pigxity.portablemc.launch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pigxity.portablemc.Main;
import com.pigxity.portablemc.platform.RuntimeArchive;
import com.pigxity.portablemc.launch.system.JavaExecutable;
import com.pigxity.portablemc.launch.system.LibraryClasspath;
import com.pigxity.portablemc.launch.system.MinecraftProcess;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.RuleParser;
import com.pigxity.portablemc.rule.RuleResolver;
import com.pigxity.portablemc.shared.ClientMetadata;
import com.pigxity.portablemc.shared.PropertiesFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MinecraftLauncher {
    private static Map<String, String> substitutions(
            Path gameDirectory, Path natives, List<Path> classpath, ClientMetadata metadata) {
        Map<String, String> values = new LinkedHashMap<>();

        values.put("auth_player_name", "Player");
        values.put("version_name", metadata.version());
        values.put("game_directory", gameDirectory.toString());
        values.put("assets_root", gameDirectory.resolve("assets").toString());
        values.put("assets_index_name", metadata.assetIndex());
        values.put("auth_uuid", "00000000000000000000000000000000");
        values.put("auth_access_token", "0");
        values.put("clientid", "");
        values.put("auth_xuid", "");
        values.put("version_type", metadata.versionType());
        values.put("natives_directory", natives.toString());
        values.put("launcher_name", "portable-minecraft");
        values.put("launcher_version", "1.0");
        values.put(
                "classpath",
                String.join(File.pathSeparator, classpath.stream().map(Path::toString).toList()));

        return values;
    }

    public void launch(Path workingDirectory, String[] minecraftArguments) throws Exception {
        launch(workingDirectory, LaunchOptions.withMinecraftArguments(minecraftArguments));
    }

    public void launch(Path workingDirectory, LaunchOptions additionalArguments) throws Exception {
        Path gameDirectory = workingDirectory.toAbsolutePath().normalize();
        RuntimeArchive archive = RuntimeArchive.current();

        Main.log("Extracting required files");

        archive.extractOverrides(gameDirectory);

        JsonObject packageRules =
                JsonParser.parseString(archive.readFile("packagerules.json")).getAsJsonObject();
        ClientMetadata metadata =
                ClientMetadata.fromMap(PropertiesFile.parse(archive.readFile(ClientMetadata.FILE_NAME)));

        RuleEnvironment environment = RuleEnvironment.current();
        RuleParser ruleParser = new RuleParser();
        RuleResolver ruleResolver = new RuleResolver(environment);

        Main.log("Loading libraries, OS: " + environment.operatingSystem());

        List<Path> classpath =
                new ArrayList<>(
                        new LibraryClasspath(
                                gameDirectory,
                                ruleParser,
                                ruleResolver)
                                .resolve(packageRules.getAsJsonArray("libraries")));

        Path client = metadata.resolveClientJar(gameDirectory);
        if (!Files.isRegularFile(client)) {
            throw new IllegalStateException("Minecraft client is missing: " + client);
        }

        classpath.add(client.toAbsolutePath().normalize());

        Path natives = gameDirectory.resolve("natives");

        Files.createDirectories(natives);

        Map<String, String> substitutions =
                substitutions(gameDirectory, natives, classpath, metadata);
        LaunchArguments arguments = new LaunchArguments(ruleParser, ruleResolver, substitutions);
        JsonObject argumentDefinitions = packageRules.getAsJsonObject("arguments");

        LaunchOptions launchOptions =
                new LaunchOptions(
                        arguments.resolve(argumentDefinitions.getAsJsonArray("jvm")),
                        arguments.resolve(argumentDefinitions.getAsJsonArray("game")))
                        .merge(additionalArguments);

        Main.log("JVM arguments: " + launchOptions.jvmArguments());
        Main.log("Minecraft arguments: " + launchOptions.minecraftArguments());

        Main.log("Starting Minecraft in a new JVM!");

        int exitCode =
                MinecraftProcess.create(
                                JavaExecutable.current(),
                                gameDirectory,
                                launchOptions.jvmArguments(),
                                metadata.mainClass(),
                                launchOptions.minecraftArguments())
                        .start()
                        .waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Minecraft exited with code " + exitCode);
        }
    }

}
