package com.github.nagyesta.cacheonly.entity;

/**
 * Represents a cache key and the ID which can help us find the request / response it belongs to.
 *
 * @param <C> The type of the cache key.
 * @param <I> The type of the ID.
 * @param key The key we use for the cache.
 * @param id  The ID of the entity.
 */
public record CacheKey<C, I>(C key, I id) {
}
