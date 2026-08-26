package com.pigxity.portablemc.build.download;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import com.pigxity.portablemc.shared.ContentHashes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

class VerifiedDownloaderTest {
    @TempDir Path temporaryDirectory;

    @Test
    void verifiedCacheDownloadsOnceAndRejectsInvalidContent() throws Exception {
        byte[] content = "portable minecraft".getBytes(StandardCharsets.UTF_8);
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext(
                "/file",
                exchange -> {
                    requests.incrementAndGet();
                    exchange.sendResponseHeaders(200, content.length);
                    exchange.getResponseBody().write(content);
                    exchange.close();
                });
        server.start();

        try {
            VerifiedDownloader downloader = new VerifiedDownloader();
            String url = "http://localhost:" + server.getAddress().getPort() + "/file";
            Path cached = temporaryDirectory.resolve("cache/file");

            downloader.downloadVerified(url, cached, ContentHashes.sha1(content));
            downloader.downloadVerified(url, cached, ContentHashes.sha1(content));

            assertArrayEquals(content, Files.readAllBytes(cached));
            assertEquals(1, requests.get());
            assertThrows(
                    DownloadVerificationException.class,
                    () ->
                            downloader.downloadVerified(
                                    url,
                                    temporaryDirectory.resolve("bad"),
                                    "0000000000000000000000000000000000000000"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void ttlCacheRefreshesOnlyAfterExpiry() throws Exception {
        byte[] content = "manifest".getBytes(StandardCharsets.UTF_8);
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext(
                "/manifest",
                exchange -> {
                    requests.incrementAndGet();
                    exchange.sendResponseHeaders(200, content.length);
                    exchange.getResponseBody().write(content);
                    exchange.close();
                });
        server.start();

        try {
            VerifiedDownloader downloader = new VerifiedDownloader();
            String url = "http://localhost:" + server.getAddress().getPort() + "/manifest";
            Path cached = temporaryDirectory.resolve("manifest.json");

            downloader.downloadWithTtl(url, cached, Duration.ofHours(1));
            downloader.downloadWithTtl(url, cached, Duration.ofHours(1));

            assertEquals(1, requests.get());
        } finally {
            server.stop(0);
        }
    }
}
