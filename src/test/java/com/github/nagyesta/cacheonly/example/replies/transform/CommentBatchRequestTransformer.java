package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommentBatchRequestTransformer implements BatchRequestTransformer<ThreadRequest, ThreadRequest, Long> {

    @Override
    public Map<Long, ThreadRequest> splitToPartialRequest(final ThreadRequest batchRequest) {
        return batchRequest.getThreadIds().stream()
                .collect(Collectors.toMap(Function.identity(),
                        id -> ThreadRequest.builder()
                                .threadIds(Collections.singletonList(id))
                                .articleId(batchRequest.getArticleId())
                                .build()));
    }

    @Override
    public ThreadRequest mergeToBatchRequest(final Map<Long, ThreadRequest> requestMap) {
        return requestMap.values().stream().findFirst()
                .map(ThreadRequest::getArticleId)
                .map(u -> ThreadRequest.builder()
                        .threadIds(new ArrayList<>(requestMap.keySet()))
                        .articleId(u)
                        .build())
                .orElse(ThreadRequest.builder().build());
    }
}
