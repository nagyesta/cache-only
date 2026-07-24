package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.example.replies.CommentContext;
import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CommentPartialCacheSupport implements PartialCacheSupport<ThreadRequest, CommentThreads, String, Long> {

    private final CacheManager cacheManager;

    @Autowired
    public CommentPartialCacheSupport(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public String cacheName() {
        return CommentContext.THREADS;
    }

    @Override
    public Class<CommentThreads> getEntityClass() {
        return CommentThreads.class;
    }

    @Override
    public CacheKey<String, Long> toCacheKey(final ThreadRequest partialRequest) {
        final var id = Objects.requireNonNull(partialRequest.getThreadIds()).get(0);
        return new CacheKey<>(Objects.requireNonNull(partialRequest.getArticleId()) + "_thread_" + id, id);
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
