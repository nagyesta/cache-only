package com.github.nagyesta.cacheonly.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Objects;

/**
 * Defines how a given partial request-response pair should be cached.
 *
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the request Id.
 */
public interface PartialCacheSupport<PR, PS, C, I> {

    /**
     * The name of the cache we will use for caching this pair.
     *
     * @return The cache name
     */
    String cacheName();

    /**
     * Returns the class of the cached entity.
     *
     * @return The cached class
     */
    Class<PS> getEntityClass();

    /**
     * Converts a partial request to a cache key.
     *
     * @param partialRequest The partial request.
     * @return The cache key
     */
    CacheKey<C, I> toCacheKey(PR partialRequest);

    /**
     * Returns the cache manager instance used for this caching operation.
     *
     * @return the cache manager
     */
    CacheManager getCacheManager();

    /**
     * Returns the cache instance that should be used.
     *
     * @return The cache
     */
    default Cache obtainCache() {
        return Objects.requireNonNull(getCacheManager().getCache(cacheName()));
    }

    /**
     * Puts the partial response entity into the cache with the given key.
     *
     * @param key    The cache key.
     * @param entity The entity we want to cache.
     */
    default void putToCache(final CacheKey<C, I> key, final PS entity) {
        obtainCache().put(key.getKey(), entity);
    }

    /**
     * Tries to get a previously cached partial response based on the key.
     *
     * @param key The cache key.
     * @return A partial response identified by the key or null in case of cache miss.
     */
    default PS getFromCache(final CacheKey<C, I> key) {
        return obtainCache().get(key.getKey(), getEntityClass());
    }
}
