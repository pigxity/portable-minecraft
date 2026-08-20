package com.pigxity.portablemc.rule.model;

import com.pigxity.portablemc.rule.UnsupportedRuleException;

public enum RuleAction {
    ALLOW,
    DISALLOW;

    public static RuleAction fromString(String string) {
        return switch (string) {
            case "allow" -> RuleAction.ALLOW;
            case "disallow" -> RuleAction.DISALLOW;
            default -> throw new UnsupportedRuleException(
                    "Unsupported rule action: " + string
            );
        };
    }
}