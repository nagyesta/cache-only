package com.github.nagyesta.cacheonly.example.replies.request;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("checkstyle:DesignForExtension")
public class ThreadRequest {
    @Nullable
    private UUID articleId;
    @Nullable
    private List<Long> threadIds;

    public ThreadRequest() {
    }

    public ThreadRequest(
            final UUID articleId,
            final List<Long> threadIds) {
        this.articleId = articleId;
        this.threadIds = threadIds;
    }

    public @Nullable UUID getArticleId() {
        return articleId;
    }

    public void setArticleId(@Nullable final UUID articleId) {
        this.articleId = articleId;
    }

    public @Nullable List<Long> getThreadIds() {
        return threadIds;
    }

    public void setThreadIds(final List<Long> threadIds) {
        this.threadIds = threadIds;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var that = (ThreadRequest) o;
        return Objects.equals(articleId, that.articleId) && Objects.equals(threadIds, that.threadIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId, threadIds);
    }

    @Override
    public String toString() {
        return "ThreadRequest{"
                + "articleId=" + articleId
                + ", threadIds=" + threadIds
                + '}';
    }
}
