package com.github.nagyesta.cacheonly.core.conurrent;

import com.github.nagyesta.cacheonly.core.AbstractCacheServiceTemplate;
import com.github.nagyesta.cacheonly.core.CacheRefreshStrategy;
import com.github.nagyesta.cacheonly.core.exception.CacheMissException;
import com.github.nagyesta.cacheonly.raw.concurrent.AsyncBatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.concurrent.AsyncPartialCacheSupport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * {@link com.github.nagyesta.cacheonly.core.CachingServiceTemplate} implementation allowing
 * us to use {@link java.util.concurrent.CompletableFuture} tasks to allow concurrent processing.
 *
 * @param <BR> The type of the batch request.
 * @param <BS> The type of the batch response.
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the ID that allows unique association of partial request
 *             and partial response pairs in the scope of the batch.
 */
@SuppressWarnings("java:S119") //the type parameter names are easier to recognize this way
public class ConcurrentCacheServiceTemplate<BR, BS, PR, PS, C, I>
        extends AbstractCacheServiceTemplate<AsyncBatchServiceCaller<BR, BS>,
        AsyncPartialCacheSupport<PR, PS, C, I>, BR, BS, PR, PS, C, I> {


    private final ForkJoinPool cachePool;
    private final ForkJoinPool originPool;

    /**
     * Creates a new instance and injects all the dependencies which are necessary for it to work.
     *
     * @param partialCacheSupport      The component defining how caching should work for a partial request.
     * @param batchRequestTransformer  The component handling transformations between batch and partial requests.
     * @param batchResponseTransformer The component handling transformations between batch and partial responses.
     * @param batchServiceCaller       The wrapper that is calling the real batch service in case of cache miss.
     */
    public ConcurrentCacheServiceTemplate(
            final AsyncPartialCacheSupport<PR, PS, C, I> partialCacheSupport,
            final BatchRequestTransformer<BR, PR, I> batchRequestTransformer,
            final BatchResponseTransformer<BS, PS, I> batchResponseTransformer,
            final AsyncBatchServiceCaller<BR, BS> batchServiceCaller) {
        super(partialCacheSupport, batchRequestTransformer, batchResponseTransformer, batchServiceCaller);
        cachePool = partialCacheSupport.forkJoinPool();
        originPool = partialCacheSupport.forkJoinPool();
    }

    @Override
    protected Map<I, PS> fetchAllFromCache(
            final CacheRefreshStrategy strategy,
            final Map<I, PR> requestMap)
            throws CacheMissException {
        final var start = System.currentTimeMillis();
        final Map<I, PS> result = new ConcurrentHashMap<>();
        try {
            callCacheParallel(strategy, requestMap, result::put);
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof CacheMissException) {
                logger().info(e.getCause().getMessage(), e.getCause());
                throw new CacheMissException(e.getCause().getMessage());
            } else {
                logger().error("Failed to fetch from cache.", e.getCause());
                result.clear();
            }
        } catch (final InterruptedException e) {
            final var end = System.currentTimeMillis();
            logger().warn("Cache call stopped after {} (timeout set to {}).", (end - start), partialCacheSupport().timeoutMillis(), e);
            result.clear();
            Thread.currentThread().interrupt();
        } catch (final TimeoutException e) {
            final var end = System.currentTimeMillis();
            logger().warn("Cache call stopped after {} (timeout set to {}).", (end - start), partialCacheSupport().timeoutMillis(), e);
            result.clear();
        } finally {
            final var end = System.currentTimeMillis();
            logger().debug("Fetch all from cache completed under {} ms.", end - start);
        }
        return result;
    }

    @Override
    protected Map<I, PS> callOriginWithPartitions(
            final List<Map<I, PR>> requestPartitions)
            throws BatchServiceException {
        final var start = System.currentTimeMillis();
        final Map<I, PS> response = new ConcurrentHashMap<>();
        try {
            callOriginParallel(requestPartitions, response::putAll);
        } catch (final ExecutionException e) {
            throw new BatchServiceException(e.getCause().getMessage(), e.getCause());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BatchServiceException("Origin call timed out.", e);
        } catch (final TimeoutException e) {
            throw new BatchServiceException("Origin call timed out.", e);
        } finally {
            final var end = System.currentTimeMillis();
            logger().debug("Fetch all from origin completed under {} ms.", end - start);
        }
        return response;
    }

    private void callCacheParallel(
            final CacheRefreshStrategy strategy,
            final Map<I, PR> requestMap,
            final BiConsumer<I, PS> resultConsumer)
            throws InterruptedException, ExecutionException, TimeoutException {
        cachePool
                .submit(() -> requestMap.entrySet()
                        .parallelStream()
                        .forEach(e -> fetchOneFromCache(strategy, e.getValue())
                                .ifPresent(v -> resultConsumer.accept(e.getKey(), v))))
                .get(partialCacheSupport().timeoutMillis(), TimeUnit.MILLISECONDS);
    }

    private void callOriginParallel(
            final List<Map<I, PR>> requestPartitions,
            final Consumer<Map<I, PS>> responseProcessor)
            throws InterruptedException, ExecutionException, TimeoutException {
        originPool
                .submit(() -> requestPartitions
                        .parallelStream()
                        .map(partition -> fetchSinglePartitionFromOrigin(partition,
                                batchServiceCaller().refreshStrategy()))
                        .forEach(responseProcessor))
                .get(batchServiceCaller().timeoutMillis(), TimeUnit.MILLISECONDS);
    }
}
