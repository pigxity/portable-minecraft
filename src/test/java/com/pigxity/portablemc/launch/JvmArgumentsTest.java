package com.pigxity.portablemc.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

class JvmArgumentsTest {
    @Test
    void userMemoryAndSystemPropertyArgumentsOverridePackagedValues() {
        List<String> merged =
                JvmArguments.merge(
                        List.of("-Xmx2G", "-Xss1M", "-Dexample=packaged", "-cp", "libraries"),
                        List.of("-Xmx8G"));
        merged = JvmArguments.merge(merged, List.of("-Dexample=user"));

        assertEquals(
                List.of("-Xmx8G", "-Xss1M", "-Dexample=user", "-cp", "libraries"),
                merged);
    }

    @Test
    void keepsRepeatedUserJvmOptionsThatTheJvmCombines() {
        List<String> merged =
                JvmArguments.merge(
                        List.of("-cp", "libraries"),
                        List.of("--add-opens=one", "--add-opens=two"));

        assertEquals(
                List.of("-cp", "libraries", "--add-opens=one", "--add-opens=two"),
                merged);
    }
}
