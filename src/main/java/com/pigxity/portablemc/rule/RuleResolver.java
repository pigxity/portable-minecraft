package com.pigxity.portablemc.rule;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pigxity.portablemc.platform.OperatingSystem;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class RuleResolver {
    private static final Set<String> RULE_KEYS = Set.of("action", "os", "features");
    private static final Set<String> OS_KEYS = Set.of("name", "version", "arch");
    private static final Set<String> ACTIONS = Set.of("allow", "disallow");
    private static final Set<String> OS_NAMES = Set.of("windows", "osx", "linux");

    private final RuleEnvironment environment;

    public RuleResolver(RuleEnvironment environment) {
        this.environment = environment;
    }

    public boolean isAllowed(JsonArray rules) {
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        for (JsonElement element : rules) {
            validate(element);
        }

        boolean allowed = false;
        for (JsonElement element : rules) {
            JsonObject rule = element.getAsJsonObject();
            if (matches(rule)) {
                allowed = "allow".equals(rule.get("action").getAsString());
            }
        }
        return allowed;
    }

    private void validate(JsonElement element) {
        if (!element.isJsonObject()) {
            throw unsupported("Rule must be a JSON object: " + element);
        }
        JsonObject rule = element.getAsJsonObject();
        rejectUnknownKeys(rule, RULE_KEYS, "rule");
        if (!rule.has("action") || !rule.get("action").isJsonPrimitive()
                || !ACTIONS.contains(rule.get("action").getAsString())) {
            throw unsupported("Unsupported or missing rule action: " + rule);
        }
        if (rule.has("os")) {
            if (!rule.get("os").isJsonObject()) {
                throw unsupported("Rule os value must be an object: " + rule);
            }
            JsonObject os = rule.getAsJsonObject("os");
            rejectUnknownKeys(os, OS_KEYS, "rule os");
            if (os.has("name") && !OS_NAMES.contains(os.get("name").getAsString())) {
                throw unsupported("Unsupported operating-system rule name: " + os.get("name"));
            }
            validatePattern(os, "version");
            validatePattern(os, "arch");
        }
        if (rule.has("features")) {
            if (!rule.get("features").isJsonObject()) {
                throw unsupported("Rule features value must be an object: " + rule);
            }
            JsonObject features = rule.getAsJsonObject("features");
            for (String feature : features.keySet()) {
                if (!RuleEnvironment.SUPPORTED_FEATURES.contains(feature)
                        || !features.get(feature).isJsonPrimitive()
                        || !features.get(feature).getAsJsonPrimitive().isBoolean()) {
                    throw unsupported("Unsupported feature rule: " + feature);
                }
            }
        }
    }

    private boolean matches(JsonObject rule) {
        if (rule.has("os") && !matchesOperatingSystem(rule.getAsJsonObject("os"))) {
            return false;
        }
        if (rule.has("features")) {
            for (var feature : rule.getAsJsonObject("features").entrySet()) {
                boolean actual = environment.features().getOrDefault(feature.getKey(), false);
                if (actual != feature.getValue().getAsBoolean()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchesOperatingSystem(JsonObject os) {
        OperatingSystem actual = environment.operatingSystem();
        return (!os.has("name") || os.get("name").getAsString().equals(actual.name()))
                && (!os.has("version") || Pattern.matches(os.get("version").getAsString(), actual.version()))
                && (!os.has("arch") || Pattern.matches(os.get("arch").getAsString(), actual.architecture()));
    }

    private static void validatePattern(JsonObject object, String key) {
        if (!object.has(key)) {
            return;
        }
        if (!object.get(key).isJsonPrimitive() || !object.get(key).getAsJsonPrimitive().isString()) {
            throw unsupported("Rule " + key + " must be a regex string");
        }
        try {
            Pattern.compile(object.get(key).getAsString());
        } catch (PatternSyntaxException exception) {
            throw unsupported("Invalid " + key + " rule regex: " + exception.getMessage());
        }
    }

    private static void rejectUnknownKeys(JsonObject object, Set<String> supported, String context) {
        for (String key : object.keySet()) {
            if (!supported.contains(key)) {
                throw unsupported("Unsupported " + context + " field: " + key);
            }
        }
    }

    private static UnsupportedRuleException unsupported(String message) {
        return new UnsupportedRuleException(message);
    }
}
