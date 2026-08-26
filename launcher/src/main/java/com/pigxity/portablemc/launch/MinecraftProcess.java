package com.pigxity.portablemc.launch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class MinecraftProcess {
    private final Path workingDirectory;
    private final List<String> command;

    private MinecraftProcess(Path workingDirectory, List<String> command) {
        this.workingDirectory = workingDirectory;
        this.command = List.copyOf(command);
    }

    static MinecraftProcess create(
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

    List<String> command() {
        return command;
    }

    Process start() throws IOException {
        return new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .inheritIO()
                .start();
    }
}
