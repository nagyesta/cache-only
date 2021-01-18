package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.entity.CacheKey;
import com.github.nagyesta.cacheonly.example.replies.CommentContext;
import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.example.replies.response.Comment;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentPartialCacheSupport implements PartialCacheSupport<ThreadRequest, List<Comment>, String, Long> {

    private final CacheManager cacheManager;

    @Autowired
    public CommentPartialCacheSupport(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public String cacheName() {
        return CommentContext.THREADS;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<List<Comment>> getEntityClass() {
        return (Class<List<Comment>>) (Object) List.class;
    }

    @Override
    public CacheKey<String, Long> toCacheKey(final ThreadRequest partialRequest) {
        final Long id = partialRequest.getThreadIds().get(0);
        return new CacheKey<>(partialRequest.getArticleId().toString() + "_thread_" + id, id);
    }

    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
