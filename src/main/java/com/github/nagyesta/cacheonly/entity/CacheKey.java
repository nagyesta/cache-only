package com.github.nagyesta.cacheonly.entity;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a cache key and the Id which can help us find the request / response it belongs to.
 *
 * @param <C> The type of the cache key.
 * @param <I> The type of the Id.
 */
public final class CacheKey<C, I> {

    private final C key;
    private final I id;

    /**
     * Creates a new instance.
     *
     * @param key The cache key value.
     * @param id  The Id of the partial request this key belongs to.
     */
    public CacheKey(final C key, final I id) {
        this.key = key;
        this.id = id;
    }

    /**
     * Returns the cache key value.
     *
     * @return key
     */
    public C getKey() {
        return key;
    }

    /**
     * Returns the partial request Id.
     *
     * @return id
     */
    public I getId() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CacheKey)) {
            return false;
        }
        final CacheKey<?, ?> cacheKey = (CacheKey<?, ?>) o;
        return key.equals(cacheKey.key) && id.equals(cacheKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CacheKey.class.getSimpleName() + "[", "]")
                .add("key=" + key)
                .add("id=" + id)
                .toString();
    }
}
