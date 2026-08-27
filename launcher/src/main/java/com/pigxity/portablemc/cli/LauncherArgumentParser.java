package com.pigxity.portablemc.cli;

import java.util.*;

public final class LauncherArgumentParser {

    public static LauncherArguments parse(String[] args) {
        boolean version = false;
        boolean help = false;
        boolean verbose = false;
        boolean auth = false;

        Map<String, String> variables = new LinkedHashMap<>();
        Set<String> features = new LinkedHashSet<>();
        List<String> jvmArguments = new ArrayList<>();
        List<String> minecraftArguments = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String argument = args[i];

            if (argument.equals("--")) {
                minecraftArguments.addAll(
                        Arrays.asList(args).subList(i + 1, args.length));
                break;
            }

            String option = argument;
            String inlineValue = null;

            if (argument.length() > 2
                    && argument.charAt(0) == '-'
                    && argument.charAt(1) != '-') {
                option = argument.substring(0, 2);
                inlineValue = argument.substring(2);
            }

            switch (option) {
                case "--version" -> version = true;
                case "--help" -> help = true;
                case "--verbose" -> verbose = true;
                case "--auth" -> auth = true;

                case "--variable", "-V" -> {
                    String value = inlineValue != null
                            ? inlineValue
                            : requireValue(args, ++i, argument);

                    addVariable(variables, value);
                }

                case "--feature", "-F" -> {
                    String value = inlineValue != null
                            ? inlineValue
                            : requireValue(args, ++i, argument);

                    features.add(value);
                }

                case "--jvm", "-J" -> {
                    String value = inlineValue != null
                            ? inlineValue
                            : requireValue(args, ++i, argument);

                    jvmArguments.add(value);
                }

                default -> unknownArg(argument);
            }
        }

        return new LauncherArguments(version, help, verbose, auth, variables, features, jvmArguments, minecraftArguments);
    }

    private static void unknownArg(String argument) {
        throw new IllegalArgumentException("Unknown argument: " + argument);
    }

    private static String requireValue(
            String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(
                    "Missing value for " + option);
        }

        return args[index];
    }

    private static void addVariable(
            Map<String, String> variables, String assignment) {
        int separator = assignment.indexOf('=');

        if (separator <= 0) {
            throw new IllegalArgumentException(
                    "Variable must have the form <name>=<value>: " + assignment);
        }

        String name = assignment.substring(0, separator);
        String value = assignment.substring(separator + 1);

        variables.put(name, value);
    }
}