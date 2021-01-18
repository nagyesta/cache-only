package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.example.replies.response.Comment;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CommentBatchResponseTransformer implements BatchResponseTransformer<CommentThreads, List<Comment>, Long> {

    @Override
    public Map<Long, List<Comment>> splitToPartialResponse(final CommentThreads batchResponse) {
        return batchResponse.getThreads();
    }

    @Override
    public CommentThreads mergeToBatchResponse(final Map<Long, List<Comment>> entityMap) {
        return CommentThreads.builder()
                .threads(entityMap)
                .build();
    }
}
