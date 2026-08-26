package com.pigxity.portablemc.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

class LaunchOptionsTest {
    @Test
    void appendsJvmArgumentsInMergeOrder() {
        LaunchOptions packaged =
                new LaunchOptions(
                        List.of("-Djava.library.path=natives", "-Xmx1G"), List.of());
        LaunchOptions additional =
                new LaunchOptions(List.of("-Xmx4G", "-Dcustom=true"), List.of());

        LaunchOptions merged = packaged.merge(additional);

        assertEquals(
                List.of(
                        "-Djava.library.path=natives",
                        "-Xmx1G",
                        "-Xmx4G",
                        "-Dcustom=true"),
                merged.jvmArguments());
    }

    @Test
    void mergesJvmAndMinecraftArgumentsTogether() {
        LaunchOptions packaged =
                new LaunchOptions(
                        List.of("-cp", "libraries;client.jar"),
                        List.of("--username", "Player", "--fullscreen"));
        LaunchOptions additional =
                new LaunchOptions(
                        List.of("-Xmx4G"),
                        List.of("--username", "Alex", "--width", "1280"));

        LaunchOptions merged = packaged.merge(additional);

        assertEquals(
                List.of("-cp", "libraries;client.jar", "-Xmx4G"),
                merged.jvmArguments());
        assertEquals(
                List.of("--username", "Alex", "--fullscreen", "--width", "1280"),
                merged.minecraftArguments());
    }
}
