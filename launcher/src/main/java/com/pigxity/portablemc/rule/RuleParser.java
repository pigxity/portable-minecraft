package com.pigxity.portablemc.rule;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pigxity.portablemc.rule.conditions.FeaturesCondition;
import com.pigxity.portablemc.rule.conditions.os.OperatingSystemCondition;
import com.pigxity.portablemc.rule.model.Rule;
import com.pigxity.portablemc.rule.model.RuleAction;
import com.pigxity.portablemc.rule.model.RuleCondition;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class RuleParser {
    public static final Map<String, Function<JsonObject, RuleCondition>> PREDICATES =
            Map.of(
                    "os", (object) -> OperatingSystemCondition.parse(object.getAsJsonObject("os")),
                    "features",
                            (object) ->
                                    FeaturesCondition.parse(object.getAsJsonObject("features")));

    public List<Rule> parse(JsonArray array) {
        return array.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(this::parseRule)
                .toList();
    }

    private Rule parseRule(JsonObject object) {
        RuleAction action = RuleAction.fromString(object.get("action").getAsString());

        final List<RuleCondition> conditions =
                PREDICATES.entrySet().stream()
                        .filter(entry -> object.has(entry.getKey()))
                        .map(entry -> entry.getValue().apply(object))
                        .toList();

        return new Rule(action, conditions);
    }
}
