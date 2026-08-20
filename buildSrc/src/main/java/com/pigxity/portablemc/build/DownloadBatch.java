package com.pigxity.portablemc.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class DownloadBatch {
    private DownloadBatch() {
    }

    static <T> void run(Collection<T> items, ThrowingConsumer<T> action) throws IOException {
        int workerCount = Math.min(16, Math.max(1, Runtime.getRuntime().availableProcessors()));
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        try {
            List<Future<?>> futures = new ArrayList<>(items.size());
            for (T item : items) {
                futures.add(executor.submit(() -> {
                    action.accept(item);
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Download interrupted", exception);
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    if (cause instanceof IOException ioException) {
                        throw ioException;
                    }
                    throw new IOException("Download failed", cause);
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @FunctionalInterface
    interface ThrowingConsumer<T> {
        void accept(T item) throws Exception;
    }
}
