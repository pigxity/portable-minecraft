package com.pigxity.portablemc;

import com.pigxity.portablemc.cli.LauncherArgumentParser;
import com.pigxity.portablemc.cli.LauncherArguments;
import com.pigxity.portablemc.launch.MinecraftLauncher;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        final LauncherArguments launcherArgs = LauncherArgumentParser.parse(args);

        if (launcherArgs.showHelp()) {
            IO.println("""
                    portable-minecraft: a launcher in a jar!
                    
                    Flags:
                    --help: print this message
                    --version: print the version
                    
                    Arguments:
                    --feature x / -F x: boolean feature flag; conditionally enables specific Minecraft arguments.
                    --variable x=y / -V x=y: replaces placeholders in launcher arguments; some require certain feature flags.
                    --jvm x / -J x: a JVM argument passed into Minecraft's JVM.
                    
                    Any arguments after a literal `--` are passed directly into Minecraft.
                    """);
            System.exit(1);
        }
        else if (launcherArgs.showVersion()) {
            IO.println("portable-minecraft version: " + Main.class.getPackage().getImplementationVersion());
            System.exit(1);
        }

        new MinecraftLauncher().launch(Path.of("."), launcherArgs);
    }

    public static void log(String text) {
        IO.println("[portable-minecraft]: " + text);
    }
}
