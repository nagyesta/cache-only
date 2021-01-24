package com.github.nagyesta.cacheonly.core.conurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ForkJoinPool;

/**
 * Defines generic concurrency related behavior.
 */
public interface ConcurrentOperationSupport {
    /**
     * One minute in milliseconds.
     */
    long ONE_MINUTE = 60000L;

    /**
     * Returns the ForkJoinPool we need to use for calling asynchronously.
     *
     * @return pool.
     */
    @NotNull
    default ForkJoinPool forkJoinPool() {
        return ForkJoinPool.commonPool();
    }

    /**
     * Returns the maximum number of milliseconds we will wait maximum for fetching items from the cache.
     *
     * @return timeout in milliseconds.
     */
    default long timeoutMillis() {
        return ONE_MINUTE;
    }
}
