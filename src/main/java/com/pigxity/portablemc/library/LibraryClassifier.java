package com.pigxity.portablemc.library;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public enum LibraryClassifier {
    ARM64(".*(?:arm64|aarch_64).*", "aarch64", "arm64"),
    X64(".*x86_64.*", "amd64", "x86_64"),
    X86(".*-x86$", "x86", "i386", "i486", "i586", "i686"),
    NATIVE("natives-.*", "amd64", "x86_64"),
    UNIVERSAL(".*");

    private final Pattern pattern;
    private final Set<String> architectures;

    LibraryClassifier(String pattern, String... architectures) {
        this.pattern = Pattern.compile(pattern);
        this.architectures = Set.of(architectures);
    }

    public static LibraryClassifier fromCoordinate(String coordinate) {
        String classifier = MavenCoordinates.classifier(coordinate).toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(candidate -> candidate.pattern.matcher(classifier).matches())
                .findFirst()
                .orElse(UNIVERSAL);
    }

    public boolean supports(String architecture) {
        return this == UNIVERSAL
                || architectures.contains(architecture.toLowerCase(Locale.ROOT));
    }
}
