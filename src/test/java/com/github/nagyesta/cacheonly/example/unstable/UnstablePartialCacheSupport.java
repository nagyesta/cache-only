package com.github.nagyesta.cacheonly.example.unstable;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.transform.concurrent.AsyncPartialCacheSupport;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

import java.util.concurrent.ForkJoinPool;

@SuppressWarnings("checkstyle:MagicNumber")
public class UnstablePartialCacheSupport
        implements AsyncPartialCacheSupport<Long, String, String, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnstablePartialCacheSupport.class);

    @Override
    public long timeoutMillis() {
        return 10;
    }

    @Override
    public String cacheName() {
        return "none";
    }

    @Override
    public Class<String> getEntityClass() {
        return String.class;
    }

    @Override
    public CacheKey<String, Long> toCacheKey(final Long partialRequest) {
        return new CacheKey<>(String.valueOf(partialRequest), partialRequest);
    }

    @Override
    public CacheManager getCacheManager() {
        return new NoOpCacheManager();
    }

    @Override
    public ForkJoinPool forkJoinPool() {
        return new ForkJoinPool(2);
    }

    @Override
    public @Nullable String getFromCache(final CacheKey<String, Long> key) {
        handleExceptionalCases(key);
        if (key.id() < 5 || key.id() > 20) {
            return null;
        }
        return key.key();
    }

    @SuppressWarnings("java:S2925")
    private void handleExceptionalCases(final CacheKey<String, Long> key) {
        if (key.id() == -15L) {
            throw new IllegalStateException("Get failed.");
        }
        if (key.id() < -30) {
            try {
                final var start = System.currentTimeMillis();
                Thread.sleep(60);
                final var end = System.currentTimeMillis();
                LOGGER.trace("Took: {} ms", (end - start));
            } catch (final InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
