package com.pigxity.portablemc.launch.system;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftProcess {
    private final Path workingDirectory;
    private final List<String> command;

    private MinecraftProcess(Path workingDirectory, List<String> command) {
        this.workingDirectory = workingDirectory;
        this.command = List.copyOf(command);
    }

    public static MinecraftProcess create(
            Path javaExecutable,
            Path workingDirectory,
            List<String> jvmArguments,
            String mainClass,
            List<String> minecraftArguments) {
        List<String> command = new ArrayList<>();

        command.add(javaExecutable.toString());
        command.addAll(jvmArguments);
        command.add(mainClass);
        command.addAll(minecraftArguments);

        return new MinecraftProcess(workingDirectory, command);
    }

    public List<String> command() {
        return command;
    }

    public Process start() throws IOException {
        return new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .inheritIO()
                .start();
    }
}
