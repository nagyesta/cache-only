package com.github.nagyesta.cacheonly.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;

/**
 * Defines how a given partial request-response pair should be cached.
 *
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the request Id.
 */
@SuppressWarnings("java:S119") //the type parameter names are easier to recognize this way
public interface PartialCacheSupport<PR, PS, C, I> {

    /**
     * The name of the cache we will use for caching this pair.
     *
     * @return The cache name
     */
    @NotNull
    String cacheName();

    /**
     * Returns the class of the cached entity.
     *
     * @return The cached class
     */
    @NotNull
    Class<PS> getEntityClass();

    /**
     * Converts a partial request to a cache key.
     *
     * @param partialRequest The partial request.
     * @return The cache key
     */
    @Nullable
    CacheKey<C, I> toCacheKey(@NotNull PR partialRequest);

    /**
     * Returns the cache manager instance used for this caching operation.
     *
     * @return the cache manager
     */
    @NotNull
    CacheManager getCacheManager();

    /**
     * Returns the cache instance that should be used.
     *
     * @return The cache
     */
    @Nullable
    default Cache obtainCache() {
        return getCacheManager().getCache(cacheName());
    }

    /**
     * Puts the partial response entity into the cache with the given key.
     *
     * @param key    The cache key.
     * @param entity The entity we want to cache.
     */
    default void putToCache(final @NotNull CacheKey<C, I> key, final @NotNull PS entity) {
        Optional.ofNullable(obtainCache())
                .ifPresent(cache -> cache.put(key.key(), entity));
    }

    /**
     * Tries to get a previously cached partial response based on the key.
     *
     * @param key The cache key.
     * @return A partial response identified by the key or null in case of cache miss.
     */
    @Nullable
    default PS getFromCache(final @NotNull CacheKey<C, I> key) {
        return Optional.ofNullable(obtainCache())
                .map(cache -> cache.get(key.key(), getEntityClass()))
                .orElse(null);
    }
}
