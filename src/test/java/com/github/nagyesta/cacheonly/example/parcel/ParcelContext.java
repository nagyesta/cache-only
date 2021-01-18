package com.github.nagyesta.cacheonly.example.parcel;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ComponentScan
public class ParcelContext {

    @Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}
