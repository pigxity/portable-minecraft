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

        Map<String, JsonArray> rulesByPath = new LinkedHashMap<>();
        for (PackageDownloads.LibraryArtifact library : PackageDownloads.libraries(packageJson)) {
            rulesByPath.put(library.path(), library.rules().deepCopy());
        }

        JsonArray libraries = new JsonArray();
        for (Map.Entry<String, JsonArray> entry : rulesByPath.entrySet()) {
            JsonObject library = new JsonObject();
            library.addProperty("path", entry.getKey());
            library.add("rules", entry.getValue());
            libraries.add(library);
        }

        output.add("libraries", libraries);

        return output;
    }
}
