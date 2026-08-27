package com.pigxity.portablemc.launch.system;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    public LibraryClasspath(Path gameDirectory, RuleParser ruleParser, RuleResolver rules) {
        this.gameDirectory = gameDirectory;
        this.ruleParser = ruleParser;
        this.rules = rules;
    }

    public List<Path> resolve(JsonArray definitions) throws IOException {
        List<Path> libraries = new ArrayList<>();

        for (JsonElement element : definitions) {
            JsonObject definition = element.getAsJsonObject();

            boolean ruleAllowed =
                    rules.isAllowed(ruleParser.parse(definition.getAsJsonArray("rules")));
            if (!ruleAllowed) {
                continue;
            }

            Path library =
                    gameDirectory
                            .resolve("libraries")
                            .resolve(definition.get("path").getAsString());

            if (!Files.isRegularFile(library)) {
                throw new IOException("Required library is missing: " + library);
            }

            libraries.add(library.toAbsolutePath().normalize());
        }

        return List.copyOf(libraries);
    }
}
