package com.pigxity.portablemc.build.task;

import com.pigxity.portablemc.build.io.JsonFiles;
import com.pigxity.portablemc.build.model.MinecraftPackage;
import com.pigxity.portablemc.build.model.PackageRules;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(
        because = "The task maintains its own SHA-1-addressed cache under Gradle user home")
public abstract class DownloadPackageTask extends MinecraftTask {
    @Input
    public abstract Property<Long> getManifestTtlHours();

    @TaskAction
    public void downloadPackage() throws Exception {
        MinecraftPackage minecraftPackage =
                MinecraftPackage.download(
                        cache(), getMinecraftVersion().get(), getManifestTtlHours().get());
        JsonFiles.write(
                cache().packageRules(minecraftPackage.version()),
                PackageRules.create(minecraftPackage.json()));
        getLogger()
                .lifecycle(
                        "Cached Minecraft {} package as {}",
                        minecraftPackage.version(),
                        minecraftPackage.path());
    }
}
