package com.pigxity.portablemc;

import com.pigxity.portablemc.launch.CommandLineArguments;
import com.pigxity.portablemc.launch.MinecraftLauncher;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        CommandLineArguments arguments;
        try {
            arguments = CommandLineArguments.parse(args);
        } catch (IllegalArgumentException exception) {
            System.err.println("[portable-minecraft]: " + exception.getMessage());
            System.exit(2);
            return;
        }
        new MinecraftLauncher().launch(Path.of("."), arguments);
    }

    public static void log(String text) {
        IO.println("[portable-minecraft]: " + text);
    }
}
