package com.pigxity.portablemc.rule;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class RuleUtils {
    public static Pattern pattern(String regex) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
            throw new UnsupportedRuleException("Invalid rule regex: " + regex);
        }
    }
}
