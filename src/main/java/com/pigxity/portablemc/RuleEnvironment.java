package com.pigxity.portablemc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public record RuleEnvironment(OperatingSystem operatingSystem, Map<String, Boolean> features) {
    public static final Set<String> SUPPORTED_FEATURES = Set.of(
            "is_demo_user",
            "has_custom_resolution",
            "has_quick_plays_support",
            "is_quick_play_singleplayer",
            "is_quick_play_multiplayer",
            "is_quick_play_realms");

    public RuleEnvironment {
        features = Map.copyOf(features);
    }

    public static Map<String, Boolean> defaultFeatures() {
        Map<String, Boolean> features = new LinkedHashMap<>();
        for (String name : SUPPORTED_FEATURES) {
            features.put(name, false);
        }
        return Map.copyOf(features);
    }

    public static RuleEnvironment current() {
        return new RuleEnvironment(OperatingSystem.current(), defaultFeatures());
    }
}
