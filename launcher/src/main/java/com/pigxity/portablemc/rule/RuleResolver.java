package com.pigxity.portablemc.rule;

import com.pigxity.portablemc.rule.model.Rule;
import com.pigxity.portablemc.rule.model.RuleAction;

import java.util.List;

public final class RuleResolver {
    private final RuleEnvironment environment;

    public RuleResolver(RuleEnvironment environment) {
        this.environment = environment;
    }

    public boolean isAllowed(List<Rule> rules) {
        if (rules.isEmpty()) {
            return true;
        }

        return rules.stream()
                .filter(rule -> rule.matches(environment))
                .reduce((first, second) -> second)
                .map(rule -> rule.action() == RuleAction.ALLOW)
                .orElse(false);
    }
}
