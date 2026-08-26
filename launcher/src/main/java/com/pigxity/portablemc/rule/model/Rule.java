package com.pigxity.portablemc.rule.model;

import com.pigxity.portablemc.rule.RuleEnvironment;

import java.util.List;

public record Rule(RuleAction action, List<RuleCondition> conditions) {
    public boolean matches(RuleEnvironment environment) {
        return conditions.stream().allMatch(condition -> condition.matches(environment));
    }
}
