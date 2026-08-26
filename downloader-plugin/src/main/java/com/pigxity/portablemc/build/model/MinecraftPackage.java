package com.pigxity.portablemc.build.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pigxity.portablemc.build.cache.MinecraftCache;
import com.pigxity.portablemc.build.download.VerifiedDownloader;
import com.pigxity.portablemc.build.io.JsonFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record MinecraftPackage(
        String version, String sha1, String url, Path path, JsonObject json) {
    private static final String MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public static MinecraftPackage download(
            MinecraftCache cache, String version, long manifestTtlHours) throws IOException {
        VerifiedDownloader downloader = new VerifiedDownloader();
        downloader.downloadWithTtl(
                MANIFEST_URL, cache.manifest(), java.time.Duration.ofHours(manifestTtlHours));

        JsonObject manifest = JsonFiles.readObject(cache.manifest());
        JsonObject metadata = findVersion(manifest.getAsJsonArray("versions"), version);
        String sha1 = metadata.get("sha1").getAsString();
        String url = metadata.get("url").getAsString();
        Path packagePath = cache.versionPackage(sha1);

        downloader.downloadVerified(url, packagePath, sha1);

        return new MinecraftPackage(
                version, sha1, url, packagePath, JsonFiles.readObject(packagePath));
    }

    public static MinecraftPackage cached(MinecraftCache cache, String version) throws IOException {
        if (!Files.isRegularFile(cache.manifest())) {
            throw new IOException(
                    "Minecraft manifest has not been downloaded; run downloadPackage first");
        }

        JsonObject metadata =
                findVersion(
                        JsonFiles.readObject(cache.manifest()).getAsJsonArray("versions"), version);
        String sha1 = metadata.get("sha1").getAsString();
        Path packagePath = cache.versionPackage(sha1);

        if (!Files.isRegularFile(packagePath)) {
            throw new IOException(
                    "Minecraft package "
                            + version
                            + " has not been downloaded; run downloadPackage first");
        }

        return new MinecraftPackage(
                version,
                sha1,
                metadata.get("url").getAsString(),
                packagePath,
                JsonFiles.readObject(packagePath));
    }

    private static JsonObject findVersion(JsonArray versions, String requestedVersion)
            throws IOException {
        for (JsonElement element : versions) {
            JsonObject version = element.getAsJsonObject();
            if (requestedVersion.equals(version.get("id").getAsString())) {
                return version;
            }
        }

        throw new IOException(
                "Minecraft version "
                        + requestedVersion
                        + " is not present in version_manifest_v2.json");
    }
}
