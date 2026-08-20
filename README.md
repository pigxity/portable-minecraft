# portable-minecraft

A Minecraft client bundler that builds a self-contained jar for the game, allowing you to run it entirely offline!

## Building from source

Note: I cannot publish releases of the project as it would violate Minecraft's EULA; therefore, you must build it from source.

Simply run the gradle `build` task. It downloads and keeps a cache of the client jar, assets, and libraries, adds them to resources, and compiles the project.
You may optionally modify the `minecraftVersion` property to change the target version.

```bash
./gradlew build "-PminecraftVersion=26.2"
```

For development, you could use the `runClient` task to run the project.
```bash
./gradlew runClient
```

## Usage

Simply run the versioned jar with Java >= 25.
```bash
java -jar portable-minecraft-26.2-1.0-SNAPSHOT.jar
```

Launcher feature flags use two dashes, JVM arguments use the `-J` prefix, and Minecraft
argument substitutions use `-name=value`. Arguments after a literal `--` are passed directly
to Minecraft without launcher validation.

```bash
java -jar portable-minecraft-26.2-1.0-SNAPSHOT.jar --is_demo_user -J-Xmx8G -auth_player_name=Player1 -- --uuid xxxxx
```

Note that Microsoft authentication is not yet supported, so you cannot join `online-mode=true` servers. 
You may, however, pass in `uuid` and `accessToken` manually but generating it requires extra setup.
