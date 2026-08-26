package com.pigxity.portablemc.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class ContentHashesTest {
    @Test
    void calculatesSha1AndComparesSha256Content() throws Exception {
        byte[] content = "portable minecraft".getBytes(StandardCharsets.UTF_8);

        assertEquals("aa1afef04ca37c64d8eabc31e7343c771e60d25c", ContentHashes.sha1(content));
        assertTrue(
                ContentHashes.sha256Matches(
                        new ByteArrayInputStream(content), new ByteArrayInputStream(content)));
        assertFalse(
                ContentHashes.sha256Matches(
                        new ByteArrayInputStream(content),
                        new ByteArrayInputStream("different".getBytes(StandardCharsets.UTF_8))));
    }
}
