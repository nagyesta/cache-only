package com.github.nagyesta.cacheonly.example.replies.response;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("checkstyle:DesignForExtension")
public class CommentThreads {

    @Nullable
    private Map<Long, List<Comment>> threads;

    public CommentThreads() {
    }

    public CommentThreads(final Map<Long, List<Comment>> threads) {
        this.threads = threads;
    }

    public @Nullable Map<Long, List<Comment>> getThreads() {
        return threads;
    }

    public void setThreads(final Map<Long, List<Comment>> threads) {
        this.threads = threads;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var that = (CommentThreads) o;
        return Objects.equals(threads, that.threads);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(threads);
    }

    @Override
    public String toString() {
        return "CommentThreads{"
                + "threads=" + threads
                + '}';
    }
}
