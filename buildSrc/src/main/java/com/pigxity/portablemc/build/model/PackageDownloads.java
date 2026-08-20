package com.pigxity.portablemc.build.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    public static List<Download> libraries(JsonObject packageJson) {
        List<Download> result = new ArrayList<>();
        for (JsonElement element : packageJson.getAsJsonArray("libraries")) {
            JsonObject library = element.getAsJsonObject();
            String coordinate = library.get("name").getAsString();
            JsonObject downloads = library.getAsJsonObject("downloads");
            if (downloads == null) {
                continue;
            }
            if (downloads.has("artifact")) {
                result.add(fromJson(coordinate, downloads.getAsJsonObject("artifact")));
            }
            if (downloads.has("classifiers")) {
                for (Map.Entry<String, JsonElement> classifier :
                        downloads.getAsJsonObject("classifiers").entrySet()) {
                    result.add(
                            fromJson(
                                    withClassifier(coordinate, classifier.getKey()),
                                    classifier.getValue().getAsJsonObject()));
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

    static String withClassifier(String coordinate, String classifier) {
        String[] parts = coordinate.split(":");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Unsupported Maven coordinate: " + coordinate);
        }
        return parts[0] + ":" + parts[1] + ":" + parts[2] + ":" + classifier;
    }

    private static Download fromJson(String name, JsonObject json) {
        return new Download(
                name,
                json.has("path") ? json.get("path").getAsString() : null,
                json.get("url").getAsString(),
                json.get("sha1").getAsString());
    }

    public record Download(String name, String path, String url, String sha1) {}
}
