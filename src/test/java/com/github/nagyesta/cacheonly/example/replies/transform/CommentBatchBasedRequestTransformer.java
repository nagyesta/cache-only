package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.transform.common.WrappedCollectionBasedRequestTransformer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommentBatchBasedRequestTransformer extends WrappedCollectionBasedRequestTransformer<ThreadRequest, List<Long>, Long, Long> {

    public CommentBatchBasedRequestTransformer() {
        super(ThreadRequest::new, ThreadRequest::getThreadIds, (request, threadIds) -> {
            request.setThreadIds(threadIds);
            return request;
        }, Collectors.toList(), Function.identity());
    }
}
