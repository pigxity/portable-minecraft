package com.pigxity.portablemc.rule.model;

import com.pigxity.portablemc.rule.RuleEnvironment;

public interface RuleCondition {
    boolean matches(RuleEnvironment environment);
}
