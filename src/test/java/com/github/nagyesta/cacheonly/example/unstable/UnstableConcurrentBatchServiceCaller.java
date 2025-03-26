package com.github.nagyesta.cacheonly.example.unstable;

import com.github.nagyesta.cacheonly.core.CacheRefreshStrategy;
import com.github.nagyesta.cacheonly.raw.concurrent.AsyncBatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@SuppressWarnings("checkstyle:MagicNumber")
@Slf4j
public class UnstableConcurrentBatchServiceCaller implements AsyncBatchServiceCaller<List<Long>, List<String>> {

    @NotNull
    @Override
    public CacheRefreshStrategy refreshStrategy() {
        return CacheRefreshStrategy.PESSIMISTIC;
    }

    @Override
    public long timeoutMillis() {
        return 10;
    }

    @Override
    public int maxPartitionSize() {
        return 5;
    }

    @Override
    public @NotNull ForkJoinPool forkJoinPool() {
        return new ForkJoinPool(2);
    }

    @Nullable
    @Override
    public List<String> callBatchService(final @NotNull List<Long> batchRequest)
            throws BatchServiceException {
        handleExceptionalCases(batchRequest);
        final var result = batchRequest.stream()
                .filter(i -> i > 10)
                .map(String::valueOf)
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }

    private void handleExceptionalCases(final @NotNull List<Long> batchRequest) {
        if (batchRequest.stream().anyMatch(i -> i < -30L)) {
            try {
                final var start = System.currentTimeMillis();
                Thread.sleep(60);
                final var end = System.currentTimeMillis();
                log.trace("Took: {} ms", (end - start));
            } catch (final InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
        if (batchRequest.stream().anyMatch(i -> i < 0L)) {
            throw new BatchServiceException("Value is below 0.");
        }
    }
}
