package com.pigxity.portablemc.launch;

import java.util.Arrays;
import java.util.List;

public record LaunchOptions(List<String> jvmArguments, List<String> minecraftArguments) {
    public LaunchOptions {
        jvmArguments = List.copyOf(jvmArguments);
        minecraftArguments = List.copyOf(minecraftArguments);
    }

    public static LaunchOptions withMinecraftArguments(String[] minecraftArguments) {
        return new LaunchOptions(List.of(), Arrays.asList(minecraftArguments));
    }

    public LaunchOptions merge(LaunchOptions other) {
        return new LaunchOptions(
                LaunchArguments.append(jvmArguments, other.jvmArguments),
                LaunchArguments.mergeArgs(minecraftArguments, other.minecraftArguments));
    }
}
