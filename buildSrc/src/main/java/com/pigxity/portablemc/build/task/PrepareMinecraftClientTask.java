package com.pigxity.portablemc.build.task;

import com.pigxity.portablemc.build.model.MinecraftPackage;
import com.pigxity.portablemc.build.resource.RuntimeResources;

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
        Path output = getOutputDirectory().get().getAsFile().toPath();
        String version = minecraftPackage.version();
        RuntimeResources.prepare(cache(), minecraftPackage, output);
        getLogger().lifecycle("Prepared Minecraft {} runtime resources in {}", version, output);
    }
}
