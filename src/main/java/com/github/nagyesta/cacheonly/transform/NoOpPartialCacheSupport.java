package com.github.nagyesta.cacheonly.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.transform.concurrent.AsyncPartialCacheSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.CacheManager;

/**
 * No-operation implementation of {@link PartialCacheSupport}.
 *
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the request Id.
 */
public class NoOpPartialCacheSupport<PR, PS, C, I> implements AsyncPartialCacheSupport<PR, PS, C, I> {
    @Override
    public @NotNull String cacheName() {
        throw new UnsupportedOperationException("No-Op implementation does not support this operation.");
    }

    @Override
    public @NotNull Class<PS> getEntityClass() {
        throw new UnsupportedOperationException("No-Op implementation does not support this operation.");
    }

    @Nullable
    @Override
    public CacheKey<C, I> toCacheKey(final @NotNull PR partialRequest) {
        return null;
    }

    @Override
    public @NotNull CacheManager getCacheManager() {
        throw new UnsupportedOperationException("No-Op implementation does not support this operation.");
    }

    @Override
    public void putToCache(final @NotNull CacheKey<C, I> key, final @NotNull PS entity) {
        //noop
    }

    @Nullable
    @Override
    public PS getFromCache(final @NotNull CacheKey<C, I> key) {
        return null;
    }
}
