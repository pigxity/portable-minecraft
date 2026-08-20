package com.pigxity.portablemc.build.task;

import com.google.gson.JsonObject;
import com.pigxity.portablemc.build.io.FileTrees;
import com.pigxity.portablemc.build.io.JsonFiles;
import com.pigxity.portablemc.build.model.MinecraftPackage;
import com.pigxity.portablemc.build.model.PackageDownloads;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

        PackageDownloads.Download client = PackageDownloads.client(packageJson);
        String version = minecraftPackage.version();
        FileTrees.copy(
                cache().client(client.sha1()),
                output.resolve("versions").resolve(version).resolve("client-" + version + ".jar"));

        PackageDownloads.Download index = PackageDownloads.assetIndex(packageJson);
        Path cachedIndex = cache().assetIndex(index.sha1());
        FileTrees.copy(
                cachedIndex, output.resolve("assets/indexes").resolve(index.name() + ".json"));
        for (String hash : PackageDownloads.assetHashes(JsonFiles.readObject(cachedIndex))) {
            FileTrees.copy(
                    cache().assetObject(hash),
                    output.resolve("assets/objects").resolve(hash.substring(0, 2)).resolve(hash));
        }

        for (PackageDownloads.LibraryArtifact library :
                PackageDownloads.libraries(packageJson)) {
            FileTrees.copy(
                    cache().library(library.path()),
                    output.resolve("libraries").resolve(library.path()));
        }

        FileTrees.copy(
                cache().packageRules(version), output.resolve("packagerules.json"));
        Files.writeString(
                output.resolve("main-class"),
                packageJson.get("mainClass").getAsString(),
                StandardCharsets.UTF_8);
        getLogger().lifecycle("Prepared Minecraft {} runtime resources in {}", version, output);
    }
}
