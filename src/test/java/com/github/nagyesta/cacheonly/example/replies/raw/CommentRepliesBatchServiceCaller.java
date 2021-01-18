package com.github.nagyesta.cacheonly.example.replies.raw;

import com.github.nagyesta.cacheonly.core.CacheRefreshStrategy;
import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import com.github.nagyesta.cacheonly.raw.BatchServiceCaller;
import com.github.nagyesta.cacheonly.raw.exception.BatchServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

import static org.mockito.Mockito.spy;

@Service
public class CommentRepliesBatchServiceCaller
        implements BatchServiceCaller<ThreadRequest, CommentThreads> {

    private static final int PARTITION_SIZE = 5;

    private final CommentService commentService;

    @Autowired
    public CommentRepliesBatchServiceCaller(final CommentService commentService) {
        this.commentService = spy(commentService);
    }

    @Override
    public int maxPartitionSize() {
        return PARTITION_SIZE;
    }

    @Override
    public CacheRefreshStrategy refreshStrategy() {
        return CacheRefreshStrategy.OPPORTUNISTIC;
    }

    @Override
    public CommentThreads callBatchService(final ThreadRequest batchRequest)
            throws BatchServiceException {
        // we call the service here
        try {
            return commentService.threadsOf(batchRequest.getArticleId(), new HashSet<>(batchRequest.getThreadIds()));
        } catch (final Exception e) {
            throw new BatchServiceException(e.getMessage(), e);
        }
    }

    public final CommentService getCommentService() {
        return commentService;
    }
}
