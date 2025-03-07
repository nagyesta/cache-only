package com.github.nagyesta.cacheonly.example.unstable;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.transform.concurrent.AsyncPartialCacheSupport;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

import java.util.concurrent.ForkJoinPool;

@SuppressWarnings("checkstyle:MagicNumber")
@Slf4j
public class UnstablePartialCacheSupport implements AsyncPartialCacheSupport<Long, String, String, Long> {

    @Override
    public long timeoutMillis() {
        return 10;
    }

    @NotNull
    @Override
    public String cacheName() {
        return "none";
    }

    @NotNull
    @Override
    public Class<String> getEntityClass() {
        return String.class;
    }

    @Nullable
    @Override
    public CacheKey<String, Long> toCacheKey(final @NotNull Long partialRequest) {
        return new CacheKey<>(String.valueOf(partialRequest), partialRequest);
    }

    @NotNull
    @Override
    public CacheManager getCacheManager() {
        return new NoOpCacheManager();
    }

    @Override
    public @NotNull ForkJoinPool forkJoinPool() {
        return new ForkJoinPool(2);
    }

    @Nullable
    @Override
    public String getFromCache(final @NotNull CacheKey<String, Long> key) {
        handleExceptionalCases(key);
        if (key.id() < 5 || key.id() > 20) {
            return null;
        }
        return key.key();
    }

    private void handleExceptionalCases(final @NotNull CacheKey<String, Long> key) {
        if (key.id() == -15L) {
            throw new IllegalStateException("Get failed.");
        }
        if (key.id() < -30) {
            try {
                final var start = System.currentTimeMillis();
                Thread.sleep(60);
                final var end = System.currentTimeMillis();
                log.trace("Took: {} ms", (end - start));
            } catch (final InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
