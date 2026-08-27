package com.pigxity.portablemc.rule.conditions.os;

import com.google.gson.JsonObject;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.RuleEnvironment;
import com.pigxity.portablemc.rule.model.RuleCondition;

import java.util.regex.Pattern;

import static com.pigxity.portablemc.rule.RuleUtils.pattern;

public record OperatingSystemCondition(String name, Pattern version, VersionRange versionRange, Pattern architecture) implements RuleCondition {

    public static OperatingSystemCondition parse(JsonObject object) {
        String name = object.has("name") ? object.get("name").getAsString() : null;

        Pattern version =
                object.has("version") ? pattern(object.get("version").getAsString()) : null;

        VersionRange versionRange =
                object.has("versionRange")
                        ? VersionRange.parse(object.getAsJsonObject("versionRange"))
                        : null;

        Pattern architecture =
                object.has("arch") ? pattern(object.get("arch").getAsString()) : null;

        return new OperatingSystemCondition(name, version, versionRange, architecture);
    }

    private static boolean matches(String expected, String actual) {
        return expected == null || expected.equals(actual);
    }

    private static boolean matches(Pattern expected, String actual) {
        return expected == null || expected.matcher(actual).matches();
    }

    @Override
    public boolean matches(RuleEnvironment environment) {
        OperatingSystem actual = environment.operatingSystem();

        return matches(name, actual.name())
                && matches(version, actual.version())
                && (versionRange == null || versionRange.contains(actual.version()))
                && matches(architecture, actual.architecture());
    }
}
