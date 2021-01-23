package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.example.replies.CommentContext;
import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.example.replies.response.Comment;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentPartialCacheSupport implements PartialCacheSupport<ThreadRequest, List<Comment>, String, Long> {

    private final CacheManager cacheManager;

    @Autowired
    public CommentPartialCacheSupport(final @NotNull CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @NotNull
    @Override
    public String cacheName() {
        return CommentContext.THREADS;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Class<List<Comment>> getEntityClass() {
        return (Class<List<Comment>>) (Object) List.class;
    }

    @NotNull
    @Override
    public CacheKey<String, Long> toCacheKey(final @NotNull ThreadRequest partialRequest) {
        final Long id = partialRequest.getThreadIds().get(0);
        return new CacheKey<>(partialRequest.getArticleId().toString() + "_thread_" + id, id);
    }

    @NotNull
    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
