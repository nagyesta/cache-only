package com.github.nagyesta.cacheonly.example.replies.response;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("checkstyle:DesignForExtension")
public record Comment(UUID articleId, long commentId, @Nullable Long threadId, String message, String author) {

}
