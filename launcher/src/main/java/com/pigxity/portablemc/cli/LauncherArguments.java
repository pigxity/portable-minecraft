package com.pigxity.portablemc.cli;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record LauncherArguments(
        boolean showVersion,
        boolean showHelp,
        boolean verbose,
        Map<String, String> variables,
        Set<String> features,
        List<String> jvmArguments,
        List<String> minecraftArguments
) {}