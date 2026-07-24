package com.github.nagyesta.cacheonly.example.stock.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.example.stock.StockContext;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StockPartialCacheSupport implements PartialCacheSupport<String, BigDecimal, String, String> {

    private final CacheManager cacheManager;

    @Autowired
    public StockPartialCacheSupport(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public String cacheName() {
        return StockContext.STOCKS;
    }

    @Override
    public Class<BigDecimal> getEntityClass() {
        return BigDecimal.class;
    }

    @Override
    public CacheKey<String, String> toCacheKey(final String partialRequest) {
        return new CacheKey<>("price_" + partialRequest, partialRequest);
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
