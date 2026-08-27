package com.pigxity.portablemc.rule;

import com.pigxity.portablemc.platform.OperatingSystem;

import java.util.Set;

public record RuleEnvironment(OperatingSystem operatingSystem, Set<String> features) {
    public static RuleEnvironment current(Set<String> features) {
        return new RuleEnvironment(OperatingSystem.current(), features);
    }
}
