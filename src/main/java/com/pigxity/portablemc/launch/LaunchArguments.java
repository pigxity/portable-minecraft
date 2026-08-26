package com.pigxity.portablemc.launch;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pigxity.portablemc.rule.RuleParser;
import com.pigxity.portablemc.rule.RuleResolver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LaunchArguments {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    private final RuleParser ruleParser;
    private final RuleResolver rules;
    private final Map<String, String> substitutions;

    public LaunchArguments(
            RuleParser ruleParser, RuleResolver rules, Map<String, String> substitutions) {
        this.ruleParser = ruleParser;
        this.rules = rules;
        this.substitutions = Map.copyOf(substitutions);
    }

    public List<String> resolve(JsonArray definitions) {
        List<String> arguments = new ArrayList<>();

        for (JsonElement definition : definitions) {
            if (definition.isJsonPrimitive() && definition.getAsJsonPrimitive().isString()) {
                arguments.add(substitute(definition.getAsString()));
                continue;
            }

            if (!definition.isJsonObject()) {
                throw new IllegalArgumentException(
                        "Unsupported argument definition: " + definition);
            }

            JsonObject conditional = definition.getAsJsonObject();
            if (!conditional.has("rules") || !conditional.has("value")) {
                throw new IllegalArgumentException(
                        "Conditional argument requires rules and value: " + conditional);
            }

            if (rules.isAllowed(ruleParser.parse(conditional.getAsJsonArray("rules")))) {
                addValue(arguments, conditional.get("value"));
            }
        }

        return List.copyOf(arguments);
    }

    private void addValue(List<String> destination, JsonElement value) {
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            destination.add(substitute(value.getAsString()));
            return;
        }

        if (value.isJsonArray()) {
            for (JsonElement item : value.getAsJsonArray()) {
                if (!item.isJsonPrimitive() || !item.getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException(
                            "Argument array values must be strings: " + item);
                }

                destination.add(substitute(item.getAsString()));
            }

            return;
        }

        throw new IllegalArgumentException(
                "Argument value must be a string or string array: " + value);
    }

    private String substitute(String value) {
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String replacement = substitutions.get(matcher.group(1));
            if (replacement == null) {
                throw new IllegalArgumentException(
                        "No value was supplied for argument placeholder ${"
                                + matcher.group(1)
                                + "}");
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);

        return result.toString();
    }

    public static List<String> mergeArgs(List<String> first, List<String> second) {
        Map<String, String> merged = new LinkedHashMap<>();

        for (int i = 0; i < first.size(); i += 2) {
            merged.put(first.get(i), first.get(i + 1));
        }

        for (int i = 0; i < second.size(); i += 2) {
            merged.put(second.get(i), second.get(i + 1));
        }

        List<String> result = new ArrayList<>();
        merged.forEach((key, value) -> {
            result.add(key);
            result.add(value);
        });

        return result;
    }
}
