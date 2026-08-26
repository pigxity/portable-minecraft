# portable-minecraft

A Minecraft client bundler that builds a self-contained jar for the game, allowing you to run it entirely offline!

## Building from source

Note: I cannot publish releases of the project as it would violate Minecraft's EULA; therefore, you must build it from source.

Simply run the launcher module's Gradle `build` task. It downloads and keeps a cache of the client jar, assets, and libraries, adds them to resources, and compiles the project.
You may optionally modify the `minecraftVersion` property to change the target version.

```bash
./gradlew :launcher:build "-PminecraftVersion=26.2"
```

For development, you could use the `runClient` task to run the project.
```bash
./gradlew :launcher:runClient
```

## Usage

Simply run it with Java >= 25.
```bash
java -jar portable-minecraft.jar
```

Arguments are passed directly into Minecraft, so you could modify the player name for example.
```bash
java -jar portable-minecraft.jar --username Pigxity
```

Note that Microsoft authentication is not yet supported, so you cannot join `online-mode=true` servers. 
You may, however, pass in `uuid` and `accessToken` manually but generating it requires extra setup.
