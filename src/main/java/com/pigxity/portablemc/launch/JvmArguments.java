package com.pigxity.portablemc.launch;

import java.util.ArrayList;
import java.util.List;

public final class JvmArguments {
    private JvmArguments() {}

    public static List<String> merge(List<String> packaged, List<String> user) {
        List<String> arguments = new ArrayList<>(packaged);
        for (String userArgument : user) {
            String key = overrideKey(userArgument);
            if (key == null) {
                arguments.add(userArgument);
                continue;
            }
            int replacement = firstWithKey(arguments, key);
            if (replacement < 0) {
                arguments.add(userArgument);
                continue;
            }
            arguments.set(replacement, userArgument);
            for (int index = arguments.size() - 1; index > replacement; index--) {
                if (key.equals(overrideKey(arguments.get(index)))) {
                    arguments.remove(index);
                }
            }
        }
        return List.copyOf(arguments);
    }

    private static int firstWithKey(List<String> arguments, String expectedKey) {
        for (int index = 0; index < arguments.size(); index++) {
            if (expectedKey.equals(overrideKey(arguments.get(index)))) {
                return index;
            }
        }
        return -1;
    }

    private static String overrideKey(String argument) {
        for (String option : List.of("-Xmx", "-Xms", "-Xss")) {
            if (argument.startsWith(option)) {
                return option;
            }
        }
        if (argument.startsWith("-D")) {
            return beforeEquals(argument);
        }
        if (argument.startsWith("-XX:+") || argument.startsWith("-XX:-")) {
            return "-XX:" + beforeEquals(argument.substring(5));
        }
        if (argument.startsWith("-XX:")) {
            return beforeEquals(argument);
        }
        return null;
    }

    private static String beforeEquals(String argument) {
        int separator = argument.indexOf('=');
        return separator < 0 ? argument : argument.substring(0, separator);
    }
}
