package com.pigxity.portablemc;

import com.pigxity.portablemc.cli.auth.AuthHandler;
import com.pigxity.portablemc.cli.LauncherArgumentParser;
import com.pigxity.portablemc.cli.LauncherArguments;
import com.pigxity.portablemc.launch.MinecraftLauncher;

import java.nio.file.Path;

public class Main {
    public static final String USAGE =
            "Usage: portable-minecraft [OPTIONS...] [--] [MINECRAFT ARGS...]";

    public static final String HELP_MESSAGE = USAGE + "\n" + """
            Arguments after '--' are passed into Minecraft.

            A portable, bundled instance of Minecraft.
            
            Launcher:
            --help: print this message
            --version: print the version
            --verbose: enable verbose logging
            --auth: authenticate before starting Minecraft
            
            Rule resolving:
            --feature x / -F x: boolean feature flag; conditionally enables specific Minecraft arguments.
            --variable x=y / -V x=y: replaces placeholders in launcher arguments; some require certain feature flags.
            
            Misc:
            --jvm x / -J x: a JVM argument passed into Minecraft's JVM""";

    public static void main(String[] args) throws Exception {
        LauncherArguments launcherArgs;
        try {
            launcherArgs = LauncherArgumentParser.parse(args);
        }
        catch (IllegalArgumentException e) {
            IO.println(e.getMessage() + "\n");
            IO.println(USAGE);
            IO.println("Run 'portable-minecraft --help' for more info");
            System.exit(0);
            return;
        }

        if (launcherArgs.showHelp()) {
            IO.println(HELP_MESSAGE);
            System.exit(1);
        }
        if (launcherArgs.showVersion()) {
            IO.println("portable-minecraft version: " + Main.class.getPackage().getImplementationVersion());
            System.exit(1);
        }
        if (launcherArgs.auth()) {
            final var authDetails = new AuthHandler().get(Path.of("token-cache.json"));
            log("Authenticated with user: " + authDetails.username());

            final var vars = launcherArgs.variables();
            vars.put("auth_access_token", authDetails.accessToken());
            vars.put("auth_player_name", authDetails.username());
            vars.put("auth_uuid", authDetails.uuid());
        }

        if (launcherArgs.verbose()) {
            log("Feature args: " + launcherArgs.features());
            log("Variable args: " + launcherArgs.variables());
            log("JVM args: " + launcherArgs.jvmArguments());
        }

        new MinecraftLauncher().launch(Path.of("."), launcherArgs);
    }

    public static void log(String text) {
        IO.println("[portable-minecraft]: " + text);
    }
}
