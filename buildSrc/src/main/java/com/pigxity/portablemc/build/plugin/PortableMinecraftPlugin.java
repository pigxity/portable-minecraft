package com.pigxity.portablemc.build.plugin;

import com.pigxity.portablemc.build.task.DownloadAssetsTask;
import com.pigxity.portablemc.build.task.DownloadClientTask;
import com.pigxity.portablemc.build.task.DownloadLibrariesTask;
import com.pigxity.portablemc.build.task.DownloadPackageTask;
import com.pigxity.portablemc.build.task.MinecraftTask;
import com.pigxity.portablemc.build.task.PrepareMinecraftClientTask;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public final class PortableMinecraftPlugin implements Plugin<Project> {
    private static final String TASK_GROUP = "portable minecraft";

    @Override
    public void apply(Project project) {
        PortableMinecraftExtension extension =
                project.getExtensions()
                        .create("portableMinecraft", PortableMinecraftExtension.class);
        extension.getManifestTtlHours().convention(24L);
        extension
                .getGeneratedResourcesDirectory()
                .convention(
                        project.getLayout().getProjectDirectory().dir("src/generated/resources"));

        File cacheRoot =
                new File(project.getGradle().getGradleUserHomeDir(), "caches/portable-minecraft");
        TaskProvider<DownloadPackageTask> downloadPackage =
                register(
                        project,
                        "downloadPackage",
                        DownloadPackageTask.class,
                        "Downloads the Minecraft manifest and version package",
                        extension,
                        cacheRoot);
        downloadPackage.configure(
                task -> task.getManifestTtlHours().convention(extension.getManifestTtlHours()));

        TaskProvider<DownloadClientTask> downloadClient =
                register(
                        project,
                        "downloadClient",
                        DownloadClientTask.class,
                        "Downloads and verifies the Minecraft client",
                        extension,
                        cacheRoot);
        downloadClient.configure(task -> task.dependsOn(downloadPackage));

        TaskProvider<DownloadAssetsTask> downloadAssets =
                register(
                        project,
                        "downloadAssets",
                        DownloadAssetsTask.class,
                        "Downloads and verifies all Minecraft assets",
                        extension,
                        cacheRoot);
        downloadAssets.configure(task -> task.dependsOn(downloadPackage));

        TaskProvider<DownloadLibrariesTask> downloadLibraries =
                register(
                        project,
                        "downloadLibraries",
                        DownloadLibrariesTask.class,
                        "Downloads and verifies all Minecraft libraries and native classifiers",
                        extension,
                        cacheRoot);
        downloadLibraries.configure(task -> task.dependsOn(downloadPackage));

        TaskProvider<PrepareMinecraftClientTask> prepare =
                register(
                        project,
                        "prepareMinecraftClient",
                        PrepareMinecraftClientTask.class,
                        "Prepares the embedded Minecraft runtime resources",
                        extension,
                        cacheRoot);
        prepare.configure(
                task -> {
                    task.dependsOn(downloadClient, downloadAssets, downloadLibraries);
                    task.getOutputDirectory()
                            .convention(extension.getGeneratedResourcesDirectory());
                });

        project.getPlugins()
                .withType(
                        JavaPlugin.class,
                        ignored -> {
                            SourceSetContainer sourceSets =
                                    project.getExtensions().getByType(SourceSetContainer.class);
                            sourceSets
                                    .getByName("main")
                                    .getResources()
                                    .srcDir(extension.getGeneratedResourcesDirectory());
                            project.getTasks()
                                    .named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME)
                                    .configure(task -> task.dependsOn(prepare));
                        });
    }

    private static <T extends MinecraftTask> TaskProvider<T> register(
            Project project,
            String name,
            Class<T> type,
            String description,
            PortableMinecraftExtension extension,
            File cacheRoot) {
        return project.getTasks()
                .register(
                        name,
                        type,
                        task -> {
                            task.setGroup(TASK_GROUP);
                            task.setDescription(description);
                            task.getMinecraftVersion().convention(extension.getVersion());
                            task.getCacheDirectory()
                                    .convention(
                                            project.getLayout()
                                                    .dir(project.provider(() -> cacheRoot)));
                        });
    }
}
