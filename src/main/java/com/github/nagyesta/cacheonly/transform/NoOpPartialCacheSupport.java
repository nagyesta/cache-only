package com.github.nagyesta.cacheonly.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.transform.concurrent.AsyncPartialCacheSupport;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.CacheManager;

/**
 * No-operation implementation of {@link PartialCacheSupport}.
 *
 * @param <PR> The type of the partial request.
 * @param <PS> The type of the partial response.
 * @param <C>  The type of the cache key.
 * @param <I>  The type of the request ID.
 */
@SuppressWarnings("java:S119") //the type parameter names are easier to recognize this way
public class NoOpPartialCacheSupport<PR, PS, C, I> implements AsyncPartialCacheSupport<PR, PS, C, I> {
    @Override
    public String cacheName() {
        throw new UnsupportedOperationException("No-Op implementation does not support this operation.");
    }

    @Override
    public Class<PS> getEntityClass() {
        throw new UnsupportedOperationException("No-Op implementation does not support this operation.");
    }

    @SuppressWarnings("java:S2638") //false positive
    @Override
    public @Nullable CacheKey<C, I> toCacheKey(final PR partialRequest) {
        return null;
    }

    @Override
    public CacheManager getCacheManager() {
        throw new UnsupportedOperationException("No-Op implementation does not support this operation.");
    }

    @Override
    public void putToCache(
            final CacheKey<C, I> key,
            final PS entity) {
        //noop
    }

    @Override
    public @Nullable PS getFromCache(final CacheKey<C, I> key) {
        return null;
    }
}
