package com.pigxity.portablemc;

import java.util.Locale;

public record OperatingSystem(String name, String version, String architecture) {
    public static OperatingSystem current() {
        return new OperatingSystem(normalizeName(System.getProperty("os.name")),
                System.getProperty("os.version"), System.getProperty("os.arch"));
    }

    private static String normalizeName(String value) {
        String name = value.toLowerCase(Locale.ROOT);
        if (name.contains("win")) {
            return "windows";
        }
        if (name.contains("mac") || name.contains("darwin")) {
            return "osx";
        }
        if (name.contains("linux") || name.contains("unix")) {
            return "linux";
        }
        return name;
    }
}
