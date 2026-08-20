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

## Simple Usage

Simply run the versioned jar with Java >= 25.
```bash
java -jar portable-minecraft-26.2-1.0-SNAPSHOT.jar
```

## CLI Arguments

Note that it is recommended to use feature flags and argument substitutions over directly passing arguments. 
This is what Minecraft's official launcher does internally.

- Feature flags (`--value`): these are used for package rule evaluation and conditionally enable specific Minecraft arguments.
- Argument substitutions (`-key=value`): These replace placeholders in launcher arguments. Some require certain feature flags to work.

Use the generated `packagerules.json` file for reference. It is a stripped version of Mojang's version package and contains all rule/feature/argument definitions.

### Other arguments

- Minecraft JVM arguments are prefixed with `-J`.
- Arguments after a literal `--` are passed directly to Minecraft (not recommended)

Full example:

```bash
java -jar portable-minecraft-26.2-1.0-SNAPSHOT.jar --is_demo_user -J-Xmx8G -auth_player_name=Player1 -- --accessToken xxxxx
```

### Authentication
To join online servers, you must pass in the following substitutions for authentication:
- `-auth_player_name=...`: your account's username
- `-auth_uuid=...`: your account's public UUID
- `-auth_access_token=...`: your **private** token generated through Microsoft's OAUTH/MinecraftServices flow.

You must generate the latter two through an external tool.