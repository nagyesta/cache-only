package com.github.nagyesta.cacheonly.example.replies.transform;

import com.github.nagyesta.cacheonly.example.replies.request.ThreadRequest;
import com.github.nagyesta.cacheonly.transform.BatchRequestTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommentBatchRequestTransformer implements BatchRequestTransformer<ThreadRequest, ThreadRequest, Long> {

    @NotNull
    @Override
    public Map<Long, ThreadRequest> splitToPartialRequest(final @NotNull ThreadRequest batchRequest) {
        return batchRequest.getThreadIds().stream()
                .collect(Collectors.toMap(Function.identity(),
                        id -> ThreadRequest.builder()
                                .threadIds(Collections.singletonList(id))
                                .articleId(batchRequest.getArticleId())
                                .build()));
    }

    @Nullable
    @Override
    public ThreadRequest mergeToBatchRequest(final @NotNull Map<Long, ThreadRequest> requestMap) {
        return requestMap.values().stream().findFirst()
                .map(ThreadRequest::getArticleId)
                .map(u -> ThreadRequest.builder()
                        .threadIds(new ArrayList<>(requestMap.keySet()))
                        .articleId(u)
                        .build())
                .orElse(null);
    }
}
