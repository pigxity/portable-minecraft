package com.pigxity.portablemc.rule.conditions;

import com.google.gson.JsonObject;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.model.RuleCondition;

import java.util.HashMap;
import java.util.Map;

public record FeaturesCondition(Map<String, Boolean> required) implements RuleCondition {
    public static FeaturesCondition parse(JsonObject object) {
        final Map<String, Boolean> features = new HashMap<>();

        for (var entry : object.entrySet()) {
            features.put(
                    entry.getKey(),
                    entry.getValue().getAsBoolean()
            );
        }

        return new FeaturesCondition(features);
    }

    @Override
    public boolean matches(RuleEnvironment environment) {
        return required.entrySet().stream()
                .allMatch(entry ->
                        environment.features()
                                .getOrDefault(entry.getKey(), false)
                                .equals(entry.getValue())
                );
    }
}