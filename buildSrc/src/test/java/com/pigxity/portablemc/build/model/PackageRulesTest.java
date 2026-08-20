package com.pigxity.portablemc.build.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageRulesTest {
    @Test
    void rulesDocumentKeepsLaunchDataButRemovesDownloads() {
        JsonObject packageJson = JsonParser.parseString("""
                {
                  "id":"26.1",
                  "type":"release",
                  "mainClass":"net.minecraft.client.main.Main",
                  "assetIndex":{"id":"26","sha1":"abc","url":"https://invalid/index"},
                  "arguments":{"game":["--demo",{"rules":[{"action":"allow","features":{"is_demo_user":true}}],"value":"--demo"}]},
                  "libraries":[{
                    "name":"org.lwjgl:lwjgl:3.4.2",
                    "rules":[{"action":"allow","os":{"name":"windows"}}],
                    "downloads":{
                      "artifact":{"path":"org/lwjgl/lwjgl/3.4.2/lwjgl-3.4.2.jar","sha1":"one","url":"https://invalid/main"},
                      "classifiers":{"natives-windows-arm64":{"path":"org/lwjgl/lwjgl/3.4.2/lwjgl-3.4.2-natives-windows-arm64.jar","sha1":"two","url":"https://invalid/native"}}
                    }
                  }]
                }
                """).getAsJsonObject();

        JsonObject rules = PackageRules.create(packageJson);

        assertEquals("26.1", rules.get("version").getAsString());
        assertEquals("26", rules.get("assetIndex").getAsString());
        assertTrue(rules.has("arguments"));
        assertFalse(rules.toString().contains("downloads"));
        assertFalse(rules.toString().contains("https://"));
        assertEquals("org.lwjgl:lwjgl:3.4.2:natives-windows-arm64",
                rules.getAsJsonArray("libraries").get(1).getAsJsonObject().get("name").getAsString());
        assertEquals("windows", rules.getAsJsonArray("libraries").get(1).getAsJsonObject()
                .getAsJsonArray("rules").get(0).getAsJsonObject().getAsJsonObject("os").get("name").getAsString());
    }
}
