package com.pigxity.portablemc.rule;

import com.pigxity.portablemc.platform.OperatingSystem;

import java.util.Map;

public record RuleEnvironment(OperatingSystem operatingSystem, Map<String, Boolean> features) {
    public static RuleEnvironment current() {
        return new RuleEnvironment(OperatingSystem.current(), Map.of());
    }
}
