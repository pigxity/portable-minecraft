package com.pigxity.portablemc.shared;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ContentHashes {
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final String SHA_1 = "SHA-1";
    private static final String SHA_256 = "SHA-256";

    private ContentHashes() {}

    public static String sha1(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            return hex(digest(input, SHA_1));
        }
    }

    public static String sha1(byte[] value) {
        return hex(messageDigest(SHA_1).digest(value));
    }

    public static boolean sha256Matches(InputStream first, InputStream second) throws IOException {
        return MessageDigest.isEqual(digest(first, SHA_256), digest(second, SHA_256));
    }

    private static byte[] digest(InputStream input, String algorithm) throws IOException {
        MessageDigest digest = messageDigest(algorithm);
        byte[] buffer = new byte[BUFFER_SIZE];
        int count;

        while ((count = input.read(buffer)) != -1) {
            digest.update(buffer, 0, count);
        }

        return digest.digest();
    }

    private static String hex(byte[] value) {
        return HexFormat.of().formatHex(value);
    }

    private static MessageDigest messageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "This Java runtime does not provide " + algorithm, exception);
        }
    }
}
