package com.pigxity.portablemc.library;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.RuleParser;
import com.pigxity.portablemc.rule.RuleResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LibraryClasspath {
    private final Path gameDirectory;
    private final RuleParser ruleParser;
    private final RuleResolver rules;
    private final OperatingSystem operatingSystem;

    public LibraryClasspath(
            Path gameDirectory,
            RuleParser ruleParser,
            RuleResolver rules,
            OperatingSystem operatingSystem) {
        this.gameDirectory = gameDirectory;
        this.ruleParser = ruleParser;
        this.rules = rules;
        this.operatingSystem = operatingSystem;
    }

    public List<Path> resolve(JsonArray definitions) throws IOException {
        List<Path> libraries = new ArrayList<>();
        for (JsonElement element : definitions) {
            JsonObject definition = element.getAsJsonObject();
            String coordinate = definition.get("name").getAsString();
            boolean ruleAllowed =
                    rules.isAllowed(ruleParser.parse(definition.getAsJsonArray("rules")));
            if (!ruleAllowed || !classifierMatchesArchitecture(coordinate)) {
                continue;
            }
            Path library =
                    gameDirectory
                            .resolve("libraries")
                            .resolve(MavenCoordinates.libraryPath(coordinate));
            if (!Files.isRegularFile(library)) {
                throw new IOException("Required library is missing: " + library);
            }
            libraries.add(library.toAbsolutePath().normalize());
        }
        return List.copyOf(libraries);
    }

    private boolean classifierMatchesArchitecture(String coordinate) {
        String[] parts = coordinate.split(":", -1);
        if (parts.length != 4) {
            return true;
        }
        String classifier = parts[3].toLowerCase(Locale.ROOT);
        String architecture = operatingSystem.architecture().toLowerCase(Locale.ROOT);
        boolean arm64 = architecture.equals("aarch64") || architecture.equals("arm64");
        boolean x86 = architecture.equals("x86") || architecture.matches("i[3-6]86");
        boolean x64 = architecture.equals("amd64") || architecture.equals("x86_64");

        if (classifier.contains("arm64") || classifier.contains("aarch_64")) {
            return arm64;
        }
        if (classifier.contains("x86_64")) {
            return x64;
        }
        if (classifier.endsWith("-x86")) {
            return x86;
        }
        if (classifier.startsWith("natives-")) {
            return x64;
        }
        return true;
    }
}
