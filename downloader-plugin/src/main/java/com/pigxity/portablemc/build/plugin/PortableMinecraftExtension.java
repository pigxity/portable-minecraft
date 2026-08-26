package com.pigxity.portablemc.build.plugin;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public abstract class PortableMinecraftExtension {
    public abstract Property<String> getVersion();

    public abstract Property<Long> getManifestTtlHours();

    public abstract DirectoryProperty getGeneratedResourcesDirectory();
}
