package com.pigxity.portablemc.rule.conditions;

import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.model.RuleCondition;

import java.util.Map;

public record FeaturesCondition(Map<String, Boolean> required) implements RuleCondition {

    public FeaturesCondition {
        required = Map.copyOf(required);
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