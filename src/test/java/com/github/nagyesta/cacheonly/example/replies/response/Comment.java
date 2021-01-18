package com.github.nagyesta.cacheonly.example.replies.response;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode
public class Comment {
    private UUID articleId;
    private long commentId;
    private Long threadId;
    private String message;
    private String author;
}
