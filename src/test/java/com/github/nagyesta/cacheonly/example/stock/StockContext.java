package com.github.nagyesta.cacheonly.example.stock;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

import static org.mockito.Mockito.spy;

@Configuration
@EnableCaching
@ComponentScan
public class StockContext {

    /**
     * The name of the cache.
     */
    public static final String STOCKS = "stocks";

    @Bean
    public CacheManager cacheManager() {
        final var cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Collections.singletonList(spy(new ConcurrentMapCache(STOCKS))));
        return cacheManager;
    }
}
