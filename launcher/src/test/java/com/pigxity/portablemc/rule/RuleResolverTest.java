package com.pigxity.portablemc.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.pigxity.portablemc.platform.OperatingSystem;
import com.pigxity.portablemc.rule.model.Rule;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class RuleResolverTest {
    private final RuleParser parser = new RuleParser();
    private final RuleEnvironment windows =
            new RuleEnvironment(
                    new OperatingSystem("windows", "10.0", "amd64"),
                    Set.of());

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
        assertThrows(UnsupportedRuleException.class, () -> parse("[{\"action\":\"sometimes\"}]"));
        assertThrows(
                UnsupportedRuleException.class,
                () -> parse("[{\"action\":\"allow\",\"os\":{\"version\":\"[\"}}]"));
    }

    @Test
    void resolvesInclusiveMinimumAndExclusiveMaximumOsVersionRanges() {
        List<Rule> minimum =
                parse(
                        """
                        [{"action":"allow","os":{"name":"windows","versionRange":{"min":"10.0.17134"}}}]
                        """);
        List<Rule> maximum =
                parse(
                        """
                        [{"action":"allow","os":{"name":"windows","versionRange":{"max":"10.0.17134"}}}]
                        """);

        assertFalse(resolverFor("10.0.17133").isAllowed(minimum));
        assertTrue(resolverFor("10.0.17134").isAllowed(minimum));
        assertTrue(resolverFor("10.0.19045").isAllowed(minimum));

        assertTrue(resolverFor("10.0.17133").isAllowed(maximum));
        assertFalse(resolverFor("10.0.17134").isAllowed(maximum));
        assertFalse(resolverFor("10.0.19045").isAllowed(maximum));
    }

    private RuleResolver resolverFor(String version) {
        return new RuleResolver(
                new RuleEnvironment(new OperatingSystem("windows", version, "amd64"), Set.of()));
    }

    private List<Rule> parse(String json) {
        return parser.parse(JsonParser.parseString(json).getAsJsonArray());
    }
}
