package com.github.nagyesta.cacheonly.example.replies;

import com.github.nagyesta.cacheonly.core.DefaultCacheServiceTemplate;
import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.example.replies.response.Comment;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.PartialCacheSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentCacheServiceTemplate
        extends DefaultCacheServiceTemplate<ThreadRequest, CommentThreads, ThreadRequest, List<Comment>, String, Long> {

    @Autowired
    public CommentCacheServiceTemplate(
            final @NotNull PartialCacheSupport<ThreadRequest, List<Comment>, String, Long> partialCacheSupport,
            final @NotNull BatchRequestTransformer<ThreadRequest, ThreadRequest, Long> batchRequestTransformer,
            final @NotNull BatchResponseTransformer<CommentThreads, List<Comment>, Long> batchResponseTransformer,
            final @NotNull BatchServiceCaller<ThreadRequest, CommentThreads> batchServiceCaller) {
        super(partialCacheSupport, batchRequestTransformer, batchResponseTransformer, batchServiceCaller);
    }
}
