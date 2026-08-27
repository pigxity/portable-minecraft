package com.pigxity.portablemc.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.RuleParser;
import com.pigxity.portablemc.rule.RuleResolver;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

class LaunchArgumentsTest {
    @Test
    void expandsStringsArraysRulesAndPlaceholdersInOrder() {
        RuleEnvironment environment =
                new RuleEnvironment(new OperatingSystem("windows", "10.0", "amd64"), Set.of());
        LaunchArguments arguments =
                new LaunchArguments(
                        new RuleParser(), new RuleResolver(environment), Map.of("name", "Player"));

        List<String> result =
                arguments.resolve(
                        JsonParser.parseString(
                                        """
                                        ["--username", "${name}",
                                         {"rules":[{"action":"allow","os":{"name":"windows"}}],"value":["--width","1280"]},
                                         {"rules":[{"action":"allow","os":{"name":"linux"}}],"value":"--linux"}]
                                        """)
                                .getAsJsonArray());

        assertEquals(List.of("--username", "Player", "--width", "1280"), result);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        arguments.resolve(
                                JsonParser.parseString("[\"${missing}\"]").getAsJsonArray()));
    }

    @Test
    void alwaysIncludesObjectDefinitionsWithoutRules() {
        RuleEnvironment environment =
                new RuleEnvironment(new OperatingSystem("windows", "10.0", "amd64"), Set.of());
        LaunchArguments arguments =
                new LaunchArguments(new RuleParser(), new RuleResolver(environment), Map.of());

        List<String> result =
                arguments.resolve(
                        JsonParser.parseString(
                                        """
                                        [{"value":["-Xms2G","-Xmx4G"]}]
                                        """)
                                .getAsJsonArray());

        assertEquals(List.of("-Xms2G", "-Xmx4G"), result);
    }

    @Test
    void appliesDefaultPackagedAndUserJvmArgumentsInOrder() {
        RuleEnvironment environment =
                new RuleEnvironment(new OperatingSystem("windows", "10.0.19045", "amd64"), Set.of());
        LaunchArguments arguments =
                new LaunchArguments(new RuleParser(), new RuleResolver(environment), Map.of());
        var definitions =
                JsonParser.parseString(
                                """
                                {
                                  "default-user-jvm":[{"value":["-Xms2G","-Xmx4G"]}],
                                  "jvm":["-Drequired=true","-cp","client.jar"],
                                  "game":["--username","Player"]
                                }
                                """)
                        .getAsJsonObject();

        LaunchOptions result =
                arguments.resolveOptions(definitions)
                        .merge(new LaunchOptions(List.of("-Xmx8G"), List.of()));

        assertEquals(
                List.of(
                        "-Xms2G",
                        "-Xmx4G",
                        "-Drequired=true",
                        "-cp",
                        "client.jar",
                        "-Xmx8G"),
                result.jvmArguments());
        assertEquals(List.of("--username", "Player"), result.minecraftArguments());
    }

    @Test
    void mergesFlagsAndValuesWhileAllowingAdditionalArgumentsToOverrideDefaults() {
        assertEquals(
                List.of("--username", "Alex", "--fullscreen", "--width", "1280"),
                LaunchArguments.mergeArgs(
                        List.of("--username", "Player", "--fullscreen"),
                        List.of("--username", "Alex", "--width", "1280")));
    }
}
