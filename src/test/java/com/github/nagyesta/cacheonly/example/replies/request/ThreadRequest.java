package com.github.nagyesta.cacheonly.example.replies.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ThreadRequest {
    private UUID articleId;
    private List<Long> threadIds;

    public ThreadRequest() {
    }

    public ThreadRequest(
            final UUID articleId,
            final List<Long> threadIds) {
        this.articleId = articleId;
        this.threadIds = threadIds;
    }
}
