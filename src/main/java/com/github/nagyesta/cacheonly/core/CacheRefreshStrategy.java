package com.github.nagyesta.cacheonly.core;

import org.apache.commons.collections4.SetUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines the supported strategies used for refreshing the cache using batch calls.
 */
public enum CacheRefreshStrategy {
    /**
     * Only calls the batch service when explicit cache refresh is performed (for example
     * using {@link CachingServiceTemplate#callBatchServiceAndPutAllToCache(Object)}).
     * Every other time, the items not found in cache will be simply skipped.
     */
    CACHE_ONLY {
        @NotNull
        @Override
        public <I> Set<I> selectItemsForFetch(final @NotNull Set<I> allRequestIds,
                                              final @NotNull Set<I> idsFoundInCache,
                                              final int maxPartitionSize) {
            assertInputIsValid(allRequestIds, idsFoundInCache, maxPartitionSize);
            return Collections.emptySet();
        }
    },
    /**
     * Only calls the batch service for entities when they weren't found in the cache.
     */
    OPTIMISTIC,
    /**
     * Calls the batch service with the maximum amount of request items when we must call
     * the service anyway. This way some of the cached entities get refreshed before their
     * expiry (extending their expiry). This can be useful when a large number of items in
     * the cache is not an issue.
     */
    OPPORTUNISTIC {
        @NotNull
        @Override
        public <I> Set<I> selectItemsForFetch(final @NotNull Set<I> allRequestIds,
                                              final @NotNull Set<I> idsFoundInCache,
                                              final int maxPartitionSize) {
            assertInputIsValid(allRequestIds, idsFoundInCache, maxPartitionSize);
            final Set<I> result = new HashSet<>(SetUtils.difference(allRequestIds, idsFoundInCache));
            if (result.size() > 0) {
                final int mustBeInLastPartition = result.size() % maxPartitionSize;
                final List<I> fetchExtra = new ArrayList<>(idsFoundInCache);
                Collections.shuffle(fetchExtra);
                fetchExtra.stream()
                        .limit(maxPartitionSize - mustBeInLastPartition)
                        .forEach(result::add);
            }
            return result;
        }
    },
    /**
     * If any of the items were not found in the cache, it won't even try the rest of the
     * cached items and will call the real service for all of the items. This can reduce
     * the overhead spent on caching when we know cache miss occurrences are likely signaling
     * a larger number of items missing.
     */
    PESSIMISTIC {
        @Override
        public boolean shouldFailOnMiss() {
            return true;
        }

        @NotNull
        @Override
        public <I> Set<I> selectItemsForFetch(final @NotNull Set<I> allRequestIds,
                                              final @NotNull Set<I> idsFoundInCache,
                                              final int maxPartitionSize) {
            assertInputIsValid(allRequestIds, idsFoundInCache, maxPartitionSize);
            Set<I> result = SetUtils.difference(allRequestIds, idsFoundInCache);
            if (!result.isEmpty()) {
                result = allRequestIds;
            }
            return result;
        }
    },
    /**
     * Never uses cache for read or write. Useful when you want to use only the request
     * partitioning and response merging functionality.
     * Recommended to use with {@link org.springframework.cache.support.NoOpCacheManager}.
     */
    NEVER_CACHE {
        @Override
        public boolean allowsCacheGet() {
            return false;
        }

        @Override
        public boolean allowsCachePut() {
            return false;
        }
    };

    /**
     * Returns whether this strategy allows us to use the cache for GET scenarios.
     *
     * @return true if cache GET is allowed, false otherwise.
     */
    public boolean allowsCacheGet() {
        return true;
    }

    /**
     * Returns whether we should stop using the cache for the remaining requests after the
     * first cache MISS.
     *
     * @return true if we should fail on the first miss, false otherwise.
     */
    public boolean shouldFailOnMiss() {
        return false;
    }

    /**
     * Returns whether this strategy allows us to use the cache for PUT scenarios.
     *
     * @return true if cache PUT is allowed, false otherwise.
     */
    public boolean allowsCachePut() {
        return true;
    }

    /**
     * Filters the set of request Ids considering the Ids found in the cache and the maximum
     * partition size. depending on the current strategy, we can decide to keep all or none
     * of the request Ids selected for fetching (or anything in between these two extremes).
     *
     * @param allRequestIds    The set of all partial request Ids in the batch.
     * @param idsFoundInCache  The set of all partial request Ids we have found in cache.
     * @param maxPartitionSize The maximum partition size we can use in a single batch.
     * @param <I>              The type of the request Id.
     * @return The set of request Ids we want to fetch.
     */
    @NotNull
    public <I> Set<I> selectItemsForFetch(final @NotNull Set<I> allRequestIds,
                                          final @NotNull Set<I> idsFoundInCache,
                                          final int maxPartitionSize) {
        assertInputIsValid(allRequestIds, idsFoundInCache, maxPartitionSize);
        return SetUtils.difference(allRequestIds, idsFoundInCache);
    }

    protected <I> void assertInputIsValid(final @NotNull Set<I> allRequestIds,
                                          final @NotNull Set<I> idsFoundInCache,
                                          final int maxPartitionSize) {
        Assert.notNull(allRequestIds, "AllRequestIds cannot be null.");
        Assert.noNullElements(allRequestIds.toArray(), "AllRequestIds cannot contain null.");
        Assert.notNull(idsFoundInCache, "IdsFoundInCache cannot be null.");
        Assert.noNullElements(idsFoundInCache.toArray(), "IdsFoundInCache cannot contain null.");
        Assert.isTrue(maxPartitionSize > 0, "MaxPartitionSize must be at least 1.");
        final Set<I> foundInCacheButNotWanted = SetUtils.difference(idsFoundInCache, allRequestIds);
        Assert.isTrue(foundInCacheButNotWanted.isEmpty(), "Unexpected Id(s) found in cache: " + foundInCacheButNotWanted);
    }
}
