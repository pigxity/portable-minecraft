package com.pigxity.portablemc.build;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import java.io.IOException;

public abstract class MinecraftTask extends DefaultTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @Internal
    public abstract DirectoryProperty getCacheDirectory();

    final MinecraftCache cache() {
        return new MinecraftCache(getCacheDirectory().get().getAsFile().toPath());
    }

    final MinecraftPackage cachedPackage() throws IOException {
        return MinecraftPackage.cached(cache(), getMinecraftVersion().get());
    }
}
