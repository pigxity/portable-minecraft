package com.pigxity.portablemc.build.task;

import com.google.gson.JsonObject;
import com.pigxity.portablemc.build.io.FileTrees;
import com.pigxity.portablemc.build.model.MinecraftPackage;
import com.pigxity.portablemc.build.model.PackageDownloads;
import com.pigxity.portablemc.shared.ClientMetadata;
import com.pigxity.portablemc.shared.PropertiesFile;
import com.pigxity.portablemc.shared.JsonFiles;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.nio.file.Path;

@DisableCachingByDefault(because = "Inputs reside in the plugin's verified Gradle user-home cache")
public abstract class PrepareMinecraftClientTask extends MinecraftTask {
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void prepareMinecraftClient() throws Exception {
        MinecraftPackage minecraftPackage = cachedPackage();
        JsonObject packageJson = minecraftPackage.json();
        Path output = getOutputDirectory().get().getAsFile().toPath();

        FileTrees.resetDirectory(output);
        Path overrides = output.resolve("overrides");

        PackageDownloads.Download client = PackageDownloads.client(packageJson);
        ClientMetadata metadata =
                ClientMetadata.create(
                        packageJson.get("mainClass").getAsString(),
                        packageJson.get("id").getAsString(),
                        packageJson.get("type").getAsString(),
                        packageJson.getAsJsonObject("assetIndex").get("id").getAsString());
        String version = metadata.version();
        FileTrees.copy(
                cache().client(client.sha1()),
                overrides.resolve(metadata.clientJarPath()).normalize());

        PackageDownloads.Download index = PackageDownloads.assetIndex(packageJson);
        Path cachedIndex = cache().assetIndex(index.sha1());
        FileTrees.copy(
                cachedIndex,
                overrides.resolve("assets/indexes").resolve(index.name() + ".json"));
        for (String hash : PackageDownloads.assetHashes(JsonFiles.readObject(cachedIndex))) {
            FileTrees.copy(
                    cache().assetObject(hash),
                    overrides
                            .resolve("assets/objects")
                            .resolve(hash.substring(0, 2))
                            .resolve(hash));
        }

        for (PackageDownloads.LibraryArtifact library : PackageDownloads.libraries(packageJson)) {
            FileTrees.copy(
                    cache().library(library.path()),
                    overrides.resolve("libraries").resolve(library.path()));
        }

        FileTrees.copy(cache().packageRules(version), output.resolve("packagerules.json"));
        PropertiesFile.write(output.resolve(ClientMetadata.FILE_NAME), metadata.toMap());

        getLogger().lifecycle("Prepared Minecraft {} runtime resources in {}", version, output);
    }
}
