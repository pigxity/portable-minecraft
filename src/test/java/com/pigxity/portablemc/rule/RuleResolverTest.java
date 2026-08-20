package com.pigxity.portablemc.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.pigxity.portablemc.platform.OperatingSystem;

import org.junit.jupiter.api.Test;

import java.util.Map;

class RuleResolverTest {
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
        assertTrue(resolver.isAllowed(JsonParser.parseString("[]").getAsJsonArray()));
        assertTrue(
                resolver.isAllowed(
                        JsonParser.parseString(
                                        """
                                        [{"action":"allow","os":{"name":"windows"}}]
                                        """)
                                .getAsJsonArray()));
        assertFalse(
                resolver.isAllowed(
                        JsonParser.parseString(
                                        """
                                        [{"action":"allow","os":{"name":"linux"}}]
                                        """)
                                .getAsJsonArray()));
        assertFalse(
                resolver.isAllowed(
                        JsonParser.parseString(
                                        """
                                        [{"action":"allow","os":{"name":"windows"}},
                                         {"action":"disallow","os":{"name":"windows"}}]
                                        """)
                                .getAsJsonArray()));
    }

    @Test
    void rejectsEveryUnsupportedPartOfARule() {
        RuleResolver resolver = new RuleResolver(windows);
        assertThrows(
                UnsupportedRuleException.class,
                () ->
                        resolver.isAllowed(
                                JsonParser.parseString("[{\"action\":\"sometimes\"}]")
                                        .getAsJsonArray()));
        assertThrows(
                UnsupportedRuleException.class,
                () ->
                        resolver.isAllowed(
                                JsonParser.parseString(
                                                "[{\"action\":\"allow\",\"os\":{\"family\":\"nt\"}}]")
                                        .getAsJsonArray()));
        assertThrows(
                UnsupportedRuleException.class,
                () ->
                        resolver.isAllowed(
                                JsonParser.parseString(
                                                "[{\"action\":\"allow\",\"features\":{\"future_feature\":true}}]")
                                        .getAsJsonArray()));
        assertThrows(
                UnsupportedRuleException.class,
                () ->
                        resolver.isAllowed(
                                JsonParser.parseString(
                                                "[{\"action\":\"allow\",\"unexpected\":true}]")
                                        .getAsJsonArray()));
    }
}
