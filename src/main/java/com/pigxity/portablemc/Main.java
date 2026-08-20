package com.pigxity.portablemc;

import com.pigxity.portablemc.launch.MinecraftLauncher;

public class Main {
    public static void main(String[] args) throws Exception {
        new MinecraftLauncher().launch(java.nio.file.Path.of("."));
    }
}
