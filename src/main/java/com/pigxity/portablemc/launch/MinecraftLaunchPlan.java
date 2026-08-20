package com.pigxity.portablemc.launch;

import com.google.gson.JsonObject;
import com.pigxity.portablemc.library.LibraryClasspath;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.RuleParser;
import com.pigxity.portablemc.rule.RuleResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record MinecraftLaunchPlan(
        List<String> jvmArguments, String mainClass, List<String> minecraftArguments) {
    public MinecraftLaunchPlan {
        jvmArguments = List.copyOf(jvmArguments);
        minecraftArguments = List.copyOf(minecraftArguments);
    }

    public static MinecraftLaunchPlan create(
            Path workingDirectory,
            ClientMetadata metadata,
            JsonObject packageRules,
            CommandLineArguments commandLine,
            OperatingSystem operatingSystem)
            throws IOException {
        Path gameDirectory = workingDirectory.toAbsolutePath().normalize();
        RuleEnvironment environment =
                new RuleEnvironment(operatingSystem, commandLine.features());
        RuleParser ruleParser = new RuleParser();
        RuleResolver ruleResolver = new RuleResolver(environment);

        List<Path> classpath =
                new ArrayList<>(
                        new LibraryClasspath(
                                        gameDirectory,
                                        ruleParser,
                                        ruleResolver,
                                        operatingSystem)
                                .resolve(packageRules.getAsJsonArray("libraries")));
        Path client = gameDirectory.resolve(metadata.clientJarPath()).normalize();
        if (!client.startsWith(gameDirectory) || !Files.isRegularFile(client)) {
            throw new IOException("Minecraft client is missing: " + client);
        }
        classpath.add(client.toAbsolutePath().normalize());

        Map<String, String> substitutions =
                substitutions(gameDirectory, metadata, classpath);
        substitutions.putAll(commandLine.substitutions());
        LaunchArguments arguments =
                new LaunchArguments(ruleParser, ruleResolver, substitutions);
        JsonObject definitions = packageRules.getAsJsonObject("arguments");
        List<String> jvmArguments =
                JvmArguments.merge(
                        arguments.resolve(definitions.getAsJsonArray("jvm")),
                        commandLine.jvmArguments());
        List<String> minecraftArguments =
                new ArrayList<>(arguments.resolve(definitions.getAsJsonArray("game")));
        minecraftArguments.addAll(commandLine.minecraftArguments());
        return new MinecraftLaunchPlan(jvmArguments, metadata.mainClass(), minecraftArguments);
    }

    private static Map<String, String> substitutions(
            Path gameDirectory, ClientMetadata metadata, List<Path> classpath) {
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
        values.put("natives_directory", gameDirectory.resolve("natives").toString());
        values.put("launcher_name", "portable-minecraft");
        values.put("launcher_version", "1.0");
        values.put(
                "classpath",
                String.join(
                        File.pathSeparator,
                        classpath.stream().map(Path::toString).toList()));
        return values;
    }
}
