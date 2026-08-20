package com.pigxity.portablemc.build.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PackageRules {
    private PackageRules() {}

    public static JsonObject create(JsonObject packageJson) {
        JsonObject output = new JsonObject();
        output.addProperty("version", packageJson.get("id").getAsString());
        output.addProperty("versionType", packageJson.get("type").getAsString());
        output.addProperty(
                "assetIndex", packageJson.getAsJsonObject("assetIndex").get("id").getAsString());
        output.add("arguments", packageJson.getAsJsonObject("arguments").deepCopy());

        Map<String, JsonArray> rulesByCoordinate = new LinkedHashMap<>();
        for (JsonElement element : packageJson.getAsJsonArray("libraries")) {
            JsonObject library = element.getAsJsonObject();
            JsonArray rules =
                    library.has("rules") ? library.getAsJsonArray("rules") : new JsonArray();
            String coordinate = library.get("name").getAsString();
            JsonObject downloads = library.getAsJsonObject("downloads");
            if (downloads == null) {
                continue;
            }
            if (downloads.has("artifact")) {
                rulesByCoordinate.put(coordinate, rules.deepCopy());
            }
            if (downloads.has("classifiers")) {
                for (String classifier : downloads.getAsJsonObject("classifiers").keySet()) {
                    rulesByCoordinate.put(
                            PackageDownloads.withClassifier(coordinate, classifier),
                            rules.deepCopy());
                }
            }
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
