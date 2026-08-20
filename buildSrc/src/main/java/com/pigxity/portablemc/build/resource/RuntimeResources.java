package com.pigxity.portablemc.build.resource;

import com.pigxity.portablemc.build.cache.MinecraftCache;
import com.pigxity.portablemc.build.io.FileTrees;
import com.pigxity.portablemc.build.io.JsonFiles;
import com.pigxity.portablemc.build.model.ClientMetadata;
import com.pigxity.portablemc.build.model.MinecraftPackage;
import com.pigxity.portablemc.build.model.PackageDownloads;

import java.io.IOException;
import java.nio.file.Path;

public final class RuntimeResources {
    private RuntimeResources() {}

    public static void prepare(
            MinecraftCache cache, MinecraftPackage minecraftPackage, Path output)
            throws IOException {
        FileTrees.resetDirectory(output);
        Path overrides = output.resolve("overrides");
        var packageJson = minecraftPackage.json();

        PackageDownloads.Download client = PackageDownloads.client(packageJson);
        String version = minecraftPackage.version();
        FileTrees.copy(
                cache.client(client.sha1()),
                overrides.resolve(ClientMetadata.clientJarPath(version)));

        PackageDownloads.Download index = PackageDownloads.assetIndex(packageJson);
        Path cachedIndex = cache.assetIndex(index.sha1());
        FileTrees.copy(
                cachedIndex,
                overrides.resolve("assets/indexes").resolve(index.name() + ".json"));
        for (String hash : PackageDownloads.assetHashes(JsonFiles.readObject(cachedIndex))) {
            FileTrees.copy(
                    cache.assetObject(hash),
                    overrides
                            .resolve("assets/objects")
                            .resolve(hash.substring(0, 2))
                            .resolve(hash));
        }

        for (PackageDownloads.LibraryArtifact library :
                PackageDownloads.libraries(packageJson)) {
            FileTrees.copy(
                    cache.library(library.path()),
                    overrides.resolve("libraries").resolve(library.path()));
        }

        FileTrees.copy(cache.packageRules(version), output.resolve("packagerules.json"));
        ClientMetadata.write(
                output.resolve("clientmeta.properties"), ClientMetadata.create(packageJson));
    }
}
