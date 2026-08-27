package com.pigxity.portablemc.build.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PackageDownloads {
    private PackageDownloads() {}

    public static Download client(JsonObject packageJson) {
        return fromJson(
                "client", packageJson.getAsJsonObject("downloads").getAsJsonObject("client"));
    }

    public static Download assetIndex(JsonObject packageJson) {
        return fromJson(
                packageJson.getAsJsonObject("assetIndex").get("id").getAsString(),
                packageJson.getAsJsonObject("assetIndex"));
    }

    public static List<LibraryArtifact> libraries(JsonObject packageJson) {
        List<LibraryArtifact> result = new ArrayList<>();

        for (JsonElement element : packageJson.getAsJsonArray("libraries")) {
            JsonObject library = element.getAsJsonObject();
            JsonArray rules =
                    library.has("rules") ? library.getAsJsonArray("rules") : new JsonArray();
            JsonObject downloads = library.getAsJsonObject("downloads");

            if (downloads == null) {
                continue;
            }

            if (downloads.has("artifact")) {
                result.add(libraryArtifact(rules, downloads.getAsJsonObject("artifact")));
            }

            if (downloads.has("classifiers")) {
                for (JsonElement classifier :
                        downloads.getAsJsonObject("classifiers").asMap().values()) {
                    result.add(libraryArtifact(rules, classifier.getAsJsonObject()));
                }
            }
        }

        return List.copyOf(result);
    }

    public static Set<String> assetHashes(JsonObject assetIndex) {
        Set<String> hashes = new LinkedHashSet<>();

        for (JsonElement element : assetIndex.getAsJsonObject("objects").asMap().values()) {
            hashes.add(element.getAsJsonObject().get("hash").getAsString());
        }

        return hashes;
    }

    private static Download fromJson(String name, JsonObject json) {
        return new Download(
                name,
                json.has("path") ? json.get("path").getAsString() : null,
                json.get("url").getAsString(),
                json.get("sha1").getAsString());
    }

    private static LibraryArtifact libraryArtifact(JsonArray rules, JsonObject json) {
        return new LibraryArtifact(
                json.get("path").getAsString(),
                json.get("url").getAsString(),
                json.get("sha1").getAsString(),
                rules.deepCopy());
    }

    public record Download(String name, String path, String url, String sha1) {}

    public record LibraryArtifact(String path, String url, String sha1, JsonArray rules) {}
}
