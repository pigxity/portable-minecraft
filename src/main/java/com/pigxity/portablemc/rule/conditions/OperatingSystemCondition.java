package com.pigxity.portablemc.rule.conditions;

import com.google.gson.JsonObject;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.model.RuleCondition;

import java.util.regex.Pattern;

import static com.pigxity.portablemc.rule.RuleUtils.pattern;

public record OperatingSystemCondition(String name, Pattern version, Pattern architecture)
        implements RuleCondition {
    @Override
    public boolean matches(RuleEnvironment environment) {
        OperatingSystem actual = environment.operatingSystem();

        return matches(name, actual.name())
                && matches(version, actual.version())
                && matches(architecture, actual.architecture());
    }

    public static OperatingSystemCondition parse(JsonObject object) {
        String name = object.has("name") ? object.get("name").getAsString() : null;

        Pattern version =
                object.has("version") ? pattern(object.get("version").getAsString()) : null;

        Pattern architecture =
                object.has("arch") ? pattern(object.get("arch").getAsString()) : null;

        return new OperatingSystemCondition(name, version, architecture);
    }

    private static boolean matches(String expected, String actual) {
        return expected == null || expected.equals(actual);
    }

    private static boolean matches(Pattern expected, String actual) {
        return expected == null || expected.matcher(actual).matches();
    }
}
