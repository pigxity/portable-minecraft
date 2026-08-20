package com.pigxity.portablemc.build;

import java.nio.file.Path;

record MinecraftCache(Path root) {
    Path manifest() {
        return root.resolve("metadata/version_manifest_v2.json");
    }

    Path versionPackage(String sha1) {
        return root.resolve("packages").resolve(sha1 + ".json");
    }

    Path packageRules(String version) {
        return root.resolve("rules").resolve("packagerules-" + version + ".json");
    }

    Path client(String sha1) {
        return root.resolve("clients").resolve(sha1 + ".jar");
    }

    Path assetIndex(String sha1) {
        return root.resolve("asset-indexes").resolve(sha1 + ".json");
    }

    Path assetObject(String sha1) {
        return root.resolve("assets/objects").resolve(sha1.substring(0, 2)).resolve(sha1);
    }

    Path library(String path) {
        return root.resolve("libraries").resolve(path);
    }
}
