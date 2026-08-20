package com.pigxity.portablemc.build.task;

import com.pigxity.portablemc.build.download.DownloadBatch;
import com.pigxity.portablemc.build.download.VerifiedDownloader;
import com.pigxity.portablemc.build.model.PackageDownloads;

import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.util.List;

@DisableCachingByDefault(
        because = "The task maintains its own verified cache under Gradle user home")
public abstract class DownloadLibrariesTask extends MinecraftTask {
    @TaskAction
    public void downloadLibraries() throws Exception {
        List<PackageDownloads.LibraryArtifact> libraries =
                PackageDownloads.libraries(cachedPackage().json());
        VerifiedDownloader downloader = new VerifiedDownloader();
        getLogger()
                .lifecycle(
                        "Downloading or verifying {} Minecraft library artifacts",
                        libraries.size());
        DownloadBatch.run(
                libraries,
                library ->
                        downloader.downloadVerified(
                                library.url(), cache().library(library.path()), library.sha1()));
    }
}
