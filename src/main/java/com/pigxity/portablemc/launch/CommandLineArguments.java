package com.pigxity.portablemc.launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public record CommandLineArguments(
        Map<String, Boolean> features,
        Map<String, String> substitutions,
        List<String> jvmArguments,
        List<String> minecraftArguments) {
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_][A-Za-z0-9_.-]*");

    public CommandLineArguments {
        features = immutableMap(features);
        substitutions = immutableMap(substitutions);
        jvmArguments = List.copyOf(jvmArguments);
        minecraftArguments = List.copyOf(minecraftArguments);
    }

    public static CommandLineArguments parse(String[] rawArguments) {
        Map<String, Boolean> features = new LinkedHashMap<>();
        Map<String, String> substitutions = new LinkedHashMap<>();
        List<String> jvmArguments = new ArrayList<>();
        List<String> minecraftArguments = new ArrayList<>();
        boolean minecraftOnly = false;

        for (String argument : rawArguments) {
            if (minecraftOnly) {
                minecraftArguments.add(argument);
                continue;
            }
            if (argument.equals("--")) {
                minecraftOnly = true;
                continue;
            }
            if (argument.startsWith("-J")) {
                String jvmArgument = argument.substring(2);
                if (jvmArgument.length() < 2 || jvmArgument.charAt(0) != '-') {
                    throw invalid(argument);
                }
                jvmArguments.add(jvmArgument);
                continue;
            }
            if (argument.startsWith("--")) {
                String feature = argument.substring(2);
                if (!NAME.matcher(feature).matches()) {
                    throw invalid(argument);
                }
                features.put(feature, true);
                continue;
            }
            if (argument.startsWith("-")) {
                int separator = argument.indexOf('=', 1);
                String name = separator < 0 ? "" : argument.substring(1, separator);
                if (!NAME.matcher(name).matches()) {
                    throw invalid(argument);
                }
                substitutions.put(name, argument.substring(separator + 1));
                continue;
            }
            throw invalid(argument);
        }

        return new CommandLineArguments(
                features, substitutions, jvmArguments, minecraftArguments);
    }

    private static IllegalArgumentException invalid(String argument) {
        return new IllegalArgumentException(
                "Invalid launcher argument '"
                        + argument
                        + "'. Expected --feature, -name=value, -J-jvmArg, or -- followed by Minecraft arguments.");
    }

    private static <K, V> Map<K, V> immutableMap(Map<K, V> values) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
