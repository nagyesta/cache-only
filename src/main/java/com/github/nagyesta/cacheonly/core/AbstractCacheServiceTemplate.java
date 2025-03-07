package com.github.nagyesta.cacheonly.core;

import com.github.nagyesta.cacheonly.core.exception.CacheMissException;
import com.github.nagyesta.cacheonly.core.metrics.BatchServiceCallMetricCollector;
import com.github.nagyesta.cacheonly.core.metrics.NoOpBatchServiceCallMetricCollector;
import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract super implementation of {@link CachingServiceTemplate}.
 *
 * @param <SC> The {@link BatchServiceCaller} implementation we want to use for calling origin.
 * @param <CS> The {@link PartialCacheSupport} implementation we want to use for calling the cache.
 * @param <BR> The type of the batch request.
 * @param <BS> The type of the batch response.
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the ID which allows unique association of partial request
 *             and partial response pairs in the scope of the batch.
 */
public abstract class AbstractCacheServiceTemplate<SC extends BatchServiceCaller<BR, BS>,
        CS extends PartialCacheSupport<PR, PS, C, I>, BR, BS, PR, PS, C, I>
        implements CachingServiceTemplate<BR, BS> {
    private final Logger logger;
    private final CS partialCacheSupport;
    private final BatchRequestTransformer<BR, PR, I> batchRequestTransformer;
    private final BatchResponseTransformer<BS, PS, I> batchResponseTransformer;
    private final SC batchServiceCaller;
    private BatchServiceCallMetricCollector metricsCollector = new NoOpBatchServiceCallMetricCollector();

    public AbstractCacheServiceTemplate(
            final @NotNull CS partialCacheSupport,
            final @NotNull BatchRequestTransformer<BR, PR, I> batchRequestTransformer,
            final @NotNull BatchResponseTransformer<BS, PS, I> batchResponseTransformer,
            final @NotNull SC batchServiceCaller) {
        this.partialCacheSupport = partialCacheSupport;
        this.batchRequestTransformer = batchRequestTransformer;
        this.batchResponseTransformer = batchResponseTransformer;
        this.batchServiceCaller = batchServiceCaller;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Nullable
    @Override
    public BS callCacheableBatchService(
            final @NotNull BR request) throws BatchServiceException {
        final var start = System.currentTimeMillis();
        var requestMap = batchRequestTransformer.splitToPartialRequest(request);
        try {
            logger.info("Processing batch of {} partial requests.", requestMap.size());
            logger.trace("Processing batch of partial requests with ids: {}", requestMap.keySet());
            Map<I, PS> fromCache;
            try {
                fromCache = attemptFetchingFromCache(requestMap, batchServiceCaller.refreshStrategy());
                requestMap = selectRemainingKeysToFetchFromOrigin(requestMap, fromCache);
            } catch (final CacheMissException e) {
                fromCache = new HashMap<>();
                metricsCollector.cacheMiss(requestMap.size());
                logger.info("Cache miss found for requestClass: {}, refresh strategy disallows further tries. Cause: {}",
                        request.getClass().getName(), e.getMessage());
            }
            fromCache.putAll(fetchAllFromOriginService(requestMap));
            return batchResponseTransformer.mergeToBatchResponse(fromCache);
        } finally {
            final var end = System.currentTimeMillis();
            logger().debug("Total execution completed under {} ms.", end - start);
        }
    }

    @Nullable
    @Override
    public BS callBatchServiceAndPutAllToCache(
            final @NotNull BR request) throws BatchServiceException {
        final var requestMap = batchRequestTransformer.splitToPartialRequest(request);
        final var response = fetchAllFromOriginService(requestMap);
        return batchResponseTransformer.mergeToBatchResponse(response);
    }

    /**
     * Resolves the missing items from the origin service.
     *
     * @param requestMap The map of partial requests we need to resolve.
     * @return The Map containing the responses.
     * @throws BatchServiceException When the resolution failed.
     */
    @NotNull
    protected Map<I, PS> fetchAllFromOriginService(
            final @NotNull Map<I, PR> requestMap) throws BatchServiceException {
        final Map<I, PS> response;
        if (requestMap.isEmpty()) {
            response = Collections.emptyMap();
        } else {
            final var partitions = partitionOriginRequests(requestMap);
            metricsCollector.partitionsCreated(partitions.size());
            try {
                response = callOriginWithPartitions(partitions);
                metricsCollector.partitionsSucceeded(partitions.size());
            } catch (final BatchServiceException e) {
                metricsCollector.partitionsFailed(partitions.size());
                throw e;
            }
        }
        return response;
    }

    /**
     * Processes the partitions supplied by calling the origin service.
     *
     * @param requestPartitions The list of request maps we need to make.
     * @return The results returned by the origin service.
     * @throws BatchServiceException When the origin call failed.
     */
    @NotNull
    protected abstract Map<I, PS> callOriginWithPartitions(
            @NotNull List<Map<I, PR>> requestPartitions) throws BatchServiceException;

    /**
     * Evaluates whether the refresh strategy allows us to put to the cache and performs
     * a put with all the responses if it is allowed.
     *
     * @param strategy The refresh strategy.
     * @param request  The request we need to use to find the cache keys when we put items into the cache.
     * @param response The response we need to put into the cache.
     */
    protected void populateCacheWithResponse(
            final @NotNull CacheRefreshStrategy strategy,
            final @NotNull Map<I, PR> request,
            final @NotNull Map<I, PS> response) {
        if (strategy.allowsCachePut()) {
            logger.trace("Responses passed for cache PUT with ids: {}", response.keySet());
            logger.trace("Requests passed for cache PUT with ids: {}", request.keySet());
            Assert.isTrue(request.keySet().containsAll(response.keySet()),
                    "Not all requests ids are found in the request.");
            response.forEach((id, entity) -> Optional.ofNullable(partialCacheSupport.toCacheKey(request.get(id)))
                    .ifPresent(cacheKey -> partialCacheSupport.putToCache(cacheKey, entity)));
            metricsCollector.cachePut(response.size());
            logger.debug("Cache PUT completed for {} items.", response.size());
        } else {
            logger.debug("Cache PUT not allowed by {} strategy, skipping.", strategy.name());
        }
    }

    @NotNull
    private List<Map<I, PR>> partitionOriginRequests(final @NotNull Map<I, PR> requestMap) {
        final var keyList = new ArrayList<>(requestMap.keySet());
        final var partitions = ListUtils.partition(keyList, batchServiceCaller.maxPartitionSize());
        logger.debug("Created {} partitions.", partitions.size());
        return partitions.stream()
                .map(p -> p.stream().collect(Collectors.toMap(Function.identity(), requestMap::get)))
                .collect(Collectors.toList());
    }

    @NotNull
    private Map<I, PR> selectRemainingKeysToFetchFromOrigin(
            final @NotNull Map<I, PR> requestMap,
            final Map<I, PS> fromCache) {
        final var toBeFetched = batchServiceCaller.refreshStrategy()
                .selectItemsForFetch(requestMap.keySet(), fromCache.keySet(), batchServiceCaller.maxPartitionSize());
        logger.trace("Fetch will be performed for ids: {}", toBeFetched);
        logger.debug("Fetch will be performed for {} items.", toBeFetched.size());
        return toBeFetched.stream()
                .collect(Collectors.toMap(Function.identity(), requestMap::get));
    }

    @NotNull
    private Map<I, PS> attemptFetchingFromCache(
            final @NotNull Map<I, PR> requestMap,
            final @NotNull CacheRefreshStrategy strategy)
            throws CacheMissException {
        final Map<I, PS> result = new HashMap<>();
        if (strategy.allowsCacheGet()) {
            metricsCollector.cacheGet(requestMap.size());
            logger.debug("Attempting cache GET for {} items.", requestMap.size());
            result.putAll(fetchAllFromCache(strategy, requestMap));
            metricsCollector.cacheHit(result.size());
            metricsCollector.cacheMiss(requestMap.size() - result.size());
            logger.info("Cache HIT for {} items.", result.size());
            logger.trace("Cache HIT for ids: {}", result.keySet());
        } else {
            logger.debug("Cache GET is not allowed by {} strategy, skipping.", strategy.name());
        }
        return result;
    }

    /**
     * Attempts to fetch a map of entries from cache.
     *
     * @param strategy   The strategy that decides how we should react to a failure.
     * @param requestMap The requests we need to fetch.
     * @return The map of partial responses we have found in the cache.
     * @throws CacheMissException When a request is not found and the strategy does not allow us to continue.
     */
    @NotNull
    protected abstract Map<I, PS> fetchAllFromCache(
            @NotNull CacheRefreshStrategy strategy,
            @NotNull Map<I, PR> requestMap)
            throws CacheMissException;

    /**
     * Attempts to fetch a single entry from cache.
     *
     * @param strategy The strategy that decides how we should react to a failure.
     * @param request  The request we need to fetch.
     * @return The (optional) partial response from the cache.
     * @throws CacheMissException When the request is not found and the strategy does not allow us to continue.
     */
    @NotNull
    protected Optional<PS> fetchOneFromCache(
            final @NotNull CacheRefreshStrategy strategy,
            final @NotNull PR request)
            throws CacheMissException {
        final var key = Optional.ofNullable(partialCacheSupport.toCacheKey(request));
        final var fromCache = key.map(partialCacheSupport::getFromCache);
        if (fromCache.isEmpty()) {
            if (strategy.shouldFailOnMiss()) {
                throw new CacheMissException("Item with id not found in cache: " + key.map(CacheKey::id).orElse(null));
            }
            key.ifPresent(k -> logger.trace("Cache miss observed for key: {}", k));
        }
        return fromCache;
    }


    /**
     * Fetches the response for a single partition and refreshes the cache with the results.
     *
     * @param requestMap The map of partial requests in the current partition.
     * @param strategy   The cache refresh strategy we need to follow.
     * @return The map of partial responses.
     * @throws BatchServiceException When the origin call fails.
     */
    @NotNull
    protected Map<I, PS> fetchSinglePartitionFromOrigin(
            final @NotNull Map<I, PR> requestMap,
            final @NotNull CacheRefreshStrategy strategy)
            throws BatchServiceException {
        final Map<I, PS> response = new HashMap<>(doFetchFromOrigin(requestMap));
        logger().trace("Responses fetched for ids: {}", response.keySet());
        logger().debug("Responses fetched for {} items.", response.size());
        populateCacheWithResponse(strategy, requestMap, response);
        return response;
    }

    @NotNull
    private Map<I, PS> doFetchFromOrigin(
            final @NotNull Map<I, PR> requestMap)
            throws BatchServiceException {
        final var batchRequest = Optional.ofNullable(batchRequestTransformer().mergeToBatchRequest(requestMap));
        Map<I, PS> response = Collections.emptyMap();
        if (batchRequest.isPresent()) {
            final var listResponse = Optional.ofNullable(batchServiceCaller().callBatchService(batchRequest.get()));
            response = listResponse.map(batchResponseTransformer()::splitToPartialResponse).orElse(Collections.emptyMap());
        }
        return response;
    }

    protected final CS partialCacheSupport() {
        return partialCacheSupport;
    }

    protected final SC batchServiceCaller() {
        return batchServiceCaller;
    }

    protected final BatchRequestTransformer<BR, PR, I> batchRequestTransformer() {
        return batchRequestTransformer;
    }

    protected final BatchResponseTransformer<BS, PS, I> batchResponseTransformer() {
        return batchResponseTransformer;
    }

    protected final Logger logger() {
        return logger;
    }

    public final void setMetricsCollector(
            final BatchServiceCallMetricCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }
}
