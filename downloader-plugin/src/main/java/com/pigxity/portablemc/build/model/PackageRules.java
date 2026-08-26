package com.pigxity.portablemc.build.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PackageRules {
    private PackageRules() {}

    public static JsonObject create(JsonObject packageJson) {
        JsonObject output = new JsonObject();
        output.add("arguments", packageJson.getAsJsonObject("arguments").deepCopy());

        Map<String, JsonArray> rulesByCoordinate = new LinkedHashMap<>();
        for (PackageDownloads.LibraryArtifact library : PackageDownloads.libraries(packageJson)) {
            rulesByCoordinate.put(library.name(), library.rules().deepCopy());
        }

        JsonArray libraries = new JsonArray();
        for (Map.Entry<String, JsonArray> entry : rulesByCoordinate.entrySet()) {
            JsonObject library = new JsonObject();
            library.addProperty("name", entry.getKey());
            library.add("rules", entry.getValue());
            libraries.add(library);
        }

        output.add("libraries", libraries);

        return output;
    }
}
