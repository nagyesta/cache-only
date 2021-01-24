package com.github.nagyesta.cacheonly.raw;

import com.github.nagyesta.cacheonly.core.CacheRefreshStrategy;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines how the underlying batch service will behave when we interact with it.
 *
 * @param <BR> The type of the batch request.
 * @param <BS> The type of the batch response.
 */
public interface BatchServiceCaller<BR, BS> {

    /**
     * Returns the maximum number of items we can query using a single batch call. Must be larger than 0.
     *
     * @return maximum batch size
     */
    default int maxPartitionSize() {
        return Integer.MAX_VALUE;
    }

    /**
     * The strategy we want to use for this batch service when we try to cache it.
     *
     * @return the strategy.
     */
    @NotNull
    default CacheRefreshStrategy refreshStrategy() {
        return CacheRefreshStrategy.OPTIMISTIC;
    }

    /**
     * Calls the real batch service with a single batch.
     * The number of items in the batch is guaranteed to be lower or equal to {@link #maxPartitionSize()}.
     *
     * @param batchRequest The batch request we need to send.
     * @return The batch response we received.
     * @throws BatchServiceException In case the batch service failed to resolve the response due to an error.
     *                               Should not be used if the response is empty because items are not found.
     */
    @Nullable
    BS callBatchService(@NotNull BR batchRequest) throws BatchServiceException;
}
