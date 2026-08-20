package com.pigxity.portablemc.rule;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pigxity.portablemc.rule.conditions.FeaturesCondition;
import com.pigxity.portablemc.rule.conditions.OperatingSystemCondition;
import com.pigxity.portablemc.rule.model.Rule;
import com.pigxity.portablemc.rule.model.RuleAction;
import com.pigxity.portablemc.rule.model.RuleCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class RuleParser {

    public List<Rule> parse(JsonArray array) {
        List<Rule> rules = new ArrayList<>();

        for (JsonElement element : array) {
            rules.add(parseRule(element.getAsJsonObject()));
        }

        return rules;
    }

    private Rule parseRule(JsonObject object) {
        RuleAction action = RuleAction.fromString(object.get("action").getAsString());

        List<RuleCondition> conditions = new ArrayList<>();

        if (object.has("os")) {
            conditions.add(parseOperatingSystem(object.getAsJsonObject("os")));
        }

        if (object.has("features")) {
            conditions.add(parseFeatures(object.getAsJsonObject("features")));
        }

        return new Rule(action, conditions);
    }

    private OperatingSystemCondition parseOperatingSystem(JsonObject object) {
        String name = object.has("name")
                ? object.get("name").getAsString()
                : null;

        Pattern version = object.has("version")
                ? pattern(object.get("version").getAsString())
                : null;

        Pattern architecture = object.has("arch")
                ? pattern(object.get("arch").getAsString())
                : null;

        return new OperatingSystemCondition(
                name,
                version,
                architecture
        );
    }

    private FeaturesCondition parseFeatures(JsonObject object) {
        Map<String, Boolean> features = new HashMap<>();

        for (var entry : object.entrySet()) {
            features.put(
                    entry.getKey(),
                    entry.getValue().getAsBoolean()
            );
        }

        return new FeaturesCondition(features);
    }

    private static Pattern pattern(String regex) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
            throw unsupported(
                    "Invalid rule regex: " + regex
            );
        }
    }

    private static UnsupportedRuleException unsupported(String message) {
        return new UnsupportedRuleException(message);
    }
}