package com.pigxity.portablemc.launch;

import java.util.List;

final class JvmConfiguration {
    private JvmConfiguration() {}

    static void applySystemProperties(List<String> arguments) {
        for (String argument : arguments) {
            if (!argument.startsWith("-D")) {
                continue;
            }

            int separator = argument.indexOf('=', 2);

            if (separator < 0) {
                System.setProperty(argument.substring(2), "");
            } else {
                System.setProperty(
                        argument.substring(2, separator), argument.substring(separator + 1));
            }
        }
    }
}
