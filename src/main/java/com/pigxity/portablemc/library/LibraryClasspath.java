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
            if (!ruleAllowed
                    || !LibraryClassifier.fromCoordinate(coordinate)
                            .supports(operatingSystem.architecture())) {
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
}
