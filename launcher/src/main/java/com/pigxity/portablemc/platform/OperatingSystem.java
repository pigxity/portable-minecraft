package com.pigxity.portablemc.platform;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public record OperatingSystem(String name, String version, String architecture) {
    private static final Map<Predicate<String>, String> OS_RULES =
            Map.of(
                    name -> name.contains("win"),
                    "windows",
                    name -> name.contains("mac") || name.contains("darwin"),
                    "osx",
                    name -> name.contains("linux") || name.contains("unix"),
                    "linux");

    public static OperatingSystem current() {
        return new OperatingSystem(
                normalizeName(System.getProperty("os.name")),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
    }

    private static String normalizeName(String value) {
        final String lowerName = value.toLowerCase(Locale.ROOT);

        return OS_RULES.entrySet().stream()
                .filter(entry -> entry.getKey().test(lowerName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(lowerName);
    }
}
