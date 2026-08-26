package com.pigxity.portablemc.launch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pigxity.portablemc.Main;
import com.pigxity.portablemc.archive.RuntimeArchive;
import com.pigxity.portablemc.library.LibraryClasspath;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.RuleParser;
import com.pigxity.portablemc.rule.RuleResolver;
import com.pigxity.portablemc.shared.ClientMetadata;
import com.pigxity.portablemc.shared.PropertiesFile;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class MinecraftLauncher {
    public void launch(Path workingDirectory, String[] minecraftArguments) throws Exception {
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
                                        ruleResolver,
                                        environment.operatingSystem())
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
        List<String> jvmArguments = arguments.resolve(argumentDefinitions.getAsJsonArray("jvm"));
        List<String> gameArguments =
                LaunchArguments.mergeArgs(
                        arguments.resolve(argumentDefinitions.getAsJsonArray("game")),
                        Arrays.asList(minecraftArguments));

        Main.log("JVM arguments: " + jvmArguments);
        Main.log("Minecraft arguments: " + gameArguments);

        JvmConfiguration.applySystemProperties(jvmArguments);

        URL[] urls = classpath.stream().map(MinecraftLauncher::toUrl).toArray(URL[]::new);
        try (URLClassLoader classLoader =
                new URLClassLoader(urls, MinecraftLauncher.class.getClassLoader())) {
            Thread.currentThread().setContextClassLoader(classLoader);

            Main.log("Starting Minecraft!");
            invokeMain(classLoader, metadata.mainClass(), gameArguments);
        }
    }

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

    private static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (java.net.MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid classpath entry: " + path, exception);
        }
    }

    private static void invokeMain(
            URLClassLoader classLoader, String mainClass, List<String> arguments) throws Exception {
        Class<?> entrypoint = Class.forName(mainClass, true, classLoader);
        Method main = entrypoint.getMethod("main", String[].class);

        try {
            main.invoke(null, (Object) arguments.toArray(String[]::new));
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();

            if (cause instanceof Exception checked) {
                throw checked;
            }

            if (cause instanceof Error error) {
                throw error;
            }

            throw exception;
        }
    }
}
