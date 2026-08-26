package com.pigxity.portablemc.rule.model;

import com.pigxity.portablemc.rule.UnsupportedRuleException;

public enum RuleAction {
    ALLOW,
    DISALLOW;

    public static RuleAction fromString(String string) {
        try {
            return RuleAction.valueOf(string.toUpperCase());
        } catch (Exception e) {
            throw new UnsupportedRuleException("Unsupported rule action: " + string);
        }
    }
}
