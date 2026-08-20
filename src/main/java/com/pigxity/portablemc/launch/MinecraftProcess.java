package com.pigxity.portablemc.launch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftProcess {
    private MinecraftProcess() {}

    public static Path javaExecutable() {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable)
                .toAbsolutePath()
                .normalize();
    }

    public static List<String> command(
            Path javaExecutable,
            List<String> jvmArguments,
            String mainClass,
            List<String> minecraftArguments) {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable.toAbsolutePath().normalize().toString());
        command.addAll(jvmArguments);
        command.add(mainClass);
        command.addAll(minecraftArguments);
        return List.copyOf(command);
    }

    public static int run(
            Path workingDirectory,
            List<String> jvmArguments,
            String mainClass,
            List<String> minecraftArguments)
            throws IOException, InterruptedException {
        Process process =
                new ProcessBuilder(
                                command(
                                        javaExecutable(),
                                        jvmArguments,
                                        mainClass,
                                        minecraftArguments))
                        .directory(workingDirectory.toFile())
                        .inheritIO()
                        .start();
        return process.waitFor();
    }
}
