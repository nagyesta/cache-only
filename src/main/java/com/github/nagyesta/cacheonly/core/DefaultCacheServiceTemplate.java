package com.github.nagyesta.cacheonly.core;

import com.github.nagyesta.cacheonly.core.exception.CacheMissException;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link CachingServiceTemplate}.
 *
 * @param <BR> The type of the batch request.
 * @param <BS> The type of the batch response.
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the ID which allows unique association of partial request
 *             and partial response pairs in the scope of the batch.
 */
@SuppressWarnings("java:S119") //the type parameter names are easier to recognize this way
public class DefaultCacheServiceTemplate<BR, BS, PR, PS, C, I>
        extends AbstractCacheServiceTemplate<BatchServiceCaller<BR, BS>, PartialCacheSupport<PR, PS, C, I>, BR, BS, PR, PS, C, I> {

    /**
     * Creates a new instance and injects all the dependencies which are necessary for it to work.
     *
     * @param partialCacheSupport      The component defining how caching should work for a partial request.
     * @param batchRequestTransformer  The component handling transformations between batch and partial requests.
     * @param batchResponseTransformer The component handling transformations between batch and partial responses.
     * @param batchServiceCaller       The wrapper which is calling the real batch service in case of cache miss.
     */
    public DefaultCacheServiceTemplate(
            final @NotNull PartialCacheSupport<PR, PS, C, I> partialCacheSupport,
            final @NotNull BatchRequestTransformer<BR, PR, I> batchRequestTransformer,
            final @NotNull BatchResponseTransformer<BS, PS, I> batchResponseTransformer,
            final @NotNull BatchServiceCaller<BR, BS> batchServiceCaller) {
        super(partialCacheSupport, batchRequestTransformer, batchResponseTransformer, batchServiceCaller);
    }

    @NotNull
    @Override
    protected Map<I, PS> fetchAllFromCache(
            final @NotNull CacheRefreshStrategy strategy,
            final @NotNull Map<I, PR> requestMap)
            throws CacheMissException {
        final var start = System.currentTimeMillis();
        final Map<I, PS> result = new HashMap<>();
        try {
            for (final var entry : requestMap.entrySet()) {
                fetchOneFromCache(strategy, entry.getValue())
                        .ifPresent(v -> result.put(entry.getKey(), v));
            }
            return result;
        } finally {
            final var end = System.currentTimeMillis();
            logger().debug("Fetch all from cache completed under {} ms.", end - start);
        }
    }

    @Override
    @NotNull
    protected Map<I, PS> callOriginWithPartitions(
            final @NotNull List<Map<I, PR>> requestPartitions)
            throws BatchServiceException {
        final var start = System.currentTimeMillis();
        final Map<I, PS> response = new HashMap<>();
        try {
            for (final var partitionedMap : requestPartitions) {
                response.putAll(fetchSinglePartitionFromOrigin(partitionedMap, batchServiceCaller().refreshStrategy()));
            }
            return response;
        } finally {
            final var end = System.currentTimeMillis();
            logger().debug("Fetch all from origin completed under {} ms.", end - start);
        }
    }

}
