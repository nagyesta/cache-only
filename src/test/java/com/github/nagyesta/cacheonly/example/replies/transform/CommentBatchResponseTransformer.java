package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.example.replies.response.Comment;
import com.github.nagyesta.cacheonly.example.replies.response.CommentThreads;
import com.github.nagyesta.cacheonly.transform.BatchResponseTransformer;
import com.github.nagyesta.cacheonly.transform.common.WrappedMapBasedResponseTransformer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentBatchResponseTransformer
        extends WrappedMapBasedResponseTransformer<CommentThreads, Map<Long, List<Comment>>, List<Comment>, Long>
        implements BatchResponseTransformer<CommentThreads, CommentThreads, Long> {

    @SuppressWarnings("java:S1612")
    public CommentBatchResponseTransformer() {
        super(() -> new CommentThreads(),
                commentThreads1 -> Objects.requireNonNull(commentThreads1.getThreads()),
                (commentThreads, longListMap) -> {
                    commentThreads.setThreads(longListMap);
                    return commentThreads;
                }, Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
