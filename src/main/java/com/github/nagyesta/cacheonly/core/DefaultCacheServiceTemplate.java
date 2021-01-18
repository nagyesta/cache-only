package com.github.nagyesta.cacheonly.core;

import com.github.nagyesta.cacheonly.core.exception.CacheMissException;
import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link CachingServiceTemplate}.
 *
 * @param <BR> The type of the batch request.
 * @param <BS> The type of the batch response.
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the Id which allows unique association of partial request
 *             and partial response pairs in the scope of the batch.
 */
public class DefaultCacheServiceTemplate<BR, BS, PR, PS, C, I>
        implements CachingServiceTemplate<BR, BS> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCacheServiceTemplate.class);

    private final PartialCacheSupport<PR, PS, C, I> partialCacheSupport;
    private final BatchRequestTransformer<BR, PR, I> batchRequestTransformer;
    private final BatchResponseTransformer<BS, PS, I> batchResponseTransformer;
    private final BatchServiceCaller<BR, BS> batchServiceCaller;

    /**
     * Creates a new instance and injects all of the dependencies which are necessary for it to work.
     *
     * @param partialCacheSupport      The component defining how caching should work for a partial request.
     * @param batchRequestTransformer  The component handling transformations between batch and partial requests.
     * @param batchResponseTransformer The component handling transformations between batch and partial responses.
     * @param batchServiceCaller       The wrapper which is calling the real batch service in case of cache miss.
     */
    public DefaultCacheServiceTemplate(@NonNull final PartialCacheSupport<PR, PS, C, I> partialCacheSupport,
                                       @NonNull final BatchRequestTransformer<BR, PR, I> batchRequestTransformer,
                                       @NonNull final BatchResponseTransformer<BS, PS, I> batchResponseTransformer,
                                       @NonNull final BatchServiceCaller<BR, BS> batchServiceCaller) {
        this.partialCacheSupport = partialCacheSupport;
        this.batchRequestTransformer = batchRequestTransformer;
        this.batchResponseTransformer = batchResponseTransformer;
        this.batchServiceCaller = batchServiceCaller;
    }

    @Override
    public BS callCacheableBatchService(final BR request) throws BatchServiceException {
        Map<I, PR> requestMap = batchRequestTransformer.splitToPartialRequest(request);
        Map<I, PS> fromCache;
        try {
            fromCache = resolveFromCache(requestMap, batchServiceCaller.refreshStrategy());
            requestMap = selectRemainingKeysForFetch(requestMap, fromCache);
        } catch (final CacheMissException e) {
            fromCache = new HashMap<>();
            LOGGER.info("Cache miss found for requestClass: {}, refresh strategy disallows further tries. Cause: {}",
                    request.getClass().getName(), e.getMessage());
        }
        fromCache.putAll(resolveFromBatchService(requestMap));
        return batchResponseTransformer.mergeToBatchResponse(fromCache);
    }

    @Override
    public BS callBatchServiceAndPutAllToCache(final BR request) throws BatchServiceException {
        final Map<I, PR> requestMap = batchRequestTransformer.splitToPartialRequest(request);
        final Map<I, PS> response = resolveFromBatchService(requestMap);
        return batchResponseTransformer.mergeToBatchResponse(response);
    }

    private Map<I, PS> resolveFromBatchService(final Map<I, PR> requestMap) throws BatchServiceException {
        final Map<I, PS> response;
        if (requestMap.isEmpty()) {
            response = Collections.emptyMap();
        } else if (requestMap.size() < batchServiceCaller.maxPartitionSize()) {
            response = processSingleBatch(requestMap, batchServiceCaller.refreshStrategy());
        } else {
            final List<Map<I, PR>> requestList = splitIntoPartitions(requestMap);
            response = new HashMap<>();
            for (final Map<I, PR> partitionedMap : requestList) {
                response.putAll(processSingleBatch(partitionedMap, batchServiceCaller.refreshStrategy()));
            }
        }
        return response;
    }

    private List<Map<I, PR>> splitIntoPartitions(final Map<I, PR> requestMap) {
        final ArrayList<I> keyList = new ArrayList<>(requestMap.keySet());
        final List<List<I>> partitions = ListUtils.partition(keyList, batchServiceCaller.maxPartitionSize());
        return partitions.stream()
                .map(p -> p.stream().collect(Collectors.toMap(Function.identity(), requestMap::get)))
                .collect(Collectors.toList());
    }

    private Map<I, PS> processSingleBatch(final Map<I, PR> requestMap,
                                          final CacheRefreshStrategy strategy) throws BatchServiceException {
        final Map<I, PS> response = new HashMap<>();
        fetchResponses(requestMap).forEach((id, entity) -> {
            if (strategy.allowsCachePut()) {
                final CacheKey<C, I> cacheKey = partialCacheSupport.toCacheKey(requestMap.get(id));
                partialCacheSupport.putToCache(cacheKey, entity);
            }
            response.put(id, entity);
        });
        return response;
    }

    private Map<I, PR> selectRemainingKeysForFetch(final Map<I, PR> requestMap, final Map<I, PS> fromCache) {
        final Set<I> toBeFetched = batchServiceCaller.refreshStrategy()
                .selectItemsForFetch(requestMap.keySet(), fromCache.keySet(), batchServiceCaller.maxPartitionSize());
        return toBeFetched.stream()
                .collect(Collectors.toMap(Function.identity(), requestMap::get));
    }

    private Map<I, PS> fetchResponses(final Map<I, PR> requestMap) throws BatchServiceException {
        final BR batchRequest = batchRequestTransformer.mergeToBatchRequest(requestMap);
        final BS listResponse = batchServiceCaller.callBatchService(batchRequest);
        return batchResponseTransformer.splitToPartialResponse(listResponse);
    }

    private Map<I, PS> resolveFromCache(final Map<I, PR> requestMap,
                                        final CacheRefreshStrategy strategy) throws CacheMissException {
        final Map<I, PS> result = new HashMap<>();
        if (strategy.allowsCacheGet()) {
            for (final Map.Entry<I, PR> entry : requestMap.entrySet()) {
                handleResult(strategy, entry, result::put);
            }
        }
        return result;
    }

    private void handleResult(final CacheRefreshStrategy strategy,
                              final Map.Entry<I, PR> entry,
                              final BiConsumer<I, PS> resultConsumer) throws CacheMissException {
        final CacheKey<C, I> key = partialCacheSupport.toCacheKey(entry.getValue());
        final Optional<PS> fromCache = Optional.ofNullable(partialCacheSupport.getFromCache(key));
        if (fromCache.isPresent()) {
            resultConsumer.accept(entry.getKey(), fromCache.get());
        } else if (strategy.shouldFailOnMiss()) {
            throw new CacheMissException("Item with id not found in cache: " + key);
        }
    }
}
