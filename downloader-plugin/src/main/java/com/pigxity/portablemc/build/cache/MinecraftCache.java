package com.pigxity.portablemc.build.cache;

import java.nio.file.Path;

public record MinecraftCache(Path root) {
    public Path manifest() {
        return root.resolve("version_manifest_v2.json");
    }

    public Path versionPackage(String sha1) {
        return root.resolve("packages").resolve(sha1 + ".json");
    }

    public Path packageRules(String version) {
        return root.resolve("rules").resolve("packagerules-" + version + ".json");
    }

    public Path client(String sha1) {
        return root.resolve("clients").resolve(sha1 + ".jar");
    }

    public Path assetIndex(String sha1) {
        return root.resolve("asset-indexes").resolve(sha1 + ".json");
    }

    public Path assetObject(String sha1) {
        return root.resolve("assets/objects").resolve(sha1.substring(0, 2)).resolve(sha1);
    }

    public Path library(String path) {
        return root.resolve("libraries").resolve(path);
    }
}
