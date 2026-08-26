package com.pigxity.portablemc;

import com.pigxity.portablemc.launch.MinecraftLauncher;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        new MinecraftLauncher().launch(Path.of("."), args);
    }

    public static void log(String text) {
        IO.println("[portable-minecraft]: " + text);
    }
}
