package com.github.nagyesta.cacheonly.core;

import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The template that is handling caching activities and calls to the batch service
 * depending on the cache status and the configuration provided.
 *
 * @param <BR> The type of the batch request.
 * @param <BS> The type of the batch response.
 */
public interface CachingServiceTemplate<BR, BS> {

    /**
     * Processes the provided batch request and returns an appropriate batch response.
     * Uses cache when possible the way the configuration allows.
     *
     * @param request The batch request we need to process.
     * @return The batch response either from the real service or from cache.
     * @throws BatchServiceException When the service call fails with an exception.
     */
    @Nullable
    BS callCacheableBatchService(@NotNull BR request) throws BatchServiceException;

    /**
     * Processes the provided batch request and returns an appropriate batch response.
     * Does not read from cache but puts all of the returned partial responses into it.
     * Can be ideal for background cache warm-ups.
     *
     * @param request The batch request we need to process.
     * @return The batch response from the real service.
     * @throws BatchServiceException When the service call fails with an exception.
     */
    @Nullable
    BS callBatchServiceAndPutAllToCache(@NotNull BR request) throws BatchServiceException;
}
