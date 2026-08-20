package com.pigxity.portablemc.build;

import com.google.gson.JsonObject;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.nio.file.Path;
import java.util.Set;

@DisableCachingByDefault(because = "The task maintains its own SHA-1-addressed cache under Gradle user home")
public abstract class DownloadAssetsTask extends MinecraftTask {
    private static final String ASSET_BASE_URL = "https://resources.download.minecraft.net/";

    @TaskAction
    public void downloadAssets() throws Exception {
        PackageDownloads.Download index = PackageDownloads.assetIndex(cachedPackage().json());
        VerifiedDownloader downloader = new VerifiedDownloader();
        Path indexPath = downloader.downloadVerified(index.url(), cache().assetIndex(index.sha1()), index.sha1());
        JsonObject indexJson = JsonFiles.readObject(indexPath);
        Set<String> hashes = PackageDownloads.assetHashes(indexJson);
        getLogger().lifecycle("Downloading or verifying {} Minecraft assets", hashes.size());
        DownloadBatch.run(hashes, hash -> downloader.downloadVerified(
                ASSET_BASE_URL + hash.substring(0, 2) + "/" + hash,
                cache().assetObject(hash), hash));
    }
}
