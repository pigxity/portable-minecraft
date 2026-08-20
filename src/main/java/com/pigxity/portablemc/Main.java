package com.pigxity.portablemc;

public class Main {
    public static void main(String[] args) throws Exception {
        new MinecraftLauncher().launch(java.nio.file.Path.of("."));
    }
}
