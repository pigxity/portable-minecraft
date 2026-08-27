package com.pigxity.portablemc.rule.conditions.os;

import com.google.gson.JsonObject;
import com.pigxity.portablemc.rule.UnsupportedRuleException;

import java.util.Arrays;

record VersionRange(String minimum, String maximum) {
    static VersionRange parse(JsonObject object) {
        return new VersionRange(
                object.has("min") ? object.get("min").getAsString() : null,
                object.has("max") ? object.get("max").getAsString() : null);
    }

    private static int compare(String first, String second) {
        int[] a = components(first);
        int[] b = components(second);

        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int av = i < a.length ? a[i] : 0;
            int bv = i < b.length ? b[i] : 0;

            int comparison = Integer.compare(av, bv);
            if (comparison != 0) {
                return comparison;
            }
        }

        return 0;
    }

    static int[] components(String version) {
        if (version == null || !version.matches("\\d+(?:\\.\\d+)*")) {
            throw new UnsupportedRuleException("Invalid numeric version: " + version);
        }

        return Arrays.stream(version.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    boolean contains(String version) {
        return (minimum == null || compare(version, minimum) >= 0)
                && (maximum == null || compare(version, maximum) < 0);
    }
}
