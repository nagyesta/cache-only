package com.github.nagyesta.cacheonly.example.replies.response;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode
public class CommentThreads {
    private Map<Long, List<Comment>> threads;

    public CommentThreads() {
    }

    public CommentThreads(final Map<Long, List<Comment>> threads) {
        this.threads = threads;
    }

}
