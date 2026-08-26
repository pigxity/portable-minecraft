package com.pigxity.portablemc.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.model.Rule;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class RuleResolverTest {
    private final RuleParser parser = new RuleParser();
    private final RuleEnvironment windows =
            new RuleEnvironment(
                    new OperatingSystem("windows", "10.0", "amd64"),
                    Map.of(
                            "is_demo_user", false,
                            "has_custom_resolution", false,
                            "has_quick_plays_support", false,
                            "is_quick_play_singleplayer", false,
                            "is_quick_play_multiplayer", false,
                            "is_quick_play_realms", false));

    @Test
    void resolvesOrderedAllowAndDisallowRules() {
        RuleResolver resolver = new RuleResolver(windows);

        assertTrue(resolver.isAllowed(parse("[]")));
        assertTrue(
                resolver.isAllowed(
                        parse(
                                """
                                [{"action":"allow","os":{"name":"windows"}}]
                                """)));
        assertFalse(
                resolver.isAllowed(
                        parse(
                                """
                                [{"action":"allow","os":{"name":"linux"}}]
                                """)));
        assertFalse(
                resolver.isAllowed(
                        parse(
                                """
                                [{"action":"allow","os":{"name":"windows"}},
                                 {"action":"disallow","os":{"name":"windows"}}]
                                """)));
    }

    @Test
    void rejectsUnsupportedActionsAndInvalidPatterns() {
        assertThrows(
                UnsupportedRuleException.class,
                () -> parse("[{\"action\":\"sometimes\"}]"));
        assertThrows(
                UnsupportedRuleException.class,
                () ->
                        parse("[{\"action\":\"allow\",\"os\":{\"version\":\"[\"}}]"));
    }

    private List<Rule> parse(String json) {
        return parser.parse(JsonParser.parseString(json).getAsJsonArray());
    }
}
