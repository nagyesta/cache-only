package com.github.nagyesta.cacheonly.example.parcel.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.example.parcel.response.ParcelResponse;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class ParcelPartialCacheSupport implements PartialCacheSupport<String, ParcelResponse, String, String> {

    private final CacheManager cacheManager;

    @Autowired
    public ParcelPartialCacheSupport(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public String cacheName() {
        throw new UnsupportedOperationException("No need to get cache name.");
    }

    @Override
    public Class<ParcelResponse> getEntityClass() {
        throw new UnsupportedOperationException("No need to get cache entity class but it was attempted.");
    }

    @Override
    public CacheKey<String, String> toCacheKey(final String partialRequest) {
        throw new UnsupportedOperationException("No need to generate a cache key but it was attempted.");
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
