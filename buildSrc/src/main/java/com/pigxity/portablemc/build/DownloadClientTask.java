package com.pigxity.portablemc.build;

import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "The task maintains its own SHA-1-addressed cache under Gradle user home")
public abstract class DownloadClientTask extends MinecraftTask {
    @TaskAction
    public void downloadClient() throws Exception {
        PackageDownloads.Download client = PackageDownloads.client(cachedPackage().json());
        new VerifiedDownloader().downloadVerified(client.url(), cache().client(client.sha1()), client.sha1());
        getLogger().lifecycle("Cached Minecraft {} client", getMinecraftVersion().get());
    }
}
