package com.pigxity.portablemc.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

class CommandLineArgumentsTest {
    @Test
    void parsesFeaturesJvmArgumentsSubstitutionsAndLiteralMinecraftArguments() {
        CommandLineArguments arguments =
                CommandLineArguments.parse(
                        new String[] {
                            "--is_demo_user",
                            "-J-Xmx8G",
                            "-auth_player_name=Player1",
                            "--",
                            "--uuid",
                            "xxxxx"
                        });

        assertEquals(Map.of("is_demo_user", true), arguments.features());
        assertEquals(Map.of("auth_player_name", "Player1"), arguments.substitutions());
        assertEquals(List.of("-Xmx8G"), arguments.jvmArguments());
        assertEquals(List.of("--uuid", "xxxxx"), arguments.minecraftArguments());
    }

    @Test
    void doesNotValidateAnythingAfterTheSeparator() {
        CommandLineArguments arguments =
                CommandLineArguments.parse(new String[] {"--", "plain", "-J", "--bad=value"});

        assertEquals(List.of("plain", "-J", "--bad=value"), arguments.minecraftArguments());
    }

    @ParameterizedTest
    @ValueSource(strings = {"plain", "---", "--feature=value", "-name", "-=value", "-J"})
    void rejectsInvalidLauncherArgumentFormats(String argument) {
        assertThrows(
                IllegalArgumentException.class,
                () -> CommandLineArguments.parse(new String[] {argument}));
    }
}
