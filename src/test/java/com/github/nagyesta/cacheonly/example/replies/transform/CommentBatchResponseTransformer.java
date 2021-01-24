package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.example.replies.response.Comment;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CommentBatchResponseTransformer implements BatchResponseTransformer<CommentThreads, List<Comment>, Long> {

    @NotNull
    @Override
    public Map<Long, List<Comment>> splitToPartialResponse(final @NotNull CommentThreads batchResponse) {
        return batchResponse.getThreads();
    }

    @Nullable
    @Override
    public CommentThreads mergeToBatchResponse(final @NotNull Map<Long, List<Comment>> entityMap) {
        return CommentThreads.builder()
                .threads(entityMap)
                .build();
    }
}
